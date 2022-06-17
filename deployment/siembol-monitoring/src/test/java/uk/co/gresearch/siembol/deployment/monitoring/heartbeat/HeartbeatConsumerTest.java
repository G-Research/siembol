package uk.co.gresearch.siembol.deployment.monitoring.heartbeat;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.TopologyTestDriver;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import uk.co.gresearch.siembol.common.constants.ServiceType;
import uk.co.gresearch.siembol.common.metrics.SiembolMetrics;
import uk.co.gresearch.siembol.common.metrics.test.SiembolMetricsTestRegistrar;
import uk.co.gresearch.siembol.common.testing.TestingDriverKafkaStreamsFactory;

import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;

public class HeartbeatConsumerTest {
    private final String heartbeatMessageStr = """
            {
                "timestamp": 1654095527000,
                "siembol_parsing_ts": 1654095527001,
                "siembol_enriching_ts": 1654095527020,
                "siembol_response_ts": 1654095527031,
                "siembol_heartbeat": true,
                "source_type": "heartbeat",
                "producer_name": "p1",
                "event_time": "2022-06-01T14:58:47.000Z"
            }
            """;
    private final String heartbeatMessageWithoutEnrichmentStr = """
            {
                "timestamp": 1654095527000,
                "siembol_parsing_ts": 1654095527001,
                "siembol_response_ts": 1654095527031,
                "siembol_heartbeat": true,
                "source_type": "heartbeat",
                "producer_name": "p1",
                "event_time": "2022-06-01T14:58:47.000Z"
            }
            """;
    private final String inputTopic = "input";
    private SiembolMetricsTestRegistrar metricsTestRegistrar;
    private KafkaStreams kafkaStreams;
    private TestingDriverKafkaStreamsFactory streamsFactory;
    private TopologyTestDriver testDriver;
    private TestInputTopic<String, String> testInputTopic;
    private MockedStatic<Instant> mockInstant;
    private HeartbeatConsumerProperties properties;

    @Before
    public void setUp() {
        metricsTestRegistrar = new SiembolMetricsTestRegistrar();
        kafkaStreams = Mockito.mock(KafkaStreams.class);
        streamsFactory = new TestingDriverKafkaStreamsFactory(kafkaStreams);

        var instant = Instant.parse("2022-06-01T14:58:47.823Z");
        mockInstant = Mockito.mockStatic(Instant.class);
        mockInstant.when(Instant::now).thenReturn(instant);

        properties = new HeartbeatConsumerProperties();
        properties.setInputTopic(inputTopic);
        properties.setKafkaProperties(new HashMap<>());
        properties.setEnabledServices(Arrays.asList(ServiceType.PARSING_APP, ServiceType.ENRICHMENT,
                ServiceType.RESPONSE));
    }

    @After
    public void tearDown() {
        streamsFactory.close();
    }

    @Test
    public void processingMessageOk() {
        new HeartbeatConsumer(properties, metricsTestRegistrar, streamsFactory);
        testDriver = streamsFactory.getTestDriver();
        testInputTopic = testDriver.createInputTopic(inputTopic, Serdes.String().serializer(),
                Serdes.String().serializer());
        testInputTopic.pipeInput(heartbeatMessageStr);
        Assert.assertEquals(1,
                metricsTestRegistrar.getGaugeValue(SiembolMetrics.HEARTBEAT_LATENCY_PARSING_MS.name()), 0);
        Assert.assertEquals(19,
                metricsTestRegistrar.getGaugeValue(SiembolMetrics.HEARTBEAT_LATENCY_ENRICHING_MS.name()), 0);
        Assert.assertEquals(11,
                metricsTestRegistrar.getGaugeValue(SiembolMetrics.HEARTBEAT_LATENCY_RESPONDING_MS.name()), 0);
        Assert.assertEquals(823,
                metricsTestRegistrar.getGaugeValue(SiembolMetrics.HEARTBEAT_LATENCY_TOTAL_MS.name()), 0);
        Assert.assertEquals(1, metricsTestRegistrar.getCounterValue(SiembolMetrics.HEARTBEAT_MESSAGES_READ.name()));
    }

    @Test
    public void processingError() {
        new HeartbeatConsumer(properties, metricsTestRegistrar, streamsFactory);
        testDriver = streamsFactory.getTestDriver();
        testInputTopic = testDriver.createInputTopic(inputTopic, Serdes.String().serializer(),
                Serdes.String().serializer());
        testInputTopic.pipeInput("test");
        Assert.assertEquals(1, metricsTestRegistrar.getCounterValue(SiembolMetrics.HEARTBEAT_CONSUMER_ERROR.name()));
    }

    @Test
    public void withoutEnrichmentService() {
        properties.setEnabledServices(Arrays.asList(ServiceType.PARSING_APP,
                ServiceType.RESPONSE));
        new HeartbeatConsumer(properties, metricsTestRegistrar, streamsFactory);
        testDriver = streamsFactory.getTestDriver();
        testInputTopic = testDriver.createInputTopic(inputTopic, Serdes.String().serializer(),
                Serdes.String().serializer());
        testInputTopic.pipeInput(heartbeatMessageWithoutEnrichmentStr);

        Assert.assertEquals(1,
                metricsTestRegistrar.getGaugeValue(SiembolMetrics.HEARTBEAT_LATENCY_PARSING_MS.name()), 0);
        Assert.assertEquals(30,
                metricsTestRegistrar.getGaugeValue(SiembolMetrics.HEARTBEAT_LATENCY_RESPONDING_MS.name()), 0);
        Assert.assertEquals(823,
                metricsTestRegistrar.getGaugeValue(SiembolMetrics.HEARTBEAT_LATENCY_TOTAL_MS.name()), 0);
    }

    @After
    public void close() {
        mockInstant.close();
    }
}
