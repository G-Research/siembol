package uk.co.gresearch.siembol.alerts.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.reinert.jjschema.Attributes;

@Attributes(title = "time computation type", description = "Type of time computation")
public enum MatcherTypeDto {
    @JsonProperty("REGEX_MATCH") REGEX_MATCH("REGEX_MATCH"),
    @JsonProperty("IS_IN_SET") IS_IN_SET("IS_IN_SET"),
    @JsonProperty("COMPOSITE_AND") COMPOSITE_AND("COMPOSITE_AND"),
    @JsonProperty("COMPOSITE_OR") COMPOSITE_OR("COMPOSITE_OR");
    private final String name;

    MatcherTypeDto(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
