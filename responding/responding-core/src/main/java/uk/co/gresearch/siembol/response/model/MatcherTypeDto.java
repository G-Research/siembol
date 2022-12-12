package uk.co.gresearch.siembol.response.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.reinert.jjschema.Attributes;
/**
 * An enum for representing a matcher type
 *
 * <p>This enum is used for json (de)serialisation of a matcher type used in a matching evaluator.
 *
 * @author  Marian Novotny
 * @see com.fasterxml.jackson.annotation.JsonProperty
 * @see #REGEX_MATCH
 * @see #IS_IN_SET
 */
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
