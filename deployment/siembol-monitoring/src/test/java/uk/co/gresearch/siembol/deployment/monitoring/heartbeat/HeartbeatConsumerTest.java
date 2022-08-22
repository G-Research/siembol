package uk.co.gresearch.siembol.deployment.monitoring.heartbeat;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.TopologyTestDriver;
import org.junit.*;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import uk.co.gresearch.siembol.common.constants.ServiceType;
import uk.co.gresearch.siembol.common.metrics.SiembolMetrics;
import uk.co.gresearch.siembol.common.metrics.test.SiembolMetricsTestRegistrar;
import uk.co.gresearch.siembol.common.testing.TestingDriverKafkaStreamsFactory;
import uk.co.gresearch.siembol.deployment.monitoring.model.HeartbeatConsumerProperties;

import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;

import static org.mockito.Mockito.when;

public class HeartbeatConsumerTest {
    private static MockedStatic<Instant> MOCK_INSTANT;
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

    private HeartbeatConsumerProperties properties;

    @BeforeClass
    public static void staticSetUp() {
        var instant = Instant.parse("2022-06-01T14:58:47.823Z");
        MOCK_INSTANT = Mockito.mockStatic(Instant.class);
        MOCK_INSTANT.when(Instant::now).thenReturn(instant);
    }

    @AfterClass
    public static void staticTearDown() {
        MOCK_INSTANT.close();
    }

    @Before
    public void setUp() {
        metricsTestRegistrar = new SiembolMetricsTestRegistrar();
        kafkaStreams = Mockito.mock(KafkaStreams.class);
        streamsFactory = new TestingDriverKafkaStreamsFactory(kafkaStreams);

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
        try (var ignored = new HeartbeatConsumer(properties, metricsTestRegistrar, streamsFactory)) {
            testDriver = streamsFactory.getTestDriver();
            testInputTopic = testDriver.createInputTopic(inputTopic, Serdes.String().serializer(),
                    Serdes.String().serializer());
            testInputTopic.pipeInput(heartbeatMessageStr);
            Assert.assertEquals(1,
                    metricsTestRegistrar.getGaugeValue(
                            SiembolMetrics.HEARTBEAT_LATENCY_PARSING_MS.getMetricName()), 0);
            Assert.assertEquals(19,
                    metricsTestRegistrar.getGaugeValue(
                            SiembolMetrics.HEARTBEAT_LATENCY_ENRICHING_MS.getMetricName()), 0);
            Assert.assertEquals(11,
                    metricsTestRegistrar.getGaugeValue(
                            SiembolMetrics.HEARTBEAT_LATENCY_RESPONDING_MS.getMetricName()), 0);
            Assert.assertEquals(823,
                    metricsTestRegistrar.getGaugeValue(
                            SiembolMetrics.HEARTBEAT_LATENCY_TOTAL_MS.getMetricName()), 0);
            Assert.assertEquals(1, metricsTestRegistrar.getCounterValue(
                    SiembolMetrics.HEARTBEAT_MESSAGES_READ.getMetricName()));
        }
    }

    @Test
    public void processingError() {
        try (var ignored = new HeartbeatConsumer(properties, metricsTestRegistrar, streamsFactory)) {
            testDriver = streamsFactory.getTestDriver();
            testInputTopic = testDriver.createInputTopic(inputTopic, Serdes.String().serializer(),
                    Serdes.String().serializer());
            testInputTopic.pipeInput("test");
            Assert.assertEquals(1,
                    metricsTestRegistrar.getCounterValue(SiembolMetrics.HEARTBEAT_CONSUMER_ERROR.getMetricName()));
        }
    }

    @Test
    public void withoutEnrichmentService() {
        properties.setEnabledServices(Arrays.asList(ServiceType.PARSING_APP,
                ServiceType.RESPONSE));
        try (var ignored = new HeartbeatConsumer(properties, metricsTestRegistrar, streamsFactory)) {
            testDriver = streamsFactory.getTestDriver();
            testInputTopic = testDriver.createInputTopic(inputTopic, Serdes.String().

                            serializer(),
                    Serdes.String().

                            serializer());
            testInputTopic.pipeInput(heartbeatMessageWithoutEnrichmentStr);

            Assert.assertEquals(1, metricsTestRegistrar.getGaugeValue(
                            SiembolMetrics.HEARTBEAT_LATENCY_PARSING_MS.getMetricName()), 0);
            Assert.assertEquals(30, metricsTestRegistrar.getGaugeValue(
                    SiembolMetrics.HEARTBEAT_LATENCY_RESPONDING_MS.getMetricName()), 0);
            Assert.assertEquals(823, metricsTestRegistrar.getGaugeValue(
                    SiembolMetrics.HEARTBEAT_LATENCY_TOTAL_MS.getMetricName()), 0);
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void missingEnabledServices() {
        properties.setEnabledServices(null);
        try (var ignored = new HeartbeatConsumer(properties, metricsTestRegistrar, streamsFactory)) {
            Assert.assertNull(ignored);
        }
    }

    @Test
    public void healthUpCreated() {
        try (var consumer = new HeartbeatConsumer(properties, metricsTestRegistrar, streamsFactory)) {
            when(kafkaStreams.state()).thenReturn(KafkaStreams.State.CREATED);
            Health health = consumer.checkHealth();
            Assert.assertEquals(Status.UP, health.getStatus());
        }
    }

    @Test
    public void healthUpRunning() {
        try (var consumer = new HeartbeatConsumer(properties, metricsTestRegistrar, streamsFactory)) {
            when(kafkaStreams.state()).thenReturn(KafkaStreams.State.RUNNING);
            Health health = consumer.checkHealth();
            Assert.assertEquals(Status.UP, health.getStatus());
        }
    }

    @Test
    public void healthUpReBalancing() {
        try (var consumer = new HeartbeatConsumer(properties, metricsTestRegistrar, streamsFactory)) {
            when(kafkaStreams.state()).thenReturn(KafkaStreams.State.REBALANCING);
            Health health = consumer.checkHealth();
            Assert.assertEquals(Status.UP, health.getStatus());
        }
    }

    @Test
    public void healthDownError() {
        try (var consumer = new HeartbeatConsumer(properties, metricsTestRegistrar, streamsFactory)) {
            when(kafkaStreams.state()).thenReturn(KafkaStreams.State.ERROR);
            Health health = consumer.checkHealth();
            Assert.assertEquals(Status.DOWN, health.getStatus());
        }
    }
}
