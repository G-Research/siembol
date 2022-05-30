package uk.co.gresearch.siembol.deployment.monitoring.application;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import uk.co.gresearch.siembol.common.metrics.SiembolMetricsRegistrar;
import uk.co.gresearch.siembol.common.metrics.spring.SpringMetricsRegistrar;
import uk.co.gresearch.siembol.deployment.monitoring.heartbeat.HeartbeatProducer;

@Configuration
@EnableConfigurationProperties(HeartbeatProperties.class)
public class SiembolMonitoringConfiguration {
    @Autowired
    private HeartbeatProperties properties;
    @Autowired
    private MeterRegistry springMeterRegistrar;

    @Bean("metricsRegistrar")
    SiembolMetricsRegistrar metricsRegistrar() {
        return new SpringMetricsRegistrar(springMeterRegistrar);
    }

    @Bean("heartbeatProducer")
    @DependsOn("metricsRegistrar")
    HeartbeatProducer heartbeatProducer(@Autowired SiembolMetricsRegistrar metricsRegistrar) {
        return new HeartbeatProducer(properties.getHeartbeatProducers(), properties.getHeartbeatIntervalSeconds(),
                properties.getMessage(), metricsRegistrar);
    }
}
