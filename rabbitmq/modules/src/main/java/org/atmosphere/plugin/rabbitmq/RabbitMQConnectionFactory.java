/*
 * Copyright 2017 Async-IO.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.atmosphere.plugin.rabbitmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.ShutdownListener;
import com.rabbitmq.client.ShutdownSignalException;

import org.atmosphere.cpr.AtmosphereConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * RabbitMQ Connection Factory.
 *
 * @author Thibault Normand
 * @author Jean-Francois Arcand
 */
public class RabbitMQConnectionFactory implements AtmosphereConfig.ShutdownHook, ShutdownListener{
    private static final Logger logger = LoggerFactory.getLogger(RabbitMQBroadcaster.class);

    private static RabbitMQConnectionFactory factory;

    public static final String PARAM_HOST = RabbitMQBroadcaster.class.getName() + ".host";
    public static final String PARAM_USER = RabbitMQBroadcaster.class.getName() + ".user";
    public static final String PARAM_PASS = RabbitMQBroadcaster.class.getName() + ".password";
    public static final String PARAM_EXCHANGE_TYPE = RabbitMQBroadcaster.class.getName() + ".exchange";
    public static final String PARAM_VHOST = RabbitMQBroadcaster.class.getName() + ".vhost";
    public static final String PARAM_PORT = RabbitMQBroadcaster.class.getName() + ".port";
    public static final String PARAM_USE_SSL = RabbitMQBroadcaster.class.getName() + ".ssl";

    private String exchangeName;
    private ConnectionFactory connectionFactory;
    private Connection connection;
    private Channel channel;
    private String exchange;
    private volatile boolean shutdown;

    private String host;
    private String vhost;
    private String user;
    private String port;
    private String password;
    private boolean useSsl;

    public RabbitMQConnectionFactory(AtmosphereConfig config) {

	shutdown = true;
        String s = config.getInitParameter(PARAM_EXCHANGE_TYPE);
        if (s != null) {
            exchange = s;
        } else {
            exchange = "topic";
        }

        host = config.getInitParameter(PARAM_HOST);
        if (host == null) {
            host = "127.0.0.1";
        }

        vhost = config.getInitParameter(PARAM_VHOST);
        if (vhost == null) {
            vhost = "/";
        }

        user = config.getInitParameter(PARAM_USER);
        if (user == null) {
            user = "guest";
        }

        useSsl = Boolean.valueOf(config.getInitParameter(PARAM_USE_SSL, "false"));

        port = config.getInitParameter(PARAM_PORT);
        if (port == null) {
            if(useSsl){
        	port = "5671";
            }else{
        	port = "5672";
            }
        }

        password = config.getInitParameter(PARAM_PASS);
        if (password == null) {
            password = "guest";
        }
        

        exchangeName = "atmosphere." + exchange;
        reInit();
        config.shutdownHook(this);
    }

    private void reInit() throws RuntimeException {
	if(shutdown){
	    synchronized(RabbitMQConnectionFactory.class){
		if(shutdown){
		    try {
			logger.debug("Create Connection Factory");
			connectionFactory = new ConnectionFactory();
			connectionFactory.setUsername(user);
			connectionFactory.setPassword(password);
			connectionFactory.setVirtualHost(vhost);
			connectionFactory.setHost(host);
			connectionFactory.setPort(Integer.valueOf(port));
			if(useSsl){
			    connectionFactory.useSslProtocol();
			}

			logger.debug("Try to acquire a connection ...");
			connection = connectionFactory.newConnection();
			channel = connection.createChannel();
			channel.addShutdownListener(this);

			logger.debug("Topic creation '{}'...", exchangeName);
			channel.exchangeDeclare(exchangeName, exchange);
			shutdown = false;
		    } catch (Exception e) {
			String msg = "Unable to configure RabbitMQBroadcaster";
			logger.error(msg, e);
			throw new RuntimeException(msg, e);
		    }
		}
	    }
	}
    }

    public final static RabbitMQConnectionFactory getFactory(AtmosphereConfig config) {
        // No need to synchronize here as the first Broadcaster created is at startup.
        if (factory == null) {
            factory = new RabbitMQConnectionFactory(config);
        }
        return factory;
    }

    public String exchangeName() {
        return exchangeName;
    }

    public Channel channel() {
	if(shutdown){
	    reInit();
	}
        return channel;
    }

    @Override
    public void shutdown() {
        try {
            channel.close();
            connection.close();
        } catch (IOException | TimeoutException e) {
            logger.trace("", e);
        }
    }

    @Override
    public void shutdownCompleted(ShutdownSignalException cause) {
	logger.info("Recieved shutdownCompleted", cause);
	shutdown = true;
    }
}
