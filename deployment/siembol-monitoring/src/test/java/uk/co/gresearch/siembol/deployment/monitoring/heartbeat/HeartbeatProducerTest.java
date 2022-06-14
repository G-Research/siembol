package uk.co.gresearch.siembol.deployment.monitoring.heartbeat;

import org.apache.kafka.clients.producer.MockProducer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import uk.co.gresearch.siembol.common.metrics.SiembolMetrics;
import uk.co.gresearch.siembol.common.metrics.test.SiembolMetricsTestRegistrar;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import org.mockito.MockedStatic;

public class HeartbeatProducerTest {
    private SiembolMetricsTestRegistrar metricsTestRegistrar;
    private final Map<String, Object> heartbeatMessageProperties = new HashMap<>();
    private MockedStatic<Instant> mockInstant;

    @Before
    public void setUp() {
        metricsTestRegistrar = new SiembolMetricsTestRegistrar();
        var instant = Instant.parse("2022-05-31T09:10:11.50Z");
        mockInstant = Mockito.mockStatic(Instant.class);
        mockInstant.when(Instant::now).thenReturn(instant);
        heartbeatMessageProperties.put("key", "value");
    }

    @Test
    public void sendHeartbeatOk() {
        var producerProperties = new HeartbeatProducerProperties();
        producerProperties.setOutputTopic("heartbeat");
        var producer = new MockProducer<>(true, new StringSerializer(), new StringSerializer());
        var heartbeatProducer = new HeartbeatProducer(producerProperties, "p", heartbeatMessageProperties,
                metricsTestRegistrar, x -> producer);
        heartbeatProducer.sendHeartbeat();
        Assert.assertEquals(producer.history().size(), 1);
        Assert.assertEquals(producer.history().get(0).topic(), "heartbeat");
        Assert.assertEquals(producer.history().get(0).value(), "{\"event_time\":\"2022-05-31T09:10:11.500Z\"," +
                "\"siembol_heartbeat\":true," +
                "\"producer_name\":\"p\",\"key\":\"value\"}");
        heartbeatProducer.sendHeartbeat();
        Assert.assertEquals(2,
                metricsTestRegistrar.getCounterValue(SiembolMetrics.HEARTBEAT_MESSAGES_SENT.getMetricName("p")));
    }

    @Test
    public void sendHeartbeatError() {
        var producer = new MockProducer<>(true, new StringSerializer(), new StringSerializer());
        var producerProperties = new HeartbeatProducerProperties();
        var heartbeatProducer = new HeartbeatProducer(
                producerProperties,
                "p",
                heartbeatMessageProperties,
                metricsTestRegistrar,
                x -> producer);

        heartbeatProducer.sendHeartbeat();
        Assert.assertEquals(1,
                metricsTestRegistrar.getCounterValue(SiembolMetrics.HEARTBEAT_PRODUCER_ERROR.getMetricName("p")));
    }

    @After
    public void close() {
        mockInstant.close();
    }
}
