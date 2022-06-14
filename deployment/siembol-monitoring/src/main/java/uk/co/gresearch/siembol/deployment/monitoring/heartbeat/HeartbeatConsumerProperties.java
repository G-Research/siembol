package uk.co.gresearch.siembol.deployment.monitoring.heartbeat;

import java.util.Map;

public class HeartbeatConsumerProperties {
    private String inputTopic;
    private Map<String, Object> kafkaProperties;

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
