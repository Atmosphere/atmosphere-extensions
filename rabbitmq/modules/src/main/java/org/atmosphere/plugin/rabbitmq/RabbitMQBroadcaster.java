/*
 * Copyright 2013 Jean-Francois Arcand
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

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.MessageProperties;
import org.atmosphere.cpr.AtmosphereConfig;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.BroadcasterFuture;
import org.atmosphere.cpr.Entry;
import org.atmosphere.util.SimpleBroadcaster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.Future;

/**
 * Simple {@link org.atmosphere.cpr.Broadcaster} implementation based on RabbitMQ
 *
 * @author Thibault Normand
 * @author Jean-Francois Arcand
 */
public class RabbitMQBroadcaster extends SimpleBroadcaster {

    private static final Logger logger = LoggerFactory.getLogger(RabbitMQBroadcaster.class);

    public static final String PARAM_HOST = RabbitMQBroadcaster.class.getName() + ".host";
    public static final String PARAM_USER = RabbitMQBroadcaster.class.getName() + ".user";
    public static final String PARAM_PASS = RabbitMQBroadcaster.class.getName() + ".password";
    public static final String PARAM_EXCHANGE_TYPE = RabbitMQBroadcaster.class.getName() + ".exchange";
    public static final String PARAM_VHOST = RabbitMQBroadcaster.class.getName() + ".vhost";
    public static final String PARAM_PORT = RabbitMQBroadcaster.class.getName() + ".port";

    private final String exchangeName;
    private final ConnectionFactory connectionFactory;
    private final Connection connection;
    private final Channel channel;
    private final String exchange;

    private String queueName;
    private String consumerTag;

    public RabbitMQBroadcaster(String id, AtmosphereConfig config) {
        super(id, config);

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

        exchangeName = "atmosphere." + exchange + "." + id;
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
    }

    @Override
    public void setID(String id) {
        super.setID(id);
        restartConsumer();
    }

    @Override
    public String getID() {
        String id = super.getID();
        if (id.startsWith("/*")) {
            id = "atmosphere";
        }
        return id;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Future<Object> broadcast(Object msg) {

        if (destroyed.get()) {
            logger.warn("This Broadcaster has been destroyed and cannot be used");
            return futureDone(msg);
        }

        start();

        Object newMsg = filter(msg);
        if (newMsg == null) return null;
        BroadcasterFuture<Object> f = new BroadcasterFuture<Object>(newMsg, this);
        entryDone(f);

        outgoingBroadcast(msg);
        return f;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Future<Object> broadcast(Object msg, AtmosphereResource r) {

        if (destroyed.get()) {
            logger.warn("This Broadcaster has been destroyed and cannot be used");
            return futureDone(msg);
        }

        start();

        Object newMsg = filter(msg);
        if (newMsg == null) return null;
        BroadcasterFuture<Object> f = new BroadcasterFuture<Object>(newMsg, this);
        entryDone(f);

        outgoingBroadcast(msg);
        return f;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Future<Object> broadcast(Object msg, Set<AtmosphereResource> subset) {

        if (destroyed.get()) {
            logger.warn("This Broadcaster has been destroyed and cannot be used");
            return futureDone(msg);
        }

        start();

        Object newMsg = filter(msg);
        if (newMsg == null) return null;

        BroadcasterFuture<Object> f = new BroadcasterFuture<Object>(newMsg, this);
        entryDone(f);
        outgoingBroadcast(msg);
        return f;
    }

    public void outgoingBroadcast(Object message) {
        try {
            String id = getID();
            if (message instanceof String) {
                logger.debug("Outgoing broadcast : {}", message);

                channel.basicPublish(exchangeName, id,
                        MessageProperties.PERSISTENT_TEXT_PLAIN, message.toString().getBytes());
            } else {
                throw new IOException("Message is not a string, so could not be handled !");
            }

        } catch (IOException e) {
            logger.warn("Failed to send message over RabbitMQ", e);
        }
    }

    void restartConsumer() {
        try {
            String id = getID();

            if (consumerTag != null) {
                logger.debug("Delete consumer {}", consumerTag);
                channel.basicCancel(consumerTag);
                consumerTag = null;
            }

            if (queueName != null) {
                logger.debug("Delete queue {}", queueName);
                channel.queueUnbind(queueName, exchangeName, id);
                channel.queueDelete(queueName);
                queueName = null;
            }

            queueName = channel.queueDeclare().getQueue();
            channel.queueBind(queueName, exchangeName, id);

            logger.info("Create AMQP consumer on queue {}, for routing key {}", queueName, id);

            DefaultConsumer queueConsumer = new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag,
                                           Envelope envelope,
                                           AMQP.BasicProperties properties,
                                           byte[] body)
                        throws IOException {
                    String message = new String(body);
                    try {
                        Object newMsg = filter(message);
                        // if newSgw == null, that means the message has been filtered.
                        if (newMsg != null) {
                            push(new Entry(newMsg, new BroadcasterFuture<Object>(newMsg, RabbitMQBroadcaster.this), message));
                        }
                    } catch (Throwable t) {
                        logger.error("failed to push message: " + message, t);
                    }
                }
            };

            consumerTag = channel.basicConsume(queueName, true, queueConsumer);
            logger.info("Consumer " + consumerTag + " for queue {}, on routing key {}", queueName, id);

        } catch (Throwable ex) {
            String msg = "Unable to initialize RabbitMQBroadcaster";
            logger.error(msg, ex);
            throw new IllegalStateException(msg, ex);
        }
    }

    /**
     * TODO: Crazy RabbitMQ client lock out on close.
     */
    @Override
    public synchronized void releaseExternalResources() {
        try {
            if (channel != null && channel.isOpen()) {
                if (consumerTag != null) {
                    channel.basicCancel(consumerTag);
                }
                channel.close();
            }
            connection.close();
        } catch (Exception ex) {
            logger.trace("", ex);
        }


    }

}