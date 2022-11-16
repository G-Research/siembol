package uk.co.gresearch.siembol.parsers.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.reinert.jjschema.Attributes;
/**
 * An enum for representing a case type used in parsing
 *
 * <p>This enum is used for json (de)serialisation of case type.
 *
 * @author  Marian Novotny
 * @see com.fasterxml.jackson.annotation.JsonProperty
 * @see #LOWERCASE
 * @see #UPPERCASE
 *
 */
@Attributes(title = "case type", description = "The type of case")
public enum  CaseTypeDto {
    @JsonProperty("lowercase") LOWERCASE("lowercase"),
    @JsonProperty("uppercase") UPPERCASE("uppercase");

    private final String name;
    CaseTypeDto(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}