package uk.co.gresearch.siembol.parsers.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.reinert.jjschema.Attributes;
/**
 * An enum for representing an extractor type used in parsing
 *
 * <p>This enum is used for json (de)serialisation of extractor type.
 *
 * @author  Marian Novotny
 * @see com.fasterxml.jackson.annotation.JsonProperty
 * @see #PATTERN_EXTRACTOR
 * @see #KEY_VALUE_EXTRACTOR
 * @see #CSV_EXTRACTOR
 * @see #JSON_EXTRACTOR
 * @see #JSON_PATH_EXTRACTOR
 * @see #REGEX_SELECT_EXTRACTOR
 *
 */
@Attributes(title = "extractor type", description = "Type of the parser extractor")
public enum ExtractorTypeDto {
    @JsonProperty("pattern_extractor") PATTERN_EXTRACTOR("pattern_extractor"),
    @JsonProperty("key_value_extractor") KEY_VALUE_EXTRACTOR("key_value_extractor"),
    @JsonProperty("csv_extractor") CSV_EXTRACTOR("csv_extractor"),
    @JsonProperty("json_extractor") JSON_EXTRACTOR("json_extractor"),
    @JsonProperty("json_path_extractor") JSON_PATH_EXTRACTOR("json_path_extractor"),
    @JsonProperty("regex_select_extractor") REGEX_SELECT_EXTRACTOR("regex_select_extractor");

    private final String name;

    ExtractorTypeDto(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
