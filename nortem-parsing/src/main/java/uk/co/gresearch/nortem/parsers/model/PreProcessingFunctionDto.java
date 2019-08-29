package uk.co.gresearch.nortem.parsers.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.reinert.jjschema.Attributes;

@Attributes(title = "preprocessing function", description = "Postprocessing function")
public enum PreProcessingFunctionDto {
    @JsonProperty("string_replace") STRING_REPLACE("string_replace"),
    @JsonProperty("string_replace_all") STRING_REPLACE_ALL("string_replace_all");

    private final String name;

    PreProcessingFunctionDto(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

}
