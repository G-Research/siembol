package uk.co.gresearch.nortem.response.common;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum ResponseEvaluationResult {
    @JsonProperty("match") MATCH("match"),
    @JsonProperty("no_match") NO_MATCH("no_match"),
    @JsonProperty("filtered") FILTERED("filtered");

    private final String name;

    ResponseEvaluationResult(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
