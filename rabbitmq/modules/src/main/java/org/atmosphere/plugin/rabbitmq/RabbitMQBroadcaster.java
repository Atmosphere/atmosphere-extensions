package org.atmosphere.plugin.rabbitmq;

import com.rabbitmq.client.*;
import org.atmosphere.cpr.AtmosphereConfig;
import org.atmosphere.util.AbstractBroadcasterProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Simple {@link org.atmosphere.cpr.Broadcaster} implementation based on RabbitMQ
 *
 * @author Thibault Normand
 */
public class RabbitMQBroadcaster extends AbstractBroadcasterProxy {

    private static final Logger logger = LoggerFactory.getLogger(RabbitMQBroadcaster.class);

    public static final String PARAM_HOST = RabbitMQBroadcaster.class.getName() + ".host";
    public static final String PARAM_USER = RabbitMQBroadcaster.class.getName() + ".user";
    public static final String PARAM_PASS = RabbitMQBroadcaster.class.getName() + ".password";

    private String exchangeName = "atmosphere.topic";
    private ConnectionFactory connectionFactory;
    private Connection connection;
    private Channel channel;

    private String queueName;
    private String consumerTag;

    public RabbitMQBroadcaster(String id, AtmosphereConfig config) {
        super(id, null, config);
        setUp(id);
    }

    private void setUp(String id) {
        try {
            logger.info("Create Connection Factory");
            connectionFactory = new ConnectionFactory();

            logger.info("Try to acquire a connection ...");
            connection = connectionFactory.newConnection();
            channel = connection.createChannel();

            logger.info("Topic creation '{}'...", exchangeName);
            channel.exchangeDeclare(exchangeName, "topic");
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

    @Override
    public void incomingBroadcast() {
        logger.debug("Incoming broadcast");
    }

    @Override
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
                    try {
                        broadcastReceivedMessage(new String(body));
                    } catch (Exception ex) {
                        logger.warn("Failed to broadcast message", ex);
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
     * Close all related JMS factory, connection, etc.
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
        } catch (Throwable ex) {
            logger.warn("releaseExternalResources", ex);
        }
    }

}