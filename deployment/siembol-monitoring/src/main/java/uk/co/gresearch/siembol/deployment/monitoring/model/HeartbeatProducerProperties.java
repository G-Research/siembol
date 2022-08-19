package uk.co.gresearch.siembol.deployment.monitoring.model;

import java.util.Map;
import java.util.Properties;

public class HeartbeatProducerProperties {
    private String outputTopic;
    private Map<String, Object> kafkaProperties;

    public String getOutputTopic() {
        return outputTopic;
    }

    public void setOutputTopic(String outputTopic) {
        this.outputTopic = outputTopic;
    }

    public Properties getKafkaProperties() {
        var props = new Properties();
        props.putAll(kafkaProperties);
        return props;
    }

    public void setKafkaProperties(Map<String, Object> kafkaProperties) {
        this.kafkaProperties = kafkaProperties;
    }
}
