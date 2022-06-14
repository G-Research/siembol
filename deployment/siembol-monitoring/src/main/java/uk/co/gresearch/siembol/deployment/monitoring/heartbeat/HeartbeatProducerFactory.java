package uk.co.gresearch.siembol.deployment.monitoring.heartbeat;

import uk.co.gresearch.siembol.common.metrics.SiembolMetricsRegistrar;

import java.util.Map;

public class HeartbeatProducerFactory {
    public HeartbeatProducer createHeartbeatProducer(HeartbeatProducerProperties properties,
                                                     String name,
                                                     Map<String, Object> heartbeatMessageProperties,
                                                     SiembolMetricsRegistrar metricsRegistrar) {
        return new HeartbeatProducer(properties, name, heartbeatMessageProperties, metricsRegistrar);
    }
}
