package uk.co.gresearch.siembol.deployment.monitoring.heartbeat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import uk.co.gresearch.siembol.common.metrics.SiembolCounter;
import uk.co.gresearch.siembol.common.metrics.SiembolMetrics;
import uk.co.gresearch.siembol.common.metrics.SiembolMetricsRegistrar;
import uk.co.gresearch.siembol.deployment.monitoring.model.HeartbeatMessage;
import uk.co.gresearch.siembol.deployment.monitoring.model.HeartbeatProducerProperties;

import java.io.Closeable;
import java.lang.invoke.MethodHandles;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

public class HeartbeatProducer implements Closeable {
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final ObjectWriter OBJECT_WRITER = new ObjectMapper().writerFor(HeartbeatMessage.class);
    private static final String MISSING_KAFKA_WRITER_PROPS_MSG = "Missing heartbeat kafka producer properties for %s";
    private static final String MESSAGE_SENT_SUCCESSFULLY_MSG = "Sent heartbeat with producer {} to topic {}";
    private static final String MESSAGE_SENT_ERROR_MSG = "Error sending message to kafka with producer {}: {}";

    private final HeartbeatMessage message;
    private final AtomicReference<Exception> exception = new AtomicReference<>();
    private final String producerName;
    private final Producer<String, String> producer;
    private final SiembolCounter updateCounter;
    private final SiembolCounter errorCounter;
    private final String topicName;

    public HeartbeatProducer(HeartbeatProducerProperties producerProperties,
                             String producerName,
                             Map<String, Object> heartbeatMessageProperties,
                             SiembolMetricsRegistrar metricsRegistrar) {
        this(producerProperties, producerName, heartbeatMessageProperties, metricsRegistrar,
                x -> new KafkaProducer<>(x.getKafkaProperties(), new StringSerializer(),
                        new StringSerializer()));
    }

    HeartbeatProducer(HeartbeatProducerProperties producerProperties,
                      String producerName,
                      Map<String, Object> heartbeatMessageProperties,
                      SiembolMetricsRegistrar metricsRegistrar,
                      Function<HeartbeatProducerProperties, Producer<String, String>> factory) {
        if (producerProperties == null || producerName == null) {
            throw new IllegalArgumentException(String.format(MISSING_KAFKA_WRITER_PROPS_MSG, producerName));
        }

        message = new HeartbeatMessage();
        if (heartbeatMessageProperties != null) {
            for (var messageEntry : heartbeatMessageProperties.entrySet()) {
                this.message.setMessage(messageEntry.getKey(), messageEntry.getValue());
            }
        }

        this.producerName = producerName;
        producer = factory.apply(producerProperties);
        topicName = producerProperties.getOutputTopic();
        updateCounter =
                metricsRegistrar.registerCounter(SiembolMetrics.HEARTBEAT_MESSAGES_SENT.getMetricName(producerName));
        errorCounter =
                metricsRegistrar.registerCounter(SiembolMetrics.HEARTBEAT_PRODUCER_ERROR.getMetricName(producerName));

    }

    public void sendHeartbeat() {
        var now = Instant.now().truncatedTo(ChronoUnit.MILLIS);
        this.message.setEventTime(now.toString()); //NOTE: timestamp in ISO format
        this.message.setProducerName(producerName);
        try {
            producer.send(new ProducerRecord<>(topicName, OBJECT_WRITER.writeValueAsString(this.message))).get();
            updateCounter.increment();
            LOG.info(MESSAGE_SENT_SUCCESSFULLY_MSG, producerName, topicName);
            exception.set(null);
        } catch (Exception e) {
            LOG.error(MESSAGE_SENT_ERROR_MSG, producerName, e.toString());
            errorCounter.increment();
            exception.set(e);
        }
    }

    @Override
    public void close() {
        producer.close();
    }

    public Health checkHealth() {
        return exception.get() == null ? Health.up().build() : Health.down().withException(exception.get()).build();
    }
}
