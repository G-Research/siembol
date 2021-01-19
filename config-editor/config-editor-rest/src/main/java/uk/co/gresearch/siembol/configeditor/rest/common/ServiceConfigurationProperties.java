package uk.co.gresearch.siembol.configeditor.rest.common;

import org.springframework.boot.context.properties.NestedConfigurationProperty;
import uk.co.gresearch.siembol.configeditor.model.ConfigStoreProperties;

import java.util.Map;

public class ServiceConfigurationProperties {
    private static final String UNKNOWN_FILE_NAME = "unknown";
    private String type;
    @NestedConfigurationProperty
    private ConfigStoreProperties configStore;

    private String uiConfigFileName = UNKNOWN_FILE_NAME;
    private Map<String, String> attributes;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public ConfigStoreProperties getConfigStore() {
        return configStore;
    }

    public void setConfigStore(ConfigStoreProperties configStore) {
        this.configStore = configStore;
    }

    public String getUiConfigFileName() {
        return uiConfigFileName;
    }

    public void setUiConfigFileName(String uiConfigFileName) {
        this.uiConfigFileName = uiConfigFileName;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }
}
