package uk.co.gresearch.siembol.deployment.monitoring.heartbeat;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import reactor.core.publisher.Mono;
import uk.co.gresearch.siembol.common.constants.SiembolMessageFields;
import uk.co.gresearch.siembol.common.metrics.SiembolGauge;
import uk.co.gresearch.siembol.common.metrics.SiembolMetrics;
import uk.co.gresearch.siembol.common.metrics.SiembolMetricsRegistrar;
import uk.co.gresearch.siembol.deployment.monitoring.application.HeartbeatConsumerProperties;

import java.lang.invoke.MethodHandles;
import java.util.Map;
import java.util.Properties;

import static uk.co.gresearch.siembol.deployment.monitoring.heartbeat.HeartbeatProcessingResult.StatusCode.OK;

public class HeartbeatConsumer {
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final String INIT_START = "Kafka stream service initialisation started";
    private static final String INIT_COMPLETED = "Kafka stream service initialisation completed";
    private final KafkaStreams streams;
    private static final ObjectReader MESSAGE_READER = new ObjectMapper()
            .readerFor(new TypeReference<Map<String, Object>>() {});
    private final SiembolGauge parsingLatencyGauge;
    private final SiembolGauge enrichmentLatencyGauge;
    private final SiembolGauge responseLatencyGauge;
    private final SiembolGauge totalLatencyGauge;

    public HeartbeatConsumer(HeartbeatConsumerProperties properties, SiembolMetricsRegistrar metricsRegistrar) {
        this(properties, metricsRegistrar, new KafkaStreamsFactory());
    }

    HeartbeatConsumer(HeartbeatConsumerProperties properties, SiembolMetricsRegistrar metricsRegistrar,
                      KafkaStreamsFactory streamsFactory) {
        streams = createStreams(streamsFactory, properties);
        streams.start();
        parsingLatencyGauge = metricsRegistrar.registerGauge(SiembolMetrics.HEARTBEAT_LATENCY_PARSING.name());
        enrichmentLatencyGauge = metricsRegistrar.registerGauge(SiembolMetrics.HEARTBEAT_LATENCY_ENRICHING.name());
        responseLatencyGauge = metricsRegistrar.registerGauge(SiembolMetrics.HEARTBEAT_LATENCY_RESPONDING.name());
        totalLatencyGauge = metricsRegistrar.registerGauge(SiembolMetrics.HEARTBEAT_LATENCY_TOTAL.name());
    }

    private KafkaStreams createStreams(KafkaStreamsFactory streamsFactory, HeartbeatConsumerProperties properties) {
        LOG.info(INIT_START);
        StreamsBuilder builder = new StreamsBuilder();
        builder.<String, String>stream(properties.getInputTopic())
                .mapValues(this::processMessage)
                .filter((x, y) -> y.getStatusCode() != OK)
                .mapValues(HeartbeatProcessingResult::getMessage)
                .to(properties.getErrorTopic());

        Properties configuration = new Properties();
        configuration.putAll(properties.getKafkaProperties());
        configuration.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        configuration.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        Topology topology = builder.build(configuration);

        KafkaStreams ret = streamsFactory.createKafkaStreams(topology, configuration);
        LOG.info(INIT_COMPLETED);
        return ret;
    }

    private HeartbeatProcessingResult processMessage(String value) {
        try {
            Map<String, Object> message = MESSAGE_READER.readValue(value);
            // check if numbers before + if field exist?
            var currentTimestamp = System.currentTimeMillis();
            var timestamp = (Number) message.get(SiembolMessageFields.TIMESTAMP);
            var parsingTimestamp = (Number) message.get(SiembolMessageFields.PARSING_TIME);
            var enrichingTimestamp = (Number) message.get(SiembolMessageFields.ENRICHING_TIME);
            var responseTimestamp = (Number) message.get(SiembolMessageFields.RESPONSE_TIME);

            parsingLatencyGauge.setValue(parsingTimestamp.longValue() - timestamp.longValue());
            enrichmentLatencyGauge.setValue(enrichingTimestamp.longValue() - parsingTimestamp.longValue());
            responseLatencyGauge.setValue(responseTimestamp.longValue() - enrichingTimestamp.longValue());
            totalLatencyGauge.setValue(currentTimestamp - timestamp.longValue());

            return HeartbeatProcessingResult.fromSuccess();
        } catch (Exception e) {
            LOG.error("error message:  {}", e.toString());
            return HeartbeatProcessingResult.fromException(e);
        }
    }

    public Mono<Health> checkHealth() {
        return Mono.just(streams.state().isRunningOrRebalancing() || streams.state().equals(KafkaStreams.State.CREATED)
                ? Health.up().build() :
                Health.down().build());
    }

    public void close() {
        streams.close();
    }
}
