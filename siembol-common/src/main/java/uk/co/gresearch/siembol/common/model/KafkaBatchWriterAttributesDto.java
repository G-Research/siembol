package uk.co.gresearch.siembol.common.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.reinert.jjschema.Attributes;

/**
 * A data transfer object for representing a kafka writer attributes
 *
 * <p>This class is used for json (de)serialisation of attributes for a kafka writer and
 * for generating json schema from this class using annotations.
 *
 * @author  Marian Novotny
 * @see com.github.reinert.jjschema.Attributes
 * @see com.fasterxml.jackson.annotation.JsonProperty
 * @see JsonRawStringDto
 */
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
