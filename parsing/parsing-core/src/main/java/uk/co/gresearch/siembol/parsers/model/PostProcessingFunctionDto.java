package uk.co.gresearch.siembol.parsers.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.reinert.jjschema.Attributes;
/**
 * A data transfer object for representing attributes for a post-processing function used in a parser extractor
 *
 * <p>This class is used for json (de)serialisation of a post-processing function used in a parser extractor and
 * for generating json schema from this class using annotations.
 *
 * @author  Marian Novotny
 * @see com.github.reinert.jjschema.Attributes
 * @see com.fasterxml.jackson.annotation.JsonProperty
 */
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
