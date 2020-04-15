package uk.co.gresearch.siembol.response.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.reinert.jjschema.Attributes;

@Attributes(title = "matcher type", description = "Type of matcher")
public enum MatcherTypeDto {
    @JsonProperty("REGEX_MATCH") REGEX_MATCH("REGEX_MATCH"),
    @JsonProperty("IS_IN_SET") IS_IN_SET("IS_IN_SET");
    private final String name;

    MatcherTypeDto(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
