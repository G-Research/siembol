package uk.co.gresearch.siembol.alerts.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.reinert.jjschema.Attributes;

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
