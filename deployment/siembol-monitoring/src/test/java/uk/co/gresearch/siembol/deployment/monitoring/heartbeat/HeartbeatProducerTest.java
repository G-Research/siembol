package uk.co.gresearch.siembol.deployment.monitoring.heartbeat;

import org.apache.kafka.clients.producer.MockProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import uk.co.gresearch.siembol.common.metrics.SiembolMetrics;
import uk.co.gresearch.siembol.common.metrics.test.SiembolMetricsTestRegistrar;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import org.mockito.MockedStatic;

public class HeartbeatProducerTest {
    private SiembolMetricsTestRegistrar metricsTestRegistrar;
    private final Map<String, HeartbeatProducerProperties> producerPropertiesMap= new HashMap<>();
    private final Map<String, Object> heartbeatMessageProperties = new HashMap<>();
    private MockProducer<String, String> producer1;
    private MockProducer<String, String> producer2;
    private final Map<String, Producer<String,String>> producerMap = new HashMap<>();
    private ScheduledExecutorService mockScheduledService;
    private MockedStatic<Instant> mockInstant;

    @Before
    public void setUp() {
        metricsTestRegistrar = new SiembolMetricsTestRegistrar();

        var instant = Instant.parse("2022-05-31T09:10:11.50Z");
        mockInstant = Mockito.mockStatic(Instant.class);
        mockInstant.when(Instant::now).thenReturn(instant);

        mockScheduledService = mock(ScheduledExecutorService.class);
        given(mockScheduledService.scheduleAtFixedRate(any(Runnable.class), anyLong(), anyLong(),
                any(TimeUnit.class))).willReturn(mock(ScheduledFuture.class));

        heartbeatMessageProperties.put("key", "value");

        var heartbeatProducerProperties1 = new HeartbeatProducerProperties();
        heartbeatProducerProperties1.setOutputTopic("heartbeat1");
        var heartbeatProducerProperties2 = new HeartbeatProducerProperties();
        heartbeatProducerProperties2.setOutputTopic("heartbeat2");
        producerPropertiesMap.put("p1", heartbeatProducerProperties1);
        producerPropertiesMap.put("p2", heartbeatProducerProperties2);

        producer1 = new MockProducer<>(true, new StringSerializer(), new StringSerializer());
        producer2 = new MockProducer<>(true, new StringSerializer(), new StringSerializer());
        producerMap.put("p1", producer1);
        producerMap.put("p2", producer2);
    }

    @Test
    public void sendHeartbeatOk() {
        new HeartbeatProducer(producerPropertiesMap, 10, heartbeatMessageProperties,
                metricsTestRegistrar, producerMap, mockScheduledService);
        ArgumentCaptor<Runnable> argumentCaptor = ArgumentCaptor.forClass(Runnable.class);
        verify(mockScheduledService, times(2)).scheduleAtFixedRate(
                argumentCaptor.capture(),
                anyLong(),
                anyLong(),
                any(TimeUnit.class));
        argumentCaptor.getAllValues().get(0).run();
        producer1.completeNext();
        argumentCaptor.getAllValues().get(1).run();
        Assert.assertEquals(producer1.history().size(), 1);
        Assert.assertEquals(producer1.history().get(0).topic(), "heartbeat1");
        Assert.assertEquals(producer1.history().get(0).value(), "{\"event_time\":\"2022-05-31T09:10:11.500Z\"," +
                "\"siembol_heartbeat\":true," +
                "\"producer_name\":\"p1\",\"key\":\"value\"}");
        Assert.assertEquals(producer2.history().size(), 1);
        Assert.assertEquals(producer2.history().get(0).topic(), "heartbeat2");
        Assert.assertEquals(producer2.history().get(0).value(),"{\"event_time\":\"2022-05-31T09:10:11.500Z\"," +
                "\"siembol_heartbeat\":true," +
                "\"producer_name\":\"p2\",\"key\":\"value\"}");
        argumentCaptor.getAllValues().get(0).run();
        Assert.assertEquals(2,
                metricsTestRegistrar.getCounterValue(SiembolMetrics.HEARTBEAT_MESSAGES_SENT.getMetricName("p1")));
        Assert.assertEquals(1,
                metricsTestRegistrar.getCounterValue(SiembolMetrics.HEARTBEAT_MESSAGES_SENT.getMetricName("p2")));
    }

    @Test
    public void sendHeartbeatError() {
        var producer3 = new MockProducer<>(true, new StringSerializer(), new StringSerializer());
        var producer3Properties = new HeartbeatProducerProperties();
        producerPropertiesMap.put("p3", producer3Properties);
        producerMap.put("p3", producer3);
        new HeartbeatProducer(producerPropertiesMap, 10, heartbeatMessageProperties,
                metricsTestRegistrar, producerMap, mockScheduledService);

        ArgumentCaptor<Runnable> argumentCaptor = ArgumentCaptor.forClass(Runnable.class);
        verify(mockScheduledService, times(3)).scheduleAtFixedRate(
                argumentCaptor.capture(),
                anyLong(),
                anyLong(),
                any(TimeUnit.class));
        argumentCaptor.getAllValues().get(2).run();
        Assert.assertEquals(1,
                metricsTestRegistrar.getCounterValue(SiembolMetrics.HEARTBEAT_PRODUCER_ERROR.getMetricName("p3")));
    }

    @After
    public void close() {
        mockInstant.close();
    }
}
