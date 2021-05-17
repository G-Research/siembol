package uk.co.gresearch.siembol.configeditor.service.alerts.sigma;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

public class SigmaRuleDto {
    private String title;
    @JsonProperty("logsource")
    private SigmaLogSourceDto logSource;
    private JsonNode detection;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public SigmaLogSourceDto getLogSource() {
        return logSource;
    }

    public void setLogSource(SigmaLogSourceDto logSource) {
        this.logSource = logSource;
    }

    public JsonNode getDetection() {
        return detection;
    }

    public void setDetection(JsonNode detection) {
        this.detection = detection;
    }
}
