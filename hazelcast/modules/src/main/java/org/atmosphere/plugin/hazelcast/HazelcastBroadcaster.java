/*
 * Copyright 2014 Jeanfrancois Arcand
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
package org.atmosphere.plugin.hazelcast;


import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ITopic;
import com.hazelcast.core.Message;
import com.hazelcast.core.MessageListener;
import org.atmosphere.cpr.AtmosphereConfig;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.Broadcaster;
import org.atmosphere.cpr.AtmosphereConfig.ShutdownHook;
import org.atmosphere.util.AbstractBroadcasterProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Simple {@link org.atmosphere.cpr.Broadcaster} implementation based on Hazelcast
 *
 * @author Jeanfrancois Arcand
 */
public class HazelcastBroadcaster extends AbstractBroadcasterProxy {

    private static final Logger logger = LoggerFactory.getLogger(org.atmosphere.plugin.hazelcast.HazelcastBroadcaster.class);
    private ITopic topic;
    private final AtomicBoolean isClosed = new AtomicBoolean();
    private String messageListenerRegistrationId;

    private final static HazelcastInstance HAZELCAST_INSTANCE = Hazelcast.newHazelcastInstance();

    public Broadcaster initialize(String id, AtmosphereConfig config) {
        return super.initialize(id, URI.create("http://localhost:6379"), config);
    }

    public Broadcaster initialize(String id, URI uri, AtmosphereConfig config) {
        return super.initialize(id, uri, config);
    }

    public void setUp() {
        topic = HAZELCAST_INSTANCE.<String>getTopic(getID());
        config.shutdownHook(new ShutdownHook() {
            @Override
            public void shutdown() {
                HAZELCAST_INSTANCE.shutdown();
                isClosed.set(true);
            }
        });
    }
    
    private synchronized void addMessageListener() {
      if (getAtmosphereResources().size() > 0 && messageListenerRegistrationId == null) {
        messageListenerRegistrationId = topic.addMessageListener(new MessageListener<String>() {
          @Override
          public void onMessage(Message<String> message) {
              broadcastReceivedMessage(message.getMessageObject());
          }
        });
        
        logger.info("Added message listener to topic");
      }
    }
    
    private synchronized void removeMessageListener() {
      if (getAtmosphereResources().size() == 0 && messageListenerRegistrationId != null) {
        getTopic().removeMessageListener(messageListenerRegistrationId);
        messageListenerRegistrationId = null;
        
        logger.info("Removed message listener from topic");
      }   
    }
    
    @Override
    public Broadcaster addAtmosphereResource(AtmosphereResource resource) {
      Broadcaster result = super.addAtmosphereResource(resource);
      addMessageListener();
      return result;
    }

    @Override
    public Broadcaster removeAtmosphereResource(AtmosphereResource resource) {
      Broadcaster result = super.removeAtmosphereResource(resource);
      removeMessageListener();
      return result;
    }

    @Override
    public synchronized void setID(String id) {
        super.setID(id);
        setUp();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void destroy() {
        if (!isClosed.get()) {
            topic.destroy();
            topic = null;
        }
        super.destroy();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void incomingBroadcast() {
        logger.info("Subscribing to: {}", getID());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void outgoingBroadcast(Object message) {
        topic.publish(message);
    }

    /**
     * Get the Hazelcast topic
     * @return topic
     */
    protected ITopic getTopic() {
        return topic;
    }


}
