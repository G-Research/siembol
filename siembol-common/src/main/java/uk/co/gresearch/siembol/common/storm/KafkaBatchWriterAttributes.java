package uk.co.gresearch.siembol.common.storm;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public class KafkaBatchWriterAttributes {
    @JsonProperty("batch.size")
    private Integer batchSize = 1;
    @JsonProperty("producer.properties")
    private Map<String, Object> producerProperties;

    public Integer getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(Integer batchSize) {
        this.batchSize = batchSize;
    }

    public Map<String, Object> getProducerProperties() {
        return producerProperties;
    }

    public void setProducerProperties(Map<String, Object> producerProperties) {
        this.producerProperties = producerProperties;
    }
}
