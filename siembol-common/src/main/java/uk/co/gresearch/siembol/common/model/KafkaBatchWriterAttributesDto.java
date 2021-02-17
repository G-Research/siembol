package uk.co.gresearch.siembol.common.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.reinert.jjschema.Attributes;
import uk.co.gresearch.siembol.common.jsonschema.JsonRawStringDto;

@Attributes(title = "kafka batch writer attributes", description = "Attributes for storm configuration")
public class KafkaBatchWriterAttributesDto {
    @JsonProperty("batch.size")
    @Attributes(required = true, description = "The max size of batch for producing messages", minimum = 1)
    private Integer batchSize = 1;
    @JsonProperty("producer.properties")
    @Attributes(required = true, description = "Defines kafka producer properties")
    private JsonRawStringDto producerProperties;

    public Integer getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(Integer batchSize) {
        this.batchSize = batchSize;
    }

    public JsonRawStringDto getProducerProperties() {
        return producerProperties;
    }

    public void setProducerProperties(JsonRawStringDto producerProperties) {
        this.producerProperties = producerProperties;
    }
}
