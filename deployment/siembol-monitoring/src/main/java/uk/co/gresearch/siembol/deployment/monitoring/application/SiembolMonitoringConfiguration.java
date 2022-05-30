package uk.co.gresearch.siembol.deployment.monitoring.application;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(HeartbeatProperties.class)
public class SiembolMonitoringConfiguration {
    @Autowired
    private HeartbeatProperties properties;
}
