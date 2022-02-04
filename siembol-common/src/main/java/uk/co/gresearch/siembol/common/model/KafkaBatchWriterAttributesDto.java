package uk.co.gresearch.siembol.common.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.reinert.jjschema.Attributes;
import uk.co.gresearch.siembol.common.jsonschema.JsonRawStringDto;

@JsonIgnoreProperties(ignoreUnknown = true)
@Attributes(title = "kafka batch writer attributes", description = "Attributes for storm configuration")
public class KafkaBatchWriterAttributesDto {
    @JsonProperty("producer.properties")
    @Attributes(required = true, description = "Defines kafka producer properties")
    private JsonRawStringDto producerProperties;

    public JsonRawStringDto getProducerProperties() {
        return producerProperties;
    }

    public void setProducerProperties(JsonRawStringDto producerProperties) {
        this.producerProperties = producerProperties;
    }
}
