package uk.co.gresearch.siembol.alerts.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.reinert.jjschema.Attributes;
/**
 * An enum for representing a matcher type used in alerting rules
 *
 * <p>This enum is used for json (de)serialisation of matcher type.
 *
 * @author  Marian Novotny
 * @see com.fasterxml.jackson.annotation.JsonProperty
 * @see MatcherDto
 * @see #REGEX_MATCH
 * @see #IS_IN_SET
 * @see #CONTAINS
 * @see #COMPOSITE_AND
 * @see #COMPOSITE_OR
 * @see #NUMERIC_COMPARE
 */
@Attributes(title = "time computation type", description = "Type of time computation")
public enum MatcherTypeDto {
    @JsonProperty("REGEX_MATCH") REGEX_MATCH("REGEX_MATCH"),
    @JsonProperty("IS_IN_SET") IS_IN_SET("IS_IN_SET"),
    @JsonProperty("CONTAINS") CONTAINS("CONTAINS"),
    @JsonProperty("COMPOSITE_AND") COMPOSITE_AND("COMPOSITE_AND"),
    @JsonProperty("COMPOSITE_OR") COMPOSITE_OR("COMPOSITE_OR"),
    @JsonProperty("NUMERIC_COMPARE") NUMERIC_COMPARE("NUMERIC_COMPARE");
    private final String name;

    MatcherTypeDto(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
