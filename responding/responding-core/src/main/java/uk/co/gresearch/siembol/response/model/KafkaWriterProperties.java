package uk.co.gresearch.siembol.response.model;

import java.util.Map;
import java.util.Properties;
/**
 * A data transfer object for representing kafka writer properties
 *
 * <p>This class is used for representing kafka writer properties.
 *
 * @author  Marian Novotny
 */
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
