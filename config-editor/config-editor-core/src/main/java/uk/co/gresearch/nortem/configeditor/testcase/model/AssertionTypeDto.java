package uk.co.gresearch.nortem.configeditor.testcase.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum AssertionTypeDto {
    @JsonProperty("path_and_value_matches") PATH_AND_VALUE_MATCHES("path_and_value_matches"),
    @JsonProperty("only_if_path_exists") ONLY_IF_PATH_EXISTS("only_if_path_exists");

    private final String name;

    AssertionTypeDto(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
