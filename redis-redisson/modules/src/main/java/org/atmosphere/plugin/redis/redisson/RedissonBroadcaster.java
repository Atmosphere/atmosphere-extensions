/*
 * Copyright 2008-2021 Async-IO.org
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
import org.atmosphere.cpr.Broadcaster;
import org.atmosphere.util.AbstractBroadcasterProxy;

import java.net.URI;

/**
 * Simple {@link org.atmosphere.cpr.Broadcaster} implementation based on Redisson
 *
 * @author Michael Gerlyand
 */
public class RedissonBroadcaster extends AbstractBroadcasterProxy {

    private RedissonUtil redisUtil;

    public RedissonBroadcaster() {
    }

    public Broadcaster initialize(String id, AtmosphereConfig config) {
        return initialize(id, URI.create("http://localhost:6379"), config);
    }

    public Broadcaster initialize(String id, URI uri, AtmosphereConfig config) {
        // Ignore default.
        uri = URI.create("http://localhost:6379");
        super.initialize(id, uri, config);
        this.redisUtil = new RedissonUtil(uri, config, new RedissonUtil.Callback() {
            @Override
            public String getID() {
                return RedissonBroadcaster.this.getID();
            }

            @Override
            public void broadcastReceivedMessage(String message) {
                RedissonBroadcaster.this.broadcastReceivedMessage(message);
            }
        });
        setUp();
        return this;
    }

    public synchronized void setUp() {
        redisUtil.configure();
    }

    @Override
    public synchronized void setID(String id) {
        super.setID(id);
        setUp();
        reconfigure();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void destroy() {
        super.destroy();
        redisUtil.destroy();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void incomingBroadcast() {
        redisUtil.incomingBroadcast();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void outgoingBroadcast(Object message) {
        redisUtil.outgoingBroadcast(message);
    }
}
