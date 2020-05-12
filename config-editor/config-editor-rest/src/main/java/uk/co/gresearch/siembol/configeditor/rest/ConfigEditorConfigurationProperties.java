package uk.co.gresearch.siembol.configeditor.rest;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import uk.co.gresearch.siembol.configeditor.configstore.ConfigStoreProperties;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@ConfigurationProperties(prefix = "config-editor")
public class ConfigEditorConfigurationProperties {
    private String centrifugeUrl;
    private String elkUrl;
    private String elkTemplatePath;

    @NestedConfigurationProperty
    private Map<String, ConfigStoreProperties> configStore;

    private Map<String, String> uiConfigFileName;

    private Map<String, String> testSpecUiConfigFileName;

    private Map<String, List<String>> authorisationGroups = new HashMap<>();

    public String getCentrifugeUrl() {
        return centrifugeUrl;
    }

    public void setCentrifugeUrl(String centrifugeUrl) {
        this.centrifugeUrl = centrifugeUrl;
    }

    public String getElkUrl() {
        return elkUrl;
    }

    public void setElkUrl(String elkUrl) {
        this.elkUrl = elkUrl;
    }

    public Map<String, ConfigStoreProperties> getConfigStore() {
        return configStore;
    }

    public void setConfigStore(Map<String, ConfigStoreProperties> configStore) {
        this.configStore = configStore;
    }

    public Map<String, List<String>> getAuthorisationGroups() {
        return authorisationGroups;
    }

    public void setAuthorisationGroups(Map<String, List<String>> authorisationGroups) {
        this.authorisationGroups = authorisationGroups;
    }

    public Map<String, String> getUiConfigFileName() {
        return uiConfigFileName;
    }

    public void setUiConfigFileName(Map<String, String> uiConfigFileName) {
        this.uiConfigFileName = uiConfigFileName;
    }

    public String getElkTemplatePath() {
        return elkTemplatePath;
    }

    public void setElkTemplatePath(String elkTemplatePath) {
        this.elkTemplatePath = elkTemplatePath;
    }

    public Map<String, String> getTestSpecUiConfigFileName() {
        return testSpecUiConfigFileName;
    }

    public void setTestSpecUiConfigFileName(Map<String, String> testSpecUiConfigFileName) {
        this.testSpecUiConfigFileName = testSpecUiConfigFileName;
    }
}
