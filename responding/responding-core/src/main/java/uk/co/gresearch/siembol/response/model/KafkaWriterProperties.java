package uk.co.gresearch.siembol.response.model;

import java.util.Map;
import java.util.Properties;

public class KafkaWriterProperties {
    private Map<String, Object> producer;

    public Map<String, Object> getProducer() {
        return producer;
    }

    public void setProducer(Map<String, Object> producer) {
        this.producer = producer;
    }

    public Properties getProducerProperties() {
        var props = new Properties();
        props.putAll(producer);
        return props;
    }
}
