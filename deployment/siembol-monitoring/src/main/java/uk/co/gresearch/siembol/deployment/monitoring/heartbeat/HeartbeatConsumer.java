package uk.co.gresearch.siembol.deployment.monitoring.heartbeat;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import uk.co.gresearch.siembol.common.constants.SiembolMessageFields;
import uk.co.gresearch.siembol.common.metrics.SiembolCounter;
import uk.co.gresearch.siembol.common.metrics.SiembolMetrics;
import uk.co.gresearch.siembol.common.metrics.SiembolMetricsRegistrar;
import uk.co.gresearch.siembol.deployment.monitoring.application.HeartbeatConsumerProperties;

import java.lang.invoke.MethodHandles;
import java.util.Map;
import java.util.Properties;

public class HeartbeatConsumer {
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final String INIT_START = "Kafka stream service initialisation started";
    private static final String INIT_COMPLETED = "Kafka stream service initialisation completed";
    private final KafkaStreams streams;
    private static ObjectReader MESSAGE_READER = new ObjectMapper()
            .readerFor(new TypeReference<Map<String, Object>>() {});
    private SiembolCounter parsingLatencyGauge;
    private SiembolCounter enrichmentLatencyGauge;
    private SiembolCounter responseLatencyGauge;
    private SiembolCounter totalLatencyGauge;

    public HeartbeatConsumer(HeartbeatConsumerProperties properties, SiembolMetricsRegistrar metricsRegistrar) {
        this(properties, metricsRegistrar, new KafkaStreamsFactory());
    }

    HeartbeatConsumer(HeartbeatConsumerProperties properties, SiembolMetricsRegistrar metricsRegistrar,
                      KafkaStreamsFactory streamsFactory) {
        streams = createStreams(streamsFactory, properties);
        streams.start();
        parsingLatencyGauge = metricsRegistrar.registerCounter(SiembolMetrics.HEARTBEAT_LATENCY_PARSING.name());
        enrichmentLatencyGauge = metricsRegistrar.registerCounter(SiembolMetrics.HEARTBEAT_LATENCY_ENRICHING.name());
        responseLatencyGauge = metricsRegistrar.registerCounter(SiembolMetrics.HEARTBEAT_LATENCY_RESPONDING.name());
        totalLatencyGauge = metricsRegistrar.registerCounter(SiembolMetrics.HEARTBEAT_LATENCY_TOTAL.name());
    }

    private KafkaStreams createStreams(KafkaStreamsFactory streamsFactory, HeartbeatConsumerProperties properties) {
        LOG.info(INIT_START);
        StreamsBuilder builder = new StreamsBuilder();
        builder.<String, String>stream(properties.getInputTopic())
                .mapValues(this::processMessage)
                .filter((x, y) -> y.getStatusCode() != OK)
                .mapValues(x -> x.getAttributes().getMessage())
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

    private void processMessage(String value) throws JsonProcessingException {
        Map<String, Object> message = MESSAGE_READER.readValue(value);
        Object timestamp = message.get(SiembolMessageFields.TIMESTAMP);
        Object parsingTimestamp = message.get(SiembolMessageFields.PARSING_TIME);
        Object enrichingTimestamp = message.get(SiembolMessageFields.ENRICHING_TIME);
        Object responseTimestamp = message.get(SiembolMessageFields.RESPONSE_TIME);

        // calculate latency between all services
        // cretae the model for expected output? Or is it just ResponseAlert?


    }
}
