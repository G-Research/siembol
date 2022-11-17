package uk.co.gresearch.siembol.parsers.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.reinert.jjschema.Attributes;
/**
 * An enum for representing a transformation type
 *
 * <p>This enum is used for json (de)serialisation of a transformation type.
 *
 * @author  Marian Novotny
 * @see com.fasterxml.jackson.annotation.JsonProperty
 * @see #FIELD_NAME_STRING_REPLACE
 * @see #FIELD_NAME_STRING_REPLACE_ALL
 * @see #FIELD_NAME_STRING_DELETE_ALL
 * @see #FIELD_NAME_CHANGE_CASE
 * @see #RENAME_FIELDS
 * @see #DELETE_FIELDS
 * @see #TRIM_VALUE
 * @see #LOWERCASE_VALUE
 * @see #UPPERCASE_VALUE
 * @see #CHOMP_VALUE
 * @see #FILTER_MESSAGE
 *
 */
@Attributes(title = "transformation type", description = "The type of transformation")
public enum  TransformationTypeDto {
    @JsonProperty("field_name_string_replace") FIELD_NAME_STRING_REPLACE("field_name_string_replace"),
    @JsonProperty("field_name_string_replace_all") FIELD_NAME_STRING_REPLACE_ALL("field_name_string_replace_all"),
    @JsonProperty("field_name_string_delete_all") FIELD_NAME_STRING_DELETE_ALL("field_name_string_delete_all"),
    @JsonProperty("field_name_change_case") FIELD_NAME_CHANGE_CASE("field_name_change_case"),
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
