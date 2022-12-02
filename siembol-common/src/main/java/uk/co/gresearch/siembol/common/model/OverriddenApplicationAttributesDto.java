package uk.co.gresearch.siembol.common.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.reinert.jjschema.Attributes;

import java.io.Serializable;
/**
 * A data transfer object for representing overridden parsing application
 *
 * <p>This class is used for json (de)serialisation of overridden parsing application and
 * for generating json schema from this class using annotations.
 *
 * @author  Marian Novotny
 * @see com.github.reinert.jjschema.Attributes
 * @see com.fasterxml.jackson.annotation.JsonProperty
 * @see StormAttributesDto
 * @see KafkaBatchWriterAttributesDto
 */
@Attributes(title = "overridden application attributes", description = "Storm parsing application attributes")
public class OverriddenApplicationAttributesDto implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("application.name")
    @Attributes(description = "The name of the application with the overridden attributes", required = true)
    private String applicationName;
    @Attributes(description = "Settings for kafka batch writer used for the selected application", required = true)
    @JsonProperty("kafka.batch.writer.attributes")
    private KafkaBatchWriterAttributesDto kafkaBatchWriterAttributes;
    @Attributes(description = "Settings for storm attributes used for the selected application", required = true)
    @JsonProperty("storm.attributes")
    private StormAttributesDto stormAttributes;

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public KafkaBatchWriterAttributesDto getKafkaBatchWriterAttributes() {
        return kafkaBatchWriterAttributes;
    }

    public void setKafkaBatchWriterAttributes(KafkaBatchWriterAttributesDto kafkaBatchWriterAttributes) {
        this.kafkaBatchWriterAttributes = kafkaBatchWriterAttributes;
    }

    public StormAttributesDto getStormAttributes() {
        return stormAttributes;
    }

    public void setStormAttributes(StormAttributesDto stormAttributes) {
        this.stormAttributes = stormAttributes;
    }
}
