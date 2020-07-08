package uk.co.gresearch.siembol.configeditor.rest;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.util.Map;

@ConfigurationProperties(prefix = "config-editor")
public class ConfigEditorConfigurationProperties {
    @NestedConfigurationProperty
    private Map<String, ServiceConfigurationProperties> services;

    public Map<String, ServiceConfigurationProperties> getServices() {
        return services;
    }

    public void setServices(Map<String, ServiceConfigurationProperties> services) {
        this.services = services;
    }
}
