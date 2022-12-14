package uk.co.gresearch.siembol.configeditor.testcase.model;

import com.fasterxml.jackson.annotation.JsonProperty;
/**
 * An enum for representing an assertion type
 *
 * <p>This enum is used for json (de)serialisation of an assertion type used in a test case.
 *
 * @author  Marian Novotny
 * @see com.fasterxml.jackson.annotation.JsonProperty
 */
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
