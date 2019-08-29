package uk.co.gresearch.nortem.parsers.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.reinert.jjschema.Attributes;

@Attributes(title = "postprocessing function", description = "Postprocessing function")
public enum PostProcessingFunctionDto {
    @JsonProperty("convert_unix_timestamp") CONVERT_UNIX_TIMESTAMP("convert_unix_timestamp"),
    @JsonProperty("format_timestamp") FORMAT_TIMESTAMP("format_timestamp"),
    @JsonProperty("convert_to_string") CONVERT_TO_STRING("convert_to_string");

    private final String name;

    PostProcessingFunctionDto(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

}
