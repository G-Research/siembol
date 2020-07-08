package uk.co.gresearch.siembol.configeditor.rest;

import org.springframework.boot.context.properties.NestedConfigurationProperty;
import uk.co.gresearch.siembol.configeditor.configstore.ConfigStoreProperties;

import java.util.Map;

public class ServiceConfigurationProperties {
    private String type;
    @NestedConfigurationProperty
    private ConfigStoreProperties configStore;

    private String uiConfigFileName;
    private String testSpecUiConfigFileName;
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

    public String getTestSpecUiConfigFileName() {
        return testSpecUiConfigFileName;
    }

    public void setTestSpecUiConfigFileName(String testSpecUiConfigFileName) {
        this.testSpecUiConfigFileName = testSpecUiConfigFileName;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }
}
