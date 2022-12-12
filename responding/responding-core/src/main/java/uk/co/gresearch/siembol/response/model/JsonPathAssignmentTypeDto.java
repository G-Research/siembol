package uk.co.gresearch.siembol.response.model;

import com.fasterxml.jackson.annotation.JsonProperty;
/**
 * An enum for representing a json path assignment type
 *
 * <p>This enum is used for json (de)serialisation of a json path assignment type used in an assignment evaluator.
 *
 * @author  Marian Novotny
 * @see com.fasterxml.jackson.annotation.JsonProperty
 * @see #MATCH_ALWAYS
 * @see #NO_MATCH_WHEN_EMPTY
 * @see #ERROR_MATCH_WHEN_EMPTY
 */
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
