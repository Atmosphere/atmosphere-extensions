package com.jupiter.vetspace.server.atmosphere;

import org.atmosphere.cpr.AtmosphereConfig;
import org.atmosphere.cpr.Broadcaster;
import org.atmosphere.util.AbstractBroadcasterProxy;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.concurrent.CountDownLatch;

/**
 * Simple {@link org.atmosphere.cpr.Broadcaster} implementation based on JGroups
 *
 * @author Jeanfrancois Arcand
 */
public class JGroupsBroadcasterTCP extends AbstractBroadcasterProxy {

    private static final Logger logger = LoggerFactory.getLogger(JGroupsBroadcasterTCP.class);

    private JChannel jchannel;
    private final CountDownLatch ready = new CountDownLatch(1);

    public JGroupsBroadcasterTCP(){}

    public Broadcaster initialize(String id, AtmosphereConfig config) {
        return super.initialize(id, null, config);
    }

    @Override
    public void incomingBroadcast() {
        try {
            logger.info("Starting Atmosphere JGroups Clustering support with group name {}", getID());

            jchannel = new JChannel("JGroupsFilterTCP.xml");
            jchannel.setReceiver(new ReceiverAdapter() {
                /** {@inheritDoc} */
                @Override
                public void receive(final Message message) {
                    final Object msg = message.getObject();
                    if (msg != null && BroadcastMessage.class.isAssignableFrom(msg.getClass())) {
                        BroadcastMessage b = BroadcastMessage.class.cast(msg);
                        if (b.getTopicId().equalsIgnoreCase(getID())) {
                            broadcastReceivedMessage(b.getMessage());
                        }
                    }
                }
            });
            jchannel.connect(getID());
        } catch (Throwable t) {
            logger.warn("failed to connect to JGroups channel", t);
        } finally {
            ready.countDown();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void outgoingBroadcast(Object message) {

        try {
            ready.await();
            jchannel.send(new Message(null, null, new BroadcastMessage(getID(), message)));
        } catch (Throwable e) {
            logger.error("failed to send messge over Jgroups channel", e.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void destroy() {
        super.destroy();
        if (!jchannel.isOpen()) return;
        try {
            Util.shutdown(jchannel);
        } catch (Throwable t) {
            Util.close(jchannel);
            logger.warn("failed to properly shutdown jgroups channel, closing abnormally", t);
        }
    }

    public static class BroadcastMessage implements Serializable {

        private final String topicId;
        private final Object message;

        public BroadcastMessage(String topicId, Object message) {
            this.topicId = topicId;
            this.message = message;
        }

        public String getTopicId() {
            return topicId;
        }

        public Object getMessage() {
            return message;
        }

    }
}