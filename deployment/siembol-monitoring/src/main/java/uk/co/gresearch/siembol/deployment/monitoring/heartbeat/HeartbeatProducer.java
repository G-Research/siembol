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

import java.io.Closeable;
import java.lang.invoke.MethodHandles;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

public class HeartbeatProducer implements Closeable {
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final HeartbeatMessage message = new HeartbeatMessage();
    private static final ObjectWriter objectWriter = new ObjectMapper().writer();
    private final AtomicReference<Exception> exception = new AtomicReference<>();
    private static final String MISSING_KAFKA_WRITER_PROPS_MSG = "Missing heartbeat kafka producer properties for %s";
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
                        new StringSerializer()) );
    }

    HeartbeatProducer(HeartbeatProducerProperties producerProperties,
                      String producerName,
                      Map<String, Object> heartbeatMessageProperties,
                      SiembolMetricsRegistrar metricsRegistrar,
                      Function<HeartbeatProducerProperties, Producer<String, String>> factory) {
        this.producerName = producerName;
        this.initialiseMessage(heartbeatMessageProperties);
        if (producerProperties == null) {
            throw new IllegalArgumentException(String.format(MISSING_KAFKA_WRITER_PROPS_MSG,
                    producerName));
        }
        producer = factory.apply(producerProperties);
        topicName = producerProperties.getOutputTopic();
        updateCounter =
                metricsRegistrar.registerCounter(SiembolMetrics.HEARTBEAT_MESSAGES_SENT.getMetricName(producerName));
        errorCounter =
                metricsRegistrar.registerCounter(SiembolMetrics.HEARTBEAT_PRODUCER_ERROR.getMetricName(producerName));

    }

    private void initialiseMessage(Map<String, Object> messageProperties) {
        if (messageProperties != null) {
            for (Map.Entry<String, Object> messageEntry : messageProperties.entrySet()) {
                this.message.setMessage(messageEntry.getKey(), messageEntry.getValue());
            }
        }
    }

    public void sendHeartbeat() {
        var now = Instant.now().truncatedTo(ChronoUnit.MILLIS);
        this.message.setEventTime(now.toString());
        this.message.setProducerName(producerName);
        try {
            producer.send(new ProducerRecord<>(topicName, objectWriter.writeValueAsString(this.message))).get();
            updateCounter.increment();
            LOG.info("Sent heartbeat with producer {} to topic {}", producerName, topicName);
            exception.set(null);
        } catch (Exception e) {
            LOG.error("Error sending message to kafka with producer {}: {}", producerName, e.toString());
            errorCounter.increment();
            exception.set(e);
        }
    }

    public void close() {
        producer.close();
    }

    public Health checkHealth() {
        return exception.get() == null ? Health.down().withException(exception.get()).build(): Health.up().build();
    }
}
