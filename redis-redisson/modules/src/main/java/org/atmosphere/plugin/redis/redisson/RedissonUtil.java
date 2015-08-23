/*
 * Copyright 2015 Async-IO.org
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
import org.redisson.Config;
import org.redisson.Redisson;
import org.redisson.core.MessageListener;
import org.redisson.core.RTopic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

public class RedissonUtil {
    private static final Logger logger = LoggerFactory.getLogger(RedissonBroadcaster.class);

    private static final String REDIS_AUTH = RedissonBroadcaster.class.getName() + ".authorization";
    private static final String REDIS_SERVER = RedissonBroadcaster.class.getName() + ".server";

    private Redisson redisson;
    private String authToken = null;

    private final AtmosphereConfig config;
    private URI uri;
    private final Callback callback;

    public RedissonUtil(URI uri, AtmosphereConfig config, Callback callback) {
        this.config = config;
        this.callback = callback;
        this.uri = uri;
    }

    public String getAuth() {
        return authToken;
    }

    public void setAuth(String auth) {
        authToken = auth;
    }

    public void configure() {

        if (config.getServletConfig().getInitParameter(REDIS_AUTH) != null) {
            authToken = config.getServletConfig().getInitParameter(REDIS_AUTH);
        }

        if (config.getServletConfig().getInitParameter(REDIS_SERVER) != null) {
            uri = URI.create(config.getServletConfig().getInitParameter(REDIS_SERVER));
        } else if (uri == null) {
            throw new NullPointerException("uri cannot be null");
        }

        Config redissonConfig = new Config();
        redissonConfig.useSingleServer().setAddress(uri.getHost() + ":" + uri.getPort());
        redissonConfig.useSingleServer().setDatabase(1);
        try {
            redisson = Redisson.create(redissonConfig);
        } catch (Exception e) {
            logger.error("failed to connect redis", e);
            disconnectRedisson();
        }
    }

    public void disconnectRedisson() {
        redisson.shutdown();
    }

    public synchronized void setID(String id) {
        disconnectRedisson();
    }

    /**
     * {@inheritDoc}
     */
    public void destroy() {
        try {
            disconnectRedisson();
        } catch (Throwable t) {
            logger.warn("Redisson error on close", t);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void incomingBroadcast() {
        logger.info("Subscribing to: {}", callback.getID());

        RTopic<String> topic = redisson.getTopic(callback.getID());
        topic.addListener(new MessageListener<String>() {

            public void onMessage(String channel, String message) {
                callback.broadcastReceivedMessage(message);
            }
        });
    }

    public void outgoingBroadcast(Object message) {
        RTopic<String> topic = redisson.getTopic(callback.getID());
        try {
            topic.publish(message.toString());
        } catch (Exception e) {
            logger.warn("outgoingBroadcast exception", e);
        }
    }

    public static interface Callback {

        String getID();

        void broadcastReceivedMessage(String message);

    }
}
