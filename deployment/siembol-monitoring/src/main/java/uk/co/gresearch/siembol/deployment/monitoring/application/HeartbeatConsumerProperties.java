package uk.co.gresearch.siembol.deployment.monitoring.application;

import java.util.Map;

public class HeartbeatConsumerProperties {
    private String inputTopic;
    private String errorTopic;
    private Map<String, Object> kafkaProperties;

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

    public Map<String, Object> getKafkaProperties() {
        return kafkaProperties;
    }

    public void setKafkaProperties(Map<String, Object> kafkaProperties) {
        this.kafkaProperties = kafkaProperties;
    }

}
