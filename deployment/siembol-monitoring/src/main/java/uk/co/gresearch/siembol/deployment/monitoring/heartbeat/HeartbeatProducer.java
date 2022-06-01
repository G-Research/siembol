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

import java.io.Closeable;
import java.lang.invoke.MethodHandles;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class HeartbeatProducer implements Closeable {
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final String MISSING_KAFKA_WRITER_PROPS_MSG = "Missing heartbeat kafka producer properties for {}";
    private final ScheduledExecutorService executorService;
    private final HeartbeatMessage message = new HeartbeatMessage();
    private static final ObjectWriter objectWriter = new ObjectMapper().writer();
    private final Map<String, Exception> exceptionMap = new HashMap<>();
    private final int errorThreshold;
    private final Map<String, Producer<String,String>> producerMap = new HashMap<>();

    public HeartbeatProducer(Map<String, HeartbeatProducerProperties> producerPropertiesMap,
                             int heartbeatIntervalSeconds,
                             Map<String, Object> heartbeatMessageProperties,
                             SiembolMetricsRegistrar metricsRegistrar) {

        this(producerPropertiesMap, heartbeatIntervalSeconds, heartbeatMessageProperties, metricsRegistrar,
                createProducers(producerPropertiesMap), Executors.newSingleThreadScheduledExecutor());
    }

    public HeartbeatProducer(Map<String, HeartbeatProducerProperties> producerPropertiesMap,
                      int heartbeatIntervalSeconds,
                      Map<String, Object> heartbeatMessageProperties,
                      SiembolMetricsRegistrar metricsRegistrar,
                      Map<String, Producer<String, String>> producerMap,
                      ScheduledExecutorService executorService) {
        this.executorService = executorService;
        this.initialiseMessage(heartbeatMessageProperties);
        this.errorThreshold = producerPropertiesMap.size();
        for (Map.Entry<String, Producer<String, String>> producer: producerMap.entrySet()) {
            var producerName = producer.getKey();
            var topicName = producerPropertiesMap.get(producerName).getOutputTopic();
            var updateCounter =
                    metricsRegistrar.registerCounter(SiembolMetrics.HEARTBEAT_MESSAGES_SENT.getMetricName(producerName));
            var errorCounter =
                    metricsRegistrar.registerCounter(SiembolMetrics.HEARTBEAT_PRODUCER_ERROR.getMetricName(producerName));
            this.executorService.scheduleAtFixedRate(() ->
                             this.sendHeartbeat(producer.getValue(), topicName,
                                    producerName,
                                    updateCounter, errorCounter),
                    heartbeatIntervalSeconds,
                    heartbeatIntervalSeconds,
                    TimeUnit.SECONDS);
        }

    }

    private static Map<String, Producer<String, String>> createProducers(Map<String, HeartbeatProducerProperties> producerPropertiesMap) {
        Map<String, Producer<String, String>> producerMap = new HashMap<>();
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
            var producerName = producerProperties.getKey();
            producerMap.put(producerName, producer);
        }
        return producerMap;
    }

    private void initialiseMessage(Map<String, Object> messageProperties) {
        for (Map.Entry<String, Object> messageEntry : messageProperties.entrySet()) {
            this.message.setMessage(messageEntry.getKey(), messageEntry.getValue());
        }
    }

    private void sendHeartbeat(Producer<String, String> producer, String topicName, String producerName,
                               SiembolCounter updateCounter, SiembolCounter errorCounter) {
        var now = Instant.now().truncatedTo(ChronoUnit.MILLIS);
        this.message.setEventTime(now.toString());
        this.message.setProducerName(producerName);
        try {
            producer.send(new ProducerRecord<>(topicName, objectWriter.writeValueAsString(this.message))).get();
            updateCounter.increment();
            LOG.info("Sent heartbeat with producer {} to topic {}", producerName, topicName);
            exceptionMap.remove(producerName);
        } catch (Exception e) {
            LOG.error("Error sending message to kafka with producer {}: {}", producerName, e.toString());
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
}
