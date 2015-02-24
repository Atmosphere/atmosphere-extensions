package org.atmosphere.plugin.hazelcast;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.Message;
import com.hazelcast.core.MessageListener;
import org.atmosphere.cpr.Action;
import org.atmosphere.cpr.AtmosphereConfig;
import org.atmosphere.cpr.AtmosphereInterceptorAdapter;
import org.atmosphere.cpr.AtmosphereResource;

public class HazelcastInterceptor extends AtmosphereInterceptorAdapter {

    private static HazelcastInstance HAZELCAST_INSTANCE;

    @Override
    public void configure(AtmosphereConfig config) {
        // this can be added to properties file or some place to be configured
        System.setProperty("hazelcast.event.queue.capacity", "10000000000"); 
        System.setProperty("hazelcast.event.thread.count", "5");

        HAZELCAST_INSTANCE = Hazelcast.newHazelcastInstance();
        HAZELCAST_INSTANCE.getTopic("distributeMessages").addMessageListener(new MessageListener<Object>() {
            @Override
            public void onMessage(Message<Object> inMessage) {
                if (!inMessage.getPublishingMember().localMember()) {
                    if (inMessage.getMessageObject() instanceof TopicMessage) {
                        TopicMessage topicMessage = (TopicMessage) inMessage.getMessageObject();
                        String message = topicMessage.getMessage();
                        /// Do code as you would normaly here

                    }
                }
            }
        });
        super.configure(config);
    }

    @Override
    public Action inspect(AtmosphereResource resource) {
        if (!resource.getRequest().body().isEmpty()) {
            HAZELCAST_INSTANCE.getTopic("distributeMessages").publish(new TopicMessage(resource.getRequest().headersMap(), resource.getBroadcaster().getID(), resource.getRequest().body().asString()));
        }
        return Action.CONTINUE;
    }
}
