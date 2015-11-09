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
import org.redisson.client.protocol.pubsub.Message;
import org.redisson.connection.RandomLoadBalancer;
import org.redisson.core.MessageListener;
import org.redisson.core.PatternMessageListener;
import org.redisson.core.RPatternTopic;
import org.redisson.core.RTopic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

public class RedissonUtil {
    private static final Logger logger = LoggerFactory.getLogger(RedissonBroadcaster.class);

    private static final String REDIS_AUTH = RedissonBroadcaster.class.getName() + ".authorization";
    private static final String REDIS_SERVER = RedissonBroadcaster.class.getName() + ".server";
    private static final String REDIS_OTHERS = RedissonBroadcaster.class.getName() + ".others";
    private static final String REDIS_TYPE = RedissonBroadcaster.class.getName() + ".type";
    private static final String REDIS_SCAN_INTERVAL = RedissonBroadcaster.class.getName() + ".scan.interval";
    private static final String REDIS_SENTINEL_MASTER_NAME = RedissonBroadcaster.class.getName() + ".master.name";

    private Redisson redisson;

    private final AtmosphereConfig config;
    private URI uri;
    private final Callback callback;

    public RedissonUtil(URI uri, AtmosphereConfig config, Callback callback) {
        this.config = config;
        this.callback = callback;
        this.uri = uri;
    }

    private enum RedisType {
        SINGLE("single"), MASTER("master"), CLUSTER("cluster"), SENTINEL("sentinel"), ELASTICACHE("elasticache");
        private String stringValue;

        RedisType(String s) {
            stringValue = s;
        }

        public String getStringValue() {
            return stringValue;
        }
    }

    public void configure() {
        String authToken = "";
        String redisType = "";

        if (config.getServletConfig().getInitParameter(REDIS_TYPE) != null) {
            redisType = config.getServletConfig().getInitParameter(REDIS_TYPE);
        }

        if (config.getServletConfig().getInitParameter(REDIS_AUTH) != null) {
            authToken = config.getServletConfig().getInitParameter(REDIS_AUTH);
        }

        if (config.getServletConfig().getInitParameter(REDIS_SERVER) != null) {
            uri = URI.create(config.getServletConfig().getInitParameter(REDIS_SERVER));
        } else if (uri == null) {
            throw new NullPointerException("uri cannot be null");
        }

        Config redissonConfig = new Config();

        if (redisType.isEmpty() || redisType.equals(RedisType.SINGLE.getStringValue())) {
            redissonConfig.useSingleServer().setAddress(uri.getHost() + ":" + uri.getPort());
            redissonConfig.useSingleServer().setDatabase(1);
            if (!authToken.isEmpty()) {
                redissonConfig.useSingleServer().setPassword(authToken);
            }
        } else {
            List<String> slaveList = Arrays.asList(config.getServletConfig().getInitParameter(REDIS_OTHERS).split("\\s*,\\s*"));
            Integer scanInterval = 2000;
            if (config.getServletConfig().getInitParameter(REDIS_SCAN_INTERVAL) != null) {
                scanInterval = Integer.parseInt(config.getServletConfig().getInitParameter(REDIS_SCAN_INTERVAL));
            }
            if (redisType.equals(RedisType.MASTER.getStringValue())) {
                redissonConfig.useMasterSlaveConnection()
                        .setMasterAddress(uri.getHost() + ":" + uri.getPort())
                        .setLoadBalancer(new RandomLoadBalancer());
                for (String slave : slaveList) {
                    URI serverAddress = URI.create(slave);
                    redissonConfig.useMasterSlaveConnection()
                            .addSlaveAddress(serverAddress.getHost() + ":" + serverAddress.getPort());
                }
                if (!authToken.isEmpty()) {
                    redissonConfig.useMasterSlaveConnection()
                            .setPassword(authToken);
                }
            } else if (redisType.equals(RedisType.CLUSTER.getStringValue())) {
                redissonConfig.useClusterServers()
                        .setScanInterval(scanInterval)
                        .addNodeAddress(uri.getHost() + ":" + uri.getPort());
                for (String slave : slaveList) {
                    URI serverAddress = URI.create(slave);
                    redissonConfig.useClusterServers()
                            .addNodeAddress(serverAddress.getHost() + ":" + serverAddress.getPort());
                }
                if (!authToken.isEmpty()) {
                    redissonConfig.useClusterServers()
                            .setPassword(authToken);
                }
            } else if (redisType.equals(RedisType.SENTINEL.getStringValue())) {
                String masterName = "";
                if (config.getServletConfig().getInitParameter(REDIS_SENTINEL_MASTER_NAME) != null) {
                    masterName = config.getServletConfig().getInitParameter(REDIS_SENTINEL_MASTER_NAME);
                } else if (masterName.isEmpty()) {
                    throw new NullPointerException("SENTINEL MASTER NAME cannot be null");
                }
                redissonConfig.useSentinelConnection()
                        .setMasterName(masterName)
                        .addSentinelAddress(uri.getHost() + ":" + uri.getPort());
                for (String slave : slaveList) {
                    URI serverAddress = URI.create(slave);
                    redissonConfig.useSentinelConnection()
                            .addSentinelAddress(serverAddress.getHost() + ":" + serverAddress.getPort());
                }
                if (!authToken.isEmpty()) {
                    redissonConfig.useSentinelConnection()
                            .setPassword(authToken);
                }
            } else if (redisType.equals(RedisType.ELASTICACHE.getStringValue())) {
                redissonConfig.useElasticacheServers()
                        .addNodeAddress(uri.getHost() + ":" + uri.getPort())
                        .setScanInterval(scanInterval);
                if (!authToken.isEmpty()) {
                    redissonConfig.useElasticacheServers()
                            .setPassword(authToken);
                }
            }
        }

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
        String callbackId = callback.getID();
        logger.info("Subscribing to: {}", callbackId);

        if (!callbackId.contains("*")) {
            RTopic<String> topic = redisson.getTopic(callbackId);
            topic.addListener(new MessageListener<String>() {

                public void onMessage(String channel, String message) {
                    callback.broadcastReceivedMessage(message);
                }
            });
        } else {
            RPatternTopic<Message> topic1 = redisson.getPatternTopic("topic1.*");
            topic1.addListener(new PatternMessageListener<Message>() {
                @Override
                public void onMessage(String pattern, String channel, Message msg) {
                    callback.broadcastReceivedMessage(msg.toString());
                }
            });
        }
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
