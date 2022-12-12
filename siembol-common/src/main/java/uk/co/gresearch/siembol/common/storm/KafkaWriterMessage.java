package uk.co.gresearch.siembol.common.storm;

import org.apache.kafka.clients.producer.ProducerRecord;

import java.io.Serializable;
/**
 * An object for representing a Kafka message
 *
 * <p>This class represents a Kafka message. It includes a topic, a message key and a message value.
 * It implements Serializable interface for integration in Storm.
 *
 * @author  Marian Novotny
 */
public class KafkaWriterMessage implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String topic;
    private final String message;
    private final String key;

    public KafkaWriterMessage(String topic, String key, String message) {
        this.topic = topic;
        this.message = message;
        this.key = key;
    }

    public KafkaWriterMessage(String topic, String message) {
        this(topic, null, message);
    }

    public String getTopic() {
        return topic;
    }

    public String getMessage() {
        return message;
    }

    public String getKey() {
        return key;
    }

    public ProducerRecord<String, String> getProducerRecord() {
        return key == null ? new ProducerRecord<>(topic, message) : new ProducerRecord<>(topic, key, message);
    }

}
