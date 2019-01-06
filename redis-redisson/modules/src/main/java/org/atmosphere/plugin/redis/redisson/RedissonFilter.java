/*
 * Copyright 2008-2019 Async-IO.org
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
package org.atmosphere.plugin.redis.redisson;

import org.atmosphere.cpr.AtmosphereConfig;
import org.atmosphere.cpr.BroadcastFilter;
import org.atmosphere.cpr.Broadcaster;
import org.atmosphere.cpr.ClusterBroadcastFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Support for Redis
 *
 * @author Michael Gerlyand
 */
public class RedissonFilter implements ClusterBroadcastFilter {

    private static final Logger logger = LoggerFactory.getLogger(RedissonFilter.class);

    private Broadcaster bc;
    private final ExecutorService listener = Executors.newSingleThreadExecutor();
    private final ConcurrentLinkedQueue<String> receivedMessages = new ConcurrentLinkedQueue<String>();
    private URI uri;
    private RedissonUtil redisUtil;
    private AtmosphereConfig config;
    private final ConcurrentLinkedQueue<String> localMessages = new ConcurrentLinkedQueue<String>();
    private String auth;

    public RedissonFilter() {
        this(URI.create("http://localhost:6379"));
    }

    public RedissonFilter(URI uri) {
        this.uri = uri;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setUri(String address) {
        uri = URI.create(address);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(AtmosphereConfig config) {
        this.config = config;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void destroy() {
        listener.shutdownNow();
        redisUtil.destroy();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BroadcastFilter.BroadcastAction filter(String broadcasterId, Object originalMessage, Object o) {
        String contents = originalMessage.toString();
        if (!localMessages.remove(contents)) {
            redisUtil.outgoingBroadcast(originalMessage.toString());
            return new BroadcastFilter.BroadcastAction(BroadcastAction.ACTION.CONTINUE, o);
        } else {
            return new BroadcastFilter.BroadcastAction(BroadcastAction.ACTION.ABORT, o);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Broadcaster getBroadcaster() {
        return bc;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setBroadcaster(final Broadcaster bc) {
        this.bc = bc;

        this.redisUtil = new RedissonUtil(uri, config, new RedissonUtil.Callback() {
            @Override
            public String getID() {
                return bc.getID();
            }

            @Override
            public void broadcastReceivedMessage(String message) {
                localMessages.offer(message);
                bc.broadcast(message);
            }
        });
        redisUtil.configure();

        listener.submit(new Runnable() {
            public void run() {
                redisUtil.incomingBroadcast();
            }
        });
    }
}
