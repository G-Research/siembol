package uk.co.gresearch.siembol.response.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.reinert.jjschema.Attributes;
/**
 * An enum for representing an array reducer type
 *
 * <p>This enum is used for json (de)serialisation of an array reducer type used in an array reducer evaluator.
 *
 * @author  Marian Novotny
 * @see com.fasterxml.jackson.annotation.JsonProperty
 * @see #FIRST_FIELD
 * @see #CONCATENATE_FIELDS
 */
@Attributes(title = "array reducer type", description = "Type of array reducer")
public enum  ArrayReducerTypeDto {
    @JsonProperty("first_field") FIRST_FIELD("first_field"),
    @JsonProperty("concatenate_fields") CONCATENATE_FIELDS("concatenate_fields");

    private final String name;

    ArrayReducerTypeDto(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
