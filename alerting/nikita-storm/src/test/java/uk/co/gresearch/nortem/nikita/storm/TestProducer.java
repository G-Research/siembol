package uk.co.gresearch.nortem.nikita.storm;

import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.Properties;

import static org.apache.kafka.clients.CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG;
import static org.apache.kafka.clients.admin.AdminClientConfig.SECURITY_PROTOCOL_CONFIG;

public class TestProducer implements Closeable {
    private static final Logger LOG =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final Producer<String, String>  producer;
    private final String topic;

    public TestProducer(NikitaStormAttributes attributes) throws IOException {
        producer = createTestKafkaProducer(attributes);
        topic = attributes.getNikitaInputTopic();
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

    private static Producer<String, String> createTestKafkaProducer(NikitaStormAttributes attributes) {
        Properties props = new Properties();
        props.put(ProducerConfig.CLIENT_ID_CONFIG, "test-producer");
        props.put(BOOTSTRAP_SERVERS_CONFIG, attributes.getBootstrapServers());
        props.put(SECURITY_PROTOCOL_CONFIG, attributes.getSecurityProtocol());
        return new KafkaProducer<>(props, new StringSerializer(), new StringSerializer());
    }
}
