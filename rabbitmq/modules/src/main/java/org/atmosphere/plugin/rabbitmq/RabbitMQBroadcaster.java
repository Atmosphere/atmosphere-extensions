/*
 * Copyright 2017 Jean-Francois Arcand
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
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.MessageProperties;
import com.rabbitmq.client.ShutdownListener;
import com.rabbitmq.client.ShutdownSignalException;

import org.atmosphere.cpr.AtmosphereConfig;
import org.atmosphere.cpr.Broadcaster;
import org.atmosphere.cpr.BroadcasterFuture;
import org.atmosphere.cpr.Deliver;
import org.atmosphere.util.SimpleBroadcaster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Simple {@link org.atmosphere.cpr.Broadcaster} implementation based on RabbitMQ
 *
 * @author Thibault Normand
 * @author Jean-Francois Arcand
 */
public class RabbitMQBroadcaster extends SimpleBroadcaster implements ShutdownListener {

    private static final Logger logger = LoggerFactory.getLogger(RabbitMQBroadcaster.class);

    private String queueName;
    private String consumerTag;
    private RabbitMQConnectionFactory factory;
    private Channel channel;
    private String exchangeName;

    public RabbitMQBroadcaster() {
    }

    @Override
    public Broadcaster initialize(String id, AtmosphereConfig config) {
        super.initialize(id, config);
        init(config);
        return this;
    }

    public void init(AtmosphereConfig config) {
        factory = RabbitMQConnectionFactory.getFactory(config);
        channel = factory.channel();
        channel.addShutdownListener(this);

        exchangeName = factory.exchangeName();
        restartConsumer();
    }

    @Override
    public Broadcaster initialize(String name, java.net.URI uri, AtmosphereConfig config) {
        super.initialize(name, uri, config);
        init(config);
        return this;
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

    @Override
    protected void push(Deliver entry) {
        if (destroyed.get()) {
            return;
        }

        outgoingBroadcast(entry.getMessage());
    }

    public void outgoingBroadcast(Object message) {
        try {
            String id = getID();
            logger.trace("Outgoing broadcast : {}", message);

            channel.basicPublish(exchangeName, id,
                    MessageProperties.PERSISTENT_TEXT_PLAIN, message.toString().getBytes());


        } catch (IOException e) {
            logger.warn("Failed to send message over RabbitMQ", e);
        }
    }

    void restartConsumer() {
        try {
            final String id = getID();

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

                    // Not for us.
                    if (!envelope.getRoutingKey().equalsIgnoreCase(id)) return;

                    String message = new String(body);
                    try {
                        Object newMsg = filter(message);
                        // if newSgw == null, that means the message has been filtered.
                        if (newMsg != null) {
                            deliverPush(new Deliver(newMsg, new BroadcasterFuture<Object>(newMsg), message), true);
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

    @Override
    public synchronized void releaseExternalResources() {
        try {
            if (channel != null && channel.isOpen()) {
                if (consumerTag != null) {
                    channel.basicCancel(consumerTag);
                }
            }
        } catch (Exception ex) {
            logger.trace("", ex);
        }
    }

    @Override
    public void shutdownCompleted(ShutdownSignalException cause) {
        this.destroy();
    }
}