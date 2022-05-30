package uk.co.gresearch.siembol.deployment.monitoring.heartbeat;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.gresearch.siembol.common.metrics.SiembolMetricsRegistrar;
import uk.co.gresearch.siembol.common.utils.TimeProvider;
import uk.co.gresearch.siembol.deployment.monitoring.application.HeartbeatProducerProperties;

import java.lang.invoke.MethodHandles;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class HeartbeatProducer {
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final String MISSING_KAFKA_WRITER_PROPS_MSG = "Missing heartbeat kafka producer properties for {}";
    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    private final HeartbeatMessage message = new HeartbeatMessage();
    private SiembolMetricsRegistrar metricsRegistrar;

    public HeartbeatProducer(Map<String, HeartbeatProducerProperties> producerPropertiesMap,
                             int heartbeatIntervalSeconds,
                             Map<String, Object> heartbeatMessageProperties,
                             SiembolMetricsRegistrar metricsRegistrar) {
        this.metricsRegistrar = metricsRegistrar;
        this.initialiseMessage(heartbeatMessageProperties);
        for (Map.Entry<String, HeartbeatProducerProperties> producerProperties : producerPropertiesMap.entrySet()) {
            var kafkaWriterProperties = producerProperties.getValue().getKafkaProperties();
            if (kafkaWriterProperties == null) {
                LOG.error(MISSING_KAFKA_WRITER_PROPS_MSG, producerProperties.getKey());
                // what to do with error
            }

            // register counter here and send as attributes in callback
            var producer = new KafkaProducer<>(
                    kafkaWriterProperties,
                    new StringSerializer(),
                    new StringSerializer());
            var topicName = producerProperties.getValue().getOutputTopic();
            var producerName = producerProperties.getKey();
            this.executorService.scheduleAtFixedRate(() ->
                            this.executorService.execute(() -> this.sendHeartbeat(producer, topicName, producerName)),
                    heartbeatIntervalSeconds,
                    heartbeatIntervalSeconds,
                    TimeUnit.SECONDS);
        }
    }

    private void initialiseMessage(Map<String, Object> messageProperties) {
        this.message.setSiembolHeartbeat(true); // true by default
// set the name        // what if empty message? -> should not fail
        for (Map.Entry<String, Object> messageEntry : messageProperties.entrySet()) {
            this.message.setMessage(messageEntry.getKey(), messageEntry.getValue());
        }
    }


    private void sendHeartbeat(Producer<String, String> producer, String topicName, String producerName) {
        var objectWriter = new ObjectMapper().writer(); // one static writer
        this.message.setTimestamp(new TimeProvider().getCurrentTimeInMs()); // current ms -> format ISO, time
        // formatter
        try {
            producer.send(new ProducerRecord<>(topicName, objectWriter.writeValueAsString(this.message))).get();
            this.metricsRegistrar.registerCounter(HeartbeatMetrics.HEARTBEAT_MESSAGES_SENT.getMetricName(producerName)).increment();
        } catch (Exception e) {
            LOG.error("Error sending message to kakfa with producer {}", producerName);
            this.metricsRegistrar.registerCounter(HeartbeatMetrics.HEARTBEAT_PRODUCER_ERROR.getMetricName(producerName)).increment();
        }
    }

    //getHealth method

    // do I need to cloe producer? yes: add closeable

    // callable vs runnable
    // time zone in applica
    // UTC time

    // map exception: producername: exception -> if all have exception fail + threshold
    // in map last exception
}
