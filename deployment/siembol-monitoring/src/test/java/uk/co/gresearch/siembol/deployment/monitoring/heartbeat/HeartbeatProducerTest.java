package uk.co.gresearch.siembol.deployment.monitoring.heartbeat;

import org.apache.kafka.clients.producer.MockProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.Before;
import org.junit.Test;
import uk.co.gresearch.siembol.common.metrics.test.SiembolMetricsTestRegistrar;
import uk.co.gresearch.siembol.deployment.monitoring.application.HeartbeatProducerProperties;

import java.util.HashMap;
import java.util.Map;

public class HeartbeatProducerTest {
    private SiembolMetricsTestRegistrar metricsTestRegistrar;
    private Map<String, HeartbeatProducerProperties> producerPropertiesMap;
    private Map<String, Object> heartbeatMessageProperties = new HashMap<>();
    private Map<String, Producer<String,String>> producerMap = new HashMap<>();

    @Before
    public void setUp() {
        metricsTestRegistrar = new SiembolMetricsTestRegistrar();
        var cachedMetricsRegistrar = metricsTestRegistrar.cachedRegistrar();

        var heartbeatProducerProperties1 = new HeartbeatProducerProperties();
        heartbeatProducerProperties1.setOutputTopic("heartbeat1");
        var heartbeatProducerProperties2 = new HeartbeatProducerProperties();
        heartbeatProducerProperties2.setOutputTopic("heartbeat2");
        producerPropertiesMap.put("p1", heartbeatProducerProperties1);
        producerPropertiesMap.put("p2", heartbeatProducerProperties2);

        var producer1 = new MockProducer<>(true, new StringSerializer(), new StringSerializer());
        var producer2 = new MockProducer<>(true, new StringSerializer(), new StringSerializer());
        producerMap.put("p1", producer1);
        producerMap.put("p2", producer2);

        var heartbeatProducer = new HeartbeatProducer(producerPropertiesMap, 10, heartbeatMessageProperties,
                cachedMetricsRegistrar, producerMap);

    }

    @Test
    public void writeOk() {
        return;
    }
}
