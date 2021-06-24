package uk.co.gresearch.siembol.response.stream.rest.application;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.context.annotation.Configuration;
import uk.co.gresearch.siembol.common.model.ZooKeeperAttributesDto;

import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "siembol-response")
public class ResponseConfigurationProperties {
    private String inputTopic;
    private String errorTopic;
    private Map<String, Object> streamConfig;
    private Boolean inactiveStreamService = false;
    @NestedConfigurationProperty
    private ZooKeeperAttributesDto zookeperAttributes;

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

    public ZooKeeperAttributesDto getZookeperAttributes() {
        return zookeperAttributes;
    }

    public void setZookeperAttributes(ZooKeeperAttributesDto zookeperAttributes) {
        this.zookeperAttributes = zookeperAttributes;
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
}
