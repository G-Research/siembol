package uk.co.gresearch.siembol.configeditor.service.alerts.sigma.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SigmaRuleDto {
    private String title;
    @JsonProperty("logsource")
    private SigmaLogSourceDto logSource;
    private SigmaDetectionDto detection;

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

    public SigmaDetectionDto getDetection() {
        return detection;
    }

    public void setDetection(SigmaDetectionDto detection) {
        this.detection = detection;
    }
}
