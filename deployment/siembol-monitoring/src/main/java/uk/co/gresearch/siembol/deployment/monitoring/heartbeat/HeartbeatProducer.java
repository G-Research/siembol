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
import reactor.core.publisher.Mono;
import uk.co.gresearch.siembol.common.metrics.SiembolCounter;
import uk.co.gresearch.siembol.common.metrics.SiembolMetrics;
import uk.co.gresearch.siembol.common.metrics.SiembolMetricsRegistrar;
import uk.co.gresearch.siembol.deployment.monitoring.application.HeartbeatProducerProperties;

import java.io.Closeable;
import java.lang.invoke.MethodHandles;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class HeartbeatProducer implements Closeable {
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final String MISSING_KAFKA_WRITER_PROPS_MSG = "Missing heartbeat kafka producer properties for {}";
    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    private final HeartbeatMessage message = new HeartbeatMessage();
    private static final ObjectWriter objectWriter = new ObjectMapper().writer();
    private final Map<String, Exception> exceptionMap = new HashMap<>();
    private final int errorThreshold;
    private final Map<String, Producer<String,String>> producerMap = new HashMap<>();

    public HeartbeatProducer(Map<String, HeartbeatProducerProperties> producerPropertiesMap,
                             int heartbeatIntervalSeconds,
                             Map<String, Object> heartbeatMessageProperties,
                             SiembolMetricsRegistrar metricsRegistrar) {
        this.initialiseMessage(heartbeatMessageProperties);
        this.errorThreshold = producerPropertiesMap.size();
        for (Map.Entry<String, HeartbeatProducerProperties> producerProperties : producerPropertiesMap.entrySet()) {
            var kafkaWriterProperties = producerProperties.getValue().getKafkaProperties();
            if (kafkaWriterProperties == null) {
                LOG.error(MISSING_KAFKA_WRITER_PROPS_MSG, producerProperties.getKey());
                // what to do with error
            }

            var producer = new KafkaProducer<>(
                    kafkaWriterProperties,
                    new StringSerializer(),
                    new StringSerializer());
            var topicName = producerProperties.getValue().getOutputTopic();
            var producerName = producerProperties.getKey();
            producerMap.put(producerName, producer);
            var updateCounter =
                    metricsRegistrar.registerCounter(SiembolMetrics.HEARTBEAT_MESSAGES_SENT.getMetricName(producerName));
            var errorCounter =
                    metricsRegistrar.registerCounter(SiembolMetrics.HEARTBEAT_PRODUCER_ERROR.getMetricName(producerName));
            this.executorService.scheduleAtFixedRate(() ->
                            this.executorService.execute(() -> this.sendHeartbeat(producer, topicName, producerName,
                                    updateCounter, errorCounter)),
                    heartbeatIntervalSeconds,
                    heartbeatIntervalSeconds,
                    TimeUnit.SECONDS);
        }
    }

    private void initialiseMessage(Map<String, Object> messageProperties) {
        // check empty message
        for (Map.Entry<String, Object> messageEntry : messageProperties.entrySet()) {
            this.message.setMessage(messageEntry.getKey(), messageEntry.getValue());
        }
    }

    private void sendHeartbeat(Producer<String, String> producer, String topicName, String producerName,
                               SiembolCounter updateCounter, SiembolCounter errorCounter) {
        var now = OffsetDateTime.now().truncatedTo(ChronoUnit.MILLIS);
        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
        this.message.setTimestamp(formatter.format(now));
        try {
            producer.send(new ProducerRecord<>(topicName, objectWriter.writeValueAsString(this.message))).get();
            updateCounter.increment();
            exceptionMap.remove(producerName);
        } catch (Exception e) {
            LOG.error("Error sending message to kafka with producer {}", producerName);
            errorCounter.increment();
            exceptionMap.put(producerName, e);
        }
    }

    private Mono<Health> checkHealth() {
        return Mono.just(exceptionMap.size() > errorThreshold? Health.down().build(): Health.up().build());
    }

    public void close() {
        for (var producer: this.producerMap.values()) {
            producer.close();
        }
    }
    // callable vs runnable
}
