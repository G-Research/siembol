package uk.co.gresearch.nortem.parsers.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.reinert.jjschema.Attributes;

@Attributes(title = "extractor type", description = "Type of the parser extractor")
public enum ExtractorTypeDto {
    @JsonProperty("pattern_extractor") PATTERN_EXTRACTOR("pattern_extractor"),
    @JsonProperty("key_value_extractor") KEY_VALUE_EXTRACTOR("key_value_extractor"),
    @JsonProperty("csv_extractor") CSV_EXTRACTOR("csv_extractor"),
    @JsonProperty("json_extractor") JSON_EXTRACTOR("json_extractor"),
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
