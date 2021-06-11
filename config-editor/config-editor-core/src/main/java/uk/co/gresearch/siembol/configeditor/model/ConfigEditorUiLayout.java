package uk.co.gresearch.siembol.configeditor.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.Map;
import java.util.HashMap;

public class ConfigEditorUiLayout {
    @JsonProperty("config_layout")
    Map<String, JsonNode> configLayout = new HashMap<>();
    @JsonProperty("test_layout")
    Map<String, JsonNode> testLayout = new HashMap<>();
    @JsonProperty("admin_config_layout")
    Map<String, JsonNode> adminConfigLayout = new HashMap<>();
    @JsonProperty("test_case_layout")
    Map<String, JsonNode> testCaseLayout = new HashMap<>();
    @JsonProperty("importers_layout")
    Map<String, JsonNode> importersLayout = new HashMap<>();

    public Map<String, JsonNode> getConfigLayout() {
        return configLayout;
    }

    public void setConfigLayout(Map<String, JsonNode> configLayout) {
        this.configLayout = configLayout;
    }

    public Map<String, JsonNode> getTestLayout() {
        return testLayout;
    }

    public void setTestLayout(Map<String, JsonNode> testLayout) {
        this.testLayout = testLayout;
    }

    public Map<String, JsonNode> getAdminConfigLayout() {
        return adminConfigLayout;
    }

    public void setAdminConfigLayout(Map<String, JsonNode> adminConfigLayout) {
        this.adminConfigLayout = adminConfigLayout;
    }

    public Map<String, JsonNode> getTestCaseLayout() {
        return testCaseLayout;
    }

    public void setTestCaseLayout(Map<String, JsonNode> testCaseLayout) {
        this.testCaseLayout = testCaseLayout;
    }

    public Map<String, JsonNode> getImportersLayout() {
        return importersLayout;
    }

    public void setImportersLayout(Map<String, JsonNode> importersLayout) {
        this.importersLayout = importersLayout;
    }
}
