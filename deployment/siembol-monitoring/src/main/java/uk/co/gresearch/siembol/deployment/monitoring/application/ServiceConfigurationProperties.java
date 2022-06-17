package uk.co.gresearch.siembol.deployment.monitoring.application;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import uk.co.gresearch.siembol.deployment.monitoring.heartbeat.HeartbeatProperties;

@ConfigurationProperties(prefix = "siembol-monitoring")
public class ServiceConfigurationProperties {
    @NestedConfigurationProperty
    private HeartbeatProperties heartbeatProperties;

    public HeartbeatProperties getHeartbeatProperties() {
        return heartbeatProperties;
    }

    public void setHeartbeatProperties(HeartbeatProperties heartbeatProperties) {
        this.heartbeatProperties = heartbeatProperties;
    }
}
