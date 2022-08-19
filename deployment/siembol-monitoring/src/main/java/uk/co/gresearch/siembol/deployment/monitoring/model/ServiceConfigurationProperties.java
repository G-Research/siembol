package uk.co.gresearch.siembol.deployment.monitoring.model;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

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
