package uk.co.gresearch.siembol.parsers.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.reinert.jjschema.Attributes;
/**
 * A data transfer object for representing attributes for a pre-processing function used in a parser extractor
 *
 * <p>This class is used for json (de)serialisation of a pre-processing function used in a parser extractor and
 * for generating json schema from this class using annotations.
 *
 * @author  Marian Novotny
 * @see com.github.reinert.jjschema.Attributes
 * @see com.fasterxml.jackson.annotation.JsonProperty
 */
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
