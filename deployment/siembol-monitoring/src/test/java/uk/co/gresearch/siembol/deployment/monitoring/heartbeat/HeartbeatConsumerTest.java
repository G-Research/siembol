package uk.co.gresearch.siembol.deployment.monitoring.heartbeat;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.TestOutputTopic;
import org.apache.kafka.streams.TopologyTestDriver;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import uk.co.gresearch.siembol.common.metrics.SiembolMetrics;
import uk.co.gresearch.siembol.common.metrics.test.SiembolMetricsTestRegistrar;

import java.time.Instant;
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
    private final String inputTopic = "input";
    private final String errorTopic = "error";
    private SiembolMetricsTestRegistrar metricsTestRegistrar;
    private KafkaStreams kafkaStreams;
    private TestingDriverKafkaStreamsFactory streamsFactory;
    private TopologyTestDriver testDriver;
    private TestInputTopic<String, String> testInputTopic;
    private TestOutputTopic<String, String> testErrorTopic;
    private MockedStatic<Instant> mockInstant;

    @Before
    public void setUp() {
        metricsTestRegistrar = new SiembolMetricsTestRegistrar();
        kafkaStreams = Mockito.mock(KafkaStreams.class);
        streamsFactory = new TestingDriverKafkaStreamsFactory(kafkaStreams);

        var instant = Instant.parse("2022-06-01T14:58:47.823Z");
        mockInstant = Mockito.mockStatic(Instant.class);
        mockInstant.when(Instant::now).thenReturn(instant);

        var properties = new HeartbeatConsumerProperties();
        properties.setInputTopic(inputTopic);
        properties.setErrorTopic(errorTopic);
        properties.setKafkaProperties(new HashMap<>());

        var service = new HeartbeatConsumer(properties, metricsTestRegistrar, streamsFactory);
        testDriver = streamsFactory.getTestDriver();
        testInputTopic = testDriver.createInputTopic(inputTopic, Serdes.String().serializer(),
                Serdes.String().serializer());
        testErrorTopic = testDriver.createOutputTopic(errorTopic, Serdes.String().deserializer(),
                Serdes.String().deserializer());
    }

    @After
    public void tearDown() {
        streamsFactory.close();
    }

    @Test
    public void testProcessingMessage() {
        testInputTopic.pipeInput(heartbeatMessageStr);
        Assert.assertTrue(testErrorTopic.isEmpty());
        Assert.assertEquals(1,
                metricsTestRegistrar.getGaugeValue(SiembolMetrics.HEARTBEAT_LATENCY_PARSING_MS.name()), 0);
        Assert.assertEquals(19,
                metricsTestRegistrar.getGaugeValue(SiembolMetrics.HEARTBEAT_LATENCY_ENRICHING_MS.name()), 0);
        Assert.assertEquals(11,
                metricsTestRegistrar.getGaugeValue(SiembolMetrics.HEARTBEAT_LATENCY_RESPONDING_MS.name()), 0);
        Assert.assertEquals(823,
                metricsTestRegistrar.getGaugeValue(SiembolMetrics.HEARTBEAT_LATENCY_TOTAL_MS.name()), 0);
    }

    @Test
    public void testProcessingError() {
        testInputTopic.pipeInput("test");
        Assert.assertFalse(testErrorTopic.isEmpty());
    }
}
