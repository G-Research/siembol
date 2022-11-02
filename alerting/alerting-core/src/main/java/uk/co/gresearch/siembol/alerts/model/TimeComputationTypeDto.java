package uk.co.gresearch.siembol.alerts.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.reinert.jjschema.Attributes;
/**
 * An enum for representing a time computation type used in correlation attributes
 *
 * <p>This enum is used for json (de)serialisation of time computation type.
 *
 * @author  Marian Novotny
 * @see com.fasterxml.jackson.annotation.JsonProperty
 * @see CorrelationAttributesDto
 * @see #EVENT_TIME
 * @see #PROCESSING_TIME
 */
@Attributes(title = "time computation type", description = "Type of time computation")
public enum TimeComputationTypeDto {
    @JsonProperty("event_time") EVENT_TIME("event_time"),
    @JsonProperty("processing_time") PROCESSING_TIME("processing_time");

    private final String name;
    TimeComputationTypeDto(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

}
