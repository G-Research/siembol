package uk.co.gresearch.siembol.parsers.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.reinert.jjschema.Attributes;

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