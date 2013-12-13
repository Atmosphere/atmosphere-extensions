package org.atmosphere.plugin.hazelcast;

/**
 * Simple {@link org.atmosphere.cpr.Broadcaster} implementation based on Hazelcast for {@link java.io.Serializable} objects
 * This broadcaster will publish the object as is to the Hazelcast topic queue and rely on Hazelcast's capability to serialize objects
 *
 * @author Ruby Boyarski
 */
public class HazelcastSerializedBroadcaster extends HazelcastBroadcaster {

    /**
     * {@inheritDoc}
     */
    @Override
    public void outgoingBroadcast(Object message) {
        getTopic().publish(message);
    }
}
