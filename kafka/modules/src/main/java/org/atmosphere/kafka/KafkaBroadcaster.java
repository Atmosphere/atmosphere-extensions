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
package org.atmosphere.kafka;

import kafka.consumer.Consumer;
import kafka.consumer.ConsumerConfig;
import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.atmosphere.config.managed.ManagedAtmosphereHandler;
import org.atmosphere.cpr.AtmosphereConfig;
import org.atmosphere.cpr.AtmosphereFramework;
import org.atmosphere.cpr.Broadcaster;
import org.atmosphere.util.AbstractBroadcasterProxy;
import org.atmosphere.util.ExecutorsFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Kafka Support via a {@link Broadcaster}
 *
 * @author Jeanfrancois Arcand.
 */
public class KafkaBroadcaster extends AbstractBroadcasterProxy {

    private final Logger logger = LoggerFactory.getLogger(KafkaBroadcaster.class);

    public final static String PROPERTIES_FILE = "org.atmosphere.kafka.propertiesFile";
    private String topic;

    private KafkaProducer producer;
    private ConsumerConnector consumer;
    private final Serializer stringSerializer = new StringSerializer();
    private Map<String, Integer> topicCountMap;
    private Map<String, List<KafkaStream<byte[], byte[]>>> consumerMap;

    @Override
    public Broadcaster initialize(String id, URI uri, final AtmosphereConfig config) {
        super.initialize(id, uri, config);

        topic = id.equals(ROOT_MASTER) ? "atmosphere" : id.replaceAll("[^a-zA-Z0-9\\s]", "");

        // We are thread-safe
        producer = (KafkaProducer) config.properties().get(KafkaProducer.class.getName());
        consumer = (ConsumerConnector) config.properties().get(ConsumerConnector.class.getName());
        topicCountMap = (Map<String, Integer>) config.properties().get("topicCountMap");

        if (producer == null) {
            String load = config.getInitParameter(PROPERTIES_FILE, null);
            Properties props = new Properties();
            if (load == null) {
                props.put("bootstrap.servers", "10.0.1.10:9092");
                props.put("zk.connect", "127.0.0.1:9092");
                props.put("group.id", "kafka.atmosphere");
                props.put("partition.assignment.strategy", "roundrobin");
                props.put("zookeeper.connect", "localhost:2181");
            } else {
                try {
                    props.load(config.getServletContext().getResourceAsStream(load));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            producer = new KafkaProducer(props, stringSerializer, stringSerializer);
            consumer = Consumer.createJavaConsumerConnector(new ConsumerConfig(props));
            topicCountMap = new HashMap<String, Integer>();

            config.properties().put("producer", producer);
            config.properties().put(ConsumerConnector.class.getName(), consumer);
            config.properties().put("topicCountMap", topicCountMap);
        }

        topicCountMap.put(topic, new Integer(1));

        config.startupHook(new AtmosphereConfig.StartupHook() {

            @Override
            public void started(AtmosphereFramework framework) {
                if (config.properties().get("started") != null) return;

                config.properties().put("started", "true");
                consumerMap = consumer.createMessageStreams(topicCountMap);
                for (final String t : topicCountMap.keySet()) {
                    ExecutorsFactory.getMessageDispatcher(config, "kafka").execute(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                KafkaStream<byte[], byte[]> stream = consumerMap.get(t).get(0);
                                ConsumerIterator<byte[], byte[]> it = stream.iterator();
                                String message;
                                while (it.hasNext()) {
                                    message = new String(it.next().message());

                                    logger.trace("{} incomingBroadcast {}", t, message);
                                    broadcastReceivedMessage(message);
                                }
                            } catch (Exception ex) {
                                if (InterruptedException.class.isAssignableFrom(ex.getClass())) {
                                    logger.trace("", ex);
                                } else {
                                    logger.warn("", ex);
                                }
                            }
                        }
                    });
                }
            }
        });

        return this;
    }

    @Override
    public void incomingBroadcast() {
    }

    @Override
    public void outgoingBroadcast(Object message) {
        logger.trace("{} outgoingBroadcast {}", topic, message);

        // TODO: Prevent message round trip.

        if (ManagedAtmosphereHandler.Managed.class.isAssignableFrom(message.getClass())) {
            message = ManagedAtmosphereHandler.Managed.class.cast(message).object();
        }

        producer.send(new ProducerRecord(topic, message));
    }
}
