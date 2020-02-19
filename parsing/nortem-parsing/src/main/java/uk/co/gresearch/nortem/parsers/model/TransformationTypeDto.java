package uk.co.gresearch.nortem.parsers.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.reinert.jjschema.Attributes;

@Attributes(title = "transformation type", description = "The type of transformation")
public enum  TransformationTypeDto {
    @JsonProperty("field_name_string_replace") FIELD_NAME_STRING_REPLACE("field_name_string_replace"),
    @JsonProperty("field_name_string_replace_all") FIELD_NAME_STRING_REPLACE_ALL("field_name_string_replace_all"),
    @JsonProperty("field_name_string_delete_all") FIELD_NAME_STRING_DELETE_ALL("field_name_string_delete_all"),
    @JsonProperty("field_name_lowercase") FIELD_NAME_LOWERCASE("field_name_lowercase"),
    @JsonProperty("field_name_uppercase") FIELD_NAME_UPPERCASE("field_name_uppercase"),
    @JsonProperty("rename_fields") RENAME_FIELDS("rename_fields"),
    @JsonProperty("delete_fields") DELETE_FIELDS("delete_fields"),
    @JsonProperty("trim_value") TRIM_VALUE("trim_value"),
    @JsonProperty("lowercase_value") LOWERCASE_VALUE("lowercase_value"),
    @JsonProperty("uppercase_value") UPPERCASE_VALUE("uppercase_value"),
    @JsonProperty("chomp_value") CHOMP_VALUE("chomp_value"),
    @JsonProperty("filter_message") FILTER_MESSAGE("filter_message");

    private final String name;
    TransformationTypeDto(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
