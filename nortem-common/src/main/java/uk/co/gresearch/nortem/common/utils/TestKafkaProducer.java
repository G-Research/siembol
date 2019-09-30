package uk.co.gresearch.nortem.common.utils;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.security.auth.SecurityProtocol;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.Properties;

public class TestKafkaProducer implements Closeable {
    private static final Logger LOG =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final Producer<String, String>  producer;
    private final String topic;

    public TestKafkaProducer(String bootstrapServers, String topic) throws IOException {
        producer = createTestKafkaProducer(bootstrapServers);
        this.topic = topic;
    }

    public void sendMessage(String message) {
        sendMessage(String.valueOf(message.hashCode()), message);
    }

    public void sendMessage(String key, String message) {
        LOG.debug("Sending message {}\n to to the input topic", message);
        producer.send(new ProducerRecord<>(topic, key, message));
    }

    @Override
    public void close() throws IOException {
        producer.close();
    }

    private static Producer<String, String> createTestKafkaProducer(String bootstrapServers) {
        Properties props = new Properties();
        props.put(ProducerConfig.CLIENT_ID_CONFIG, "test-producer");
        props.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(AdminClientConfig.SECURITY_PROTOCOL_CONFIG, SecurityProtocol.PLAINTEXT.toString());
        return new KafkaProducer<>(props, new StringSerializer(), new StringSerializer());
    }
}
