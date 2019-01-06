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
package org.atmosphere.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.atmosphere.cpr.AtmosphereConfig;
import org.atmosphere.cpr.Broadcaster;
import org.atmosphere.util.AbstractBroadcasterProxy;
import org.atmosphere.util.ExecutorsFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Kafka Support via a {@link Broadcaster}
 *
 * @author Jeanfrancois Arcand.
 */
public class KafkaBroadcaster extends AbstractBroadcasterProxy {

    private final Logger logger = LoggerFactory.getLogger(KafkaBroadcaster.class);

    public final static String PROPERTIES_FILE = "org.atmosphere.kafka.propertiesFile";
    private String topic;

    // using kafka 0.9+ API
    private KafkaProducer producer;
    private KafkaConsumer consumer;
    private final Serializer stringSerializer = new StringSerializer();
    private final Deserializer stringDeserializer = new StringDeserializer();
    private final AtomicBoolean closed = new AtomicBoolean(false);

    @Override
    public Broadcaster initialize(String id, URI uri, final AtmosphereConfig config) {
        super.initialize(id, uri, config);

        topic = id.equals(ROOT_MASTER) ? "atmosphere" : id.replaceAll("[^a-zA-Z0-9\\s]", "");
        // We are thread-safe
        producer = (KafkaProducer) config.properties().get("producer");
        Set<String> topics = (Set<String>) config.properties().get("topics");
        if (topics == null) {
            topics = new HashSet<String>();
            config.properties().put("topics", topics);
        }
        // create a new producer and consumer when the topic changes
        if (topics.isEmpty() || !topics.contains(topic)) {
            String load = config.getInitParameter(PROPERTIES_FILE, null);
            Properties props = new Properties();
            // let each consumer use its own group by default so that each can receive messages
            UUID uuid = UUID.randomUUID();
            String defaultGroupId = "atmosphere-consumer-" + Long.toHexString(uuid.getMostSignificantBits() ^ uuid.getLeastSignificantBits());
            props.put("group.id", defaultGroupId);
            props.put("bootstrap.servers", "127.0.0.1:9092");
            props.put("enable.auto.commit", "true");
            props.put("auto.commit.interval.ms", "1000");
            if (load != null) {
                try {
                    props.load(config.getServletContext().getResourceAsStream(load));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            if (topics.isEmpty()) {
                // producer can be reused, so it is instantiated only once
                producer = new KafkaProducer<String, String>(props, stringSerializer, stringSerializer);
                config.properties().put("producer", producer);
            }
            // consumer needs to be created for each topic subscription
            consumer = new KafkaConsumer<String, String>(props, stringDeserializer, stringDeserializer);
            topics.add(topic);

            startConsumer();
        }



        return this;
    }

    @Override
    public synchronized void destroy() {
        closed.set(true);
        super.destroy();
    }

    void startConsumer() {

        consumer.subscribe(Arrays.asList(topic));
        ExecutorsFactory.getMessageDispatcher(config, "kafka").execute(new Runnable() {
            @Override
            public void run() {
                while (!closed.get()) {
                    ConsumerRecords<String, String> records = consumer.poll(1000);
                    for (ConsumerRecord<String, String> record : records) {
                        broadcastReceivedMessage(record.value());
                    }
                }
                consumer.close();
                ((Set<String>)config.properties().get("topics")).remove(topic);
            }
        });
    }

    @Override
    public void incomingBroadcast() {
    }

    @Override
    public void outgoingBroadcast(Object message) {
        logger.trace("{} outgoingBroadcast {}", topic, message);

        // TODO: Prevent message round trip.

        producer.send(new ProducerRecord<String, String>(topic, message.toString()));
    }
}
