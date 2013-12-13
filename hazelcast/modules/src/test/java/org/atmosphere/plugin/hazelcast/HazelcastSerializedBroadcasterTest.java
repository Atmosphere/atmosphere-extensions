package org.atmosphere.plugin.hazelcast;


import com.hazelcast.core.ITopic;
import org.junit.Test;

import static org.mockito.Mockito.*;

/**
 * Created by rubyboy on 13/12/13.
 */
public class HazelcastSerializedBroadcasterTest {

    @Test
    public void test() {
        HazelcastSerializedBroadcaster hazelcastSerializedBroadcaster = spy(new HazelcastSerializedBroadcaster());
        ITopic topic = mock(ITopic.class);
        when(hazelcastSerializedBroadcaster.getTopic()).thenReturn(topic);
        Object message = new Object();
        hazelcastSerializedBroadcaster.outgoingBroadcast(message);
        verify(hazelcastSerializedBroadcaster,times(1)).getTopic();
        verify(topic,times(1)).publish(message);
    }

}
