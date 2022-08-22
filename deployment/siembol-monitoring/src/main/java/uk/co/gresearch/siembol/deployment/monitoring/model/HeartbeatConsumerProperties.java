package uk.co.gresearch.siembol.deployment.monitoring.model;

import java.util.List;
import java.util.Map;
import uk.co.gresearch.siembol.common.constants.ServiceType;

public class HeartbeatConsumerProperties {
    private List<ServiceType> enabledServices;
    private String inputTopic;
    private Map<String, Object> kafkaProperties;

    public List<ServiceType> getEnabledServices() {
        return enabledServices;
    }

    public void setEnabledServices(List<ServiceType> enabledServices) {
        this.enabledServices = enabledServices;
    }

    public String getInputTopic() {
        return inputTopic;
    }

    public void setInputTopic(String inputTopic) {
        this.inputTopic = inputTopic;
    }

    public Map<String, Object> getKafkaProperties() {
        return kafkaProperties;
    }

    public void setKafkaProperties(Map<String, Object> kafkaProperties) {
        this.kafkaProperties = kafkaProperties;
    }

}
