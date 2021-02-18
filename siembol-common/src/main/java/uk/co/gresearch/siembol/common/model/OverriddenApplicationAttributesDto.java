package uk.co.gresearch.siembol.common.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.reinert.jjschema.Attributes;

import java.io.Serializable;

@Attributes(title = "overridden application attributes", description = "Storm parsing application attributes")
public class OverriddenApplicationAttributesDto implements Serializable {
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
