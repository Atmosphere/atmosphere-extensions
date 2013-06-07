/*
 * Copyright 2013 Jeanfrancois Arcand
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
import org.atmosphere.cpr.AtmosphereConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * RabbitMQ Connection Factory.
 *
 * @author Thibault Normand
 * @author Jean-Francois Arcand
 */
public class RabbitMQConnectionFactory implements AtmosphereConfig.ShutdownHook{
    private static final Logger logger = LoggerFactory.getLogger(RabbitMQBroadcaster.class);

    private static RabbitMQConnectionFactory factory;

    public static final String PARAM_HOST = RabbitMQBroadcaster.class.getName() + ".host";
    public static final String PARAM_USER = RabbitMQBroadcaster.class.getName() + ".user";
    public static final String PARAM_PASS = RabbitMQBroadcaster.class.getName() + ".password";
    public static final String PARAM_EXCHANGE_TYPE = RabbitMQBroadcaster.class.getName() + ".exchange";
    public static final String PARAM_VHOST = RabbitMQBroadcaster.class.getName() + ".vhost";
    public static final String PARAM_PORT = RabbitMQBroadcaster.class.getName() + ".port";

    private String exchangeName;
    private ConnectionFactory connectionFactory;
    private Connection connection;
    private Channel channel;
    private String exchange;

    public RabbitMQConnectionFactory(AtmosphereConfig config) {

        String s = config.getInitParameter(PARAM_EXCHANGE_TYPE);
        if (s != null) {
            exchange = s;
        } else {
            exchange = "fanout";
        }

        String host = config.getInitParameter(PARAM_HOST);
        if (host == null) {
            host = "127.0.0.1";
        }

        String vhost = config.getInitParameter(PARAM_VHOST);
        if (vhost == null) {
            vhost = "/";
        }

        String user = config.getInitParameter(PARAM_USER);
        if (user == null) {
            user = "guest";
        }

        String port = config.getInitParameter(PARAM_PORT);
        if (port == null) {
            port = "5672";
        }

        String password = config.getInitParameter(PARAM_PASS);
        if (password == null) {
            password = "guest";
        }

        exchangeName = "atmosphere." + exchange;
        try {
            logger.debug("Create Connection Factory");
            connectionFactory = new ConnectionFactory();
            connectionFactory.setUsername(user);
            connectionFactory.setPassword(password);
            connectionFactory.setVirtualHost(vhost);
            connectionFactory.setHost(host);
            connectionFactory.setPort(Integer.valueOf(port));

            logger.debug("Try to acquire a connection ...");
            connection = connectionFactory.newConnection();
            channel = connection.createChannel();

            logger.debug("Topic creation '{}'...", exchangeName);
            channel.exchangeDeclare(exchangeName, exchange);
        } catch (Exception e) {
            String msg = "Unable to configure RabbitMQBroadcaster";
            logger.error(msg, e);
            throw new RuntimeException(msg, e);
        }
        config.shutdownHook(this);
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
        return channel;
    }

    @Override
    public void shutdown() {
        try {
            channel.close();
            connection.close();
        } catch (IOException e) {
            logger.trace("", e);
        }
    }
}
