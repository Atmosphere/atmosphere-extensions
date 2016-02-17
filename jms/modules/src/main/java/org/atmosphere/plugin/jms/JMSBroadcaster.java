/*
 * Copyright 2014 Jean-Francois Arcand
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
package org.atmosphere.plugin.jms;

import org.atmosphere.cpr.AtmosphereConfig;
import org.atmosphere.cpr.Broadcaster;
import org.atmosphere.util.AbstractBroadcasterProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.naming.Context;
import javax.naming.InitialContext;
import java.net.URI;

/**
 * Simple {@link org.atmosphere.cpr.Broadcaster} implementation based on JMS
 * <p/>
 * The {@link ConnectionFactory} name's is jms/atmosphereFactory The
 * {@link Topic} by constructing "BroadcasterId =
 * {@link org.atmosphere.cpr.Broadcaster#getID}
 *
 * @author Jeanfrancois Arcand
 */
public class JMSBroadcaster extends AbstractBroadcasterProxy {
    private static final String JMS_TOPIC = JMSBroadcaster.class.getName() + ".topic";
    private static final String JNDI_NAMESPACE = JMSBroadcaster.class.getName() + ".JNDINamespace";
    private static final String JNDI_FACTORY_NAME = JMSBroadcaster.class.getName() + ".JNDIConnectionFactoryName";
    private static final String JNDI_TOPIC = JMSBroadcaster.class.getName() + ".JNDITopic";
    private static final Logger logger = LoggerFactory.getLogger(JMSBroadcaster.class);

    private Connection connection;
    private Session consumerSession;
    private Session publisherSession;
    private Topic topic;
    private MessageConsumer consumer;
    private MessageProducer publisher;

    private String topicId = "atmosphere";
    private String factoryName = "atmosphereFactory";
    private String namespace = "jms/";

    public  JMSBroadcaster() {}

    public Broadcaster initialize(String id, URI uri, AtmosphereConfig config) {
        super.initialize(id, uri, config);
        setUp();
        startOrRestartConsumer();
        return this;
    }

    private void setUp() {
        try {
            // For backward compatibility.
            if (config.getInitParameter(JMS_TOPIC) != null) {
                topicId = config.getInitParameter(JMS_TOPIC);
            }

            if (config.getInitParameter(JNDI_NAMESPACE) != null) {
                namespace = config.getInitParameter(JNDI_NAMESPACE);
            }

            if (config.getInitParameter(JNDI_FACTORY_NAME) != null) {
                factoryName = config.getInitParameter(JNDI_FACTORY_NAME);
            }

            if (config.getInitParameter(JNDI_TOPIC) != null) {
                topicId = config.getInitParameter(JNDI_TOPIC);
            }

            logger.info("Looking up Connection Factory {}", namespace + factoryName);
            Context ctx = new InitialContext();
            ConnectionFactory connectionFactory = (ConnectionFactory) ctx.lookup(namespace + factoryName);

            logger.info("Looking up topic: {}", topicId);
            topic = (Topic) ctx.lookup(namespace + topicId);

            connection = connectionFactory.createConnection();
            consumerSession = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            publisherSession = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            publisher = publisherSession.createProducer(topic);
            connection.start();
            logger.info("JMS created for topic {}", topicId);
            // Unfortunately we need the getID() to complete the configuration
            // But setID() is called after configure(), therefore we do the
            // rest of the configuration in incomingBroadcast() (which is called
            // once during configuration). We cannot do all the configuration in
            // incomingBroadcast() though, as using bc.getAtmosphereConfig() would
            // introduce a race condition (the configuration is loaded in a different
            // thread).
        } catch (Exception e) {
            String msg = "Unable to configure JMSBroadcaster";
            logger.error(msg, e);
            throw new RuntimeException(msg, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void incomingBroadcast() {
        // we setup the consumer in the setID method. No need to do it here too
    }

    @Override
    public void setID(String id) {
        super.setID(id);
        startOrRestartConsumer();
    }

    void startOrRestartConsumer() {
        try {
            String id = getID();
            if (id.startsWith("/*")) {
                id = "atmosphere";
            }
            if (consumer != null) {
                consumer.close();
                consumer = null;
            }

            logger.info("Create JMS consumer: {}", id);
            String selector = String.format("BroadcasterId = '%s'", id);

            consumer = consumerSession.createConsumer(topic, selector);
            consumer.setMessageListener(new MessageListener() {
                @Override
                public void onMessage(Message msg) {
                    try {
                        TextMessage textMessage = (TextMessage) msg;
                        String message = textMessage.getText();

                        if (message != null && bc != null) {
                            broadcastReceivedMessage(message);
                        }
                    } catch (JMSException ex) {
                        logger.warn("Failed to broadcast message", ex);
                    }
                }
            });
            logger.info("Consumer created for topic {}, with filter {}",
                    topicId, selector);
        } catch (Throwable ex) {
            String msg = "Unable to initialize JMSBroadcaster";
            logger.error(msg, ex);
            throw new IllegalStateException(msg, ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void outgoingBroadcast(Object message) {
        try {
            String id = getID();
            if (id.startsWith("/*")) {
                id = "atmosphere";
            }

            if (publisherSession == null) {
                throw new IllegalStateException("JMS Session is null");
            }

            TextMessage textMessage = publisherSession.createTextMessage(message
                    .toString());
            textMessage.setStringProperty("BroadcasterId", id);
            publisher.send(textMessage);
        } catch (JMSException ex) {
            logger.warn("Failed to send message over JMS", ex);
        }
    }

    /**
     * Close all related JMS factory, connection, etc.
     */
    @Override
    public synchronized void releaseExternalResources() {
        try {
            connection.close();
            consumerSession.close();
            publisherSession.close();
            if (consumer != null) consumer.close();
            if (publisher != null) publisher.close();
        } catch (Throwable ex) {
            logger.warn("releaseExternalResources", ex);
        }
    }
}
