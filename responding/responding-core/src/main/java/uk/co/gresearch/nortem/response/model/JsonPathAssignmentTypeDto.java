package uk.co.gresearch.nortem.response.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum JsonPathAssignmentTypeDto {
    @JsonProperty("match_always") MATCH_ALWAYS("match_always"),
    @JsonProperty("no_match_when_empty") NO_MATCH_WHEN_EMPTY("no_match_when_empty"),
    @JsonProperty("error_match_when_empty") ERROR_MATCH_WHEN_EMPTY("error_match_when_empty");
    private final String name;

    JsonPathAssignmentTypeDto(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
