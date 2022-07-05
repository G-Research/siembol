package uk.co.gresearch.siembol.response.stream.rest.application;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.context.annotation.Configuration;
import uk.co.gresearch.siembol.common.model.ZooKeeperAttributesDto;
import uk.co.gresearch.siembol.response.model.ProvidedEvaluatorsProperties;

import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "siembol-response")
public class ResponseConfigurationProperties {
    private String inputTopic;
    private String errorTopic;
    private Map<String, Object> streamConfig;
    private Boolean inactiveStreamService = false;
    @NestedConfigurationProperty
    private ZooKeeperAttributesDto zookeeperAttributes;
    @NestedConfigurationProperty
    private ProvidedEvaluatorsProperties evaluatorsProperties;
    private int initialisationSleepTimeMs = 1;

    public String getInputTopic() {
        return inputTopic;
    }

    public void setInputTopic(String inputTopic) {
        this.inputTopic = inputTopic;
    }

    public String getErrorTopic() {
        return errorTopic;
    }

    public void setErrorTopic(String errorTopic) {
        this.errorTopic = errorTopic;
    }

    public ZooKeeperAttributesDto getZookeeperAttributes() {
        return zookeeperAttributes;
    }

    public void setZookeeperAttributes(ZooKeeperAttributesDto zookeeperAttributes) {
        this.zookeeperAttributes = zookeeperAttributes;
    }

    public Boolean getInactiveStreamService() {
        return inactiveStreamService;
    }

    public void setInactiveStreamService(Boolean inactiveStreamService) {
        this.inactiveStreamService = inactiveStreamService;
    }

    public Map<String, Object> getStreamConfig() {
        return streamConfig;
    }

    public void setStreamConfig(Map<String, Object> streamConfig) {
        this.streamConfig = streamConfig;
    }

    public ProvidedEvaluatorsProperties getEvaluatorsProperties() {
        return evaluatorsProperties;
    }

    public void setEvaluatorsProperties(ProvidedEvaluatorsProperties evaluatorsProperties) {
        this.evaluatorsProperties = evaluatorsProperties;
    }

    public int getInitialisationSleepTimeMs() {
        return initialisationSleepTimeMs;
    }

    public void setInitialisationSleepTimeMs(int initialisationSleepTimeMs) {
        this.initialisationSleepTimeMs = initialisationSleepTimeMs;
    }
}
