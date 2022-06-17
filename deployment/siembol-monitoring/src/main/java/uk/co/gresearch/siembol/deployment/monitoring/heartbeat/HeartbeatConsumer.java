package uk.co.gresearch.siembol.deployment.monitoring.heartbeat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import uk.co.gresearch.siembol.common.constants.ServiceType;
import uk.co.gresearch.siembol.common.metrics.SiembolCounter;
import uk.co.gresearch.siembol.common.metrics.SiembolGauge;
import uk.co.gresearch.siembol.common.metrics.SiembolMetrics;
import uk.co.gresearch.siembol.common.metrics.SiembolMetricsRegistrar;
import uk.co.gresearch.siembol.common.utils.KafkaStreamsFactoryImpl;
import uk.co.gresearch.siembol.common.utils.KafkaStreamsFactory;

import java.lang.invoke.MethodHandles;
import java.time.Instant;
import java.util.*;


public class HeartbeatConsumer {
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final String INIT_START = "Kafka stream service initialisation started";
    private static final String INIT_COMPLETED = "Kafka stream service initialisation completed";
    private final KafkaStreams streams;
    private static final ObjectReader MESSAGE_READER = new ObjectMapper()
            .readerFor(HeartbeatProcessedMessage.class);
    private final SiembolGauge totalLatencyGauge;
    private final SiembolCounter consumerErrorCount;
    private final SiembolCounter consumerMessageRead;
    private final List<Pair<ServiceType, SiembolGauge>> servicesMetrics = new ArrayList<>();

    public HeartbeatConsumer(HeartbeatConsumerProperties properties, SiembolMetricsRegistrar metricsRegistrar) {
        this(properties, metricsRegistrar, new KafkaStreamsFactoryImpl());
    }

    HeartbeatConsumer(HeartbeatConsumerProperties properties, SiembolMetricsRegistrar metricsRegistrar,
                      KafkaStreamsFactory streamsFactory) {
        consumerErrorCount = metricsRegistrar.registerCounter(SiembolMetrics.HEARTBEAT_CONSUMER_ERROR.name());
        consumerMessageRead = metricsRegistrar.registerCounter(SiembolMetrics.HEARTBEAT_MESSAGES_READ.name());

        if (properties.getEnabledServices() == null) {
            throw new IllegalArgumentException("Missing enabled services for heartbeat consumer.");
        }
        if (properties.getEnabledServices().contains(ServiceType.PARSING_APP)) {
            servicesMetrics.add(Pair.of(ServiceType.PARSING_APP,
                    metricsRegistrar.registerGauge(SiembolMetrics.HEARTBEAT_LATENCY_PARSING_MS.name())));
        }
        if (properties.getEnabledServices().contains(ServiceType.ENRICHMENT)) {
            servicesMetrics.add(Pair.of(ServiceType.ENRICHMENT,
                    metricsRegistrar.registerGauge(SiembolMetrics.HEARTBEAT_LATENCY_ENRICHING_MS.name())));
        }
        if (properties.getEnabledServices().contains(ServiceType.RESPONSE)) {
            servicesMetrics.add(Pair.of(ServiceType.RESPONSE,
                    metricsRegistrar.registerGauge(SiembolMetrics.HEARTBEAT_LATENCY_RESPONDING_MS.name())));
        }
        totalLatencyGauge = metricsRegistrar.registerGauge(SiembolMetrics.HEARTBEAT_LATENCY_TOTAL_MS.name());
        streams = createStreams(streamsFactory, properties);
        streams.start();
    }

    private KafkaStreams createStreams(KafkaStreamsFactory streamsFactory, HeartbeatConsumerProperties properties) {
        LOG.info(INIT_START);
        StreamsBuilder builder = new StreamsBuilder();
        builder.<String, String>stream(properties.getInputTopic())
                .foreach((key, value) -> this.processMessage(value));

        Properties configuration = new Properties();
        configuration.putAll(properties.getKafkaProperties());
        configuration.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        configuration.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        Topology topology = builder.build(configuration);

        KafkaStreams ret = streamsFactory.createKafkaStreams(topology, configuration);
        LOG.info(INIT_COMPLETED);
        return ret;
    }

    private void processMessage(String value) {
        try {
            var currentTimestamp = Instant.now().toEpochMilli();
            HeartbeatProcessedMessage message = MESSAGE_READER.readValue(value);
            var lastTimestamp = message.getTimestamp().longValue();

            for(Pair<ServiceType, SiembolGauge> serviceMetric: servicesMetrics) {
                switch (serviceMetric.getKey()) {
                    case PARSING_APP:
                        serviceMetric.getValue().setValue(message.getParsingTime().longValue() - lastTimestamp);
                        lastTimestamp = message.getParsingTime().longValue();
                        break;
                    case ENRICHMENT:
                        serviceMetric.getValue().setValue(message.getEnrichingTime().longValue() - lastTimestamp);
                        lastTimestamp = message.getEnrichingTime().longValue();
                        break;
                    case RESPONSE:
                        serviceMetric.getValue().setValue(message.getResponseTime().longValue() - lastTimestamp);
                        lastTimestamp = message.getResponseTime().longValue();
                }

            }
            totalLatencyGauge.setValue(currentTimestamp - message.getTimestamp().longValue());
            consumerMessageRead.increment();
        } catch (Exception e) {
            LOG.error("Error reading heartbeat message from kafka:  {}", e.toString());
            consumerErrorCount.increment();
        }
    }

    public Health checkHealth() {
        return streams.state().isRunningOrRebalancing() || streams.state().equals(KafkaStreams.State.CREATED)
                ? Health.up().build() :
                Health.down().build();
    }

    public void close() {
        streams.close();
    }
}
