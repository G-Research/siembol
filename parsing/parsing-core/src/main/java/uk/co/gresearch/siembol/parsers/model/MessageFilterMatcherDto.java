package uk.co.gresearch.siembol.parsers.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.reinert.jjschema.Attributes;
/**
 * A data transfer object for representing attributes for a message filter matcher
 *
 * <p>This class is used for json (de)serialisation of a message filter matcher attributes and
 * for generating json schema from this class using annotations.
 *
 * @author  Marian Novotny
 * @see com.github.reinert.jjschema.Attributes
 * @see com.fasterxml.jackson.annotation.JsonProperty
 */
@Attributes(title = "message filter matcher", description = "Specification for message filter matcher")
public class MessageFilterMatcherDto {
    @JsonProperty("field_name")
    @Attributes(required = true, description = "The name of the field for matching")
    private String fieldName;
    @Attributes(required = true, description = "Regular expression for matching the field value")
    private String pattern;
    @Attributes(description = "The matcher is negated")
    private Boolean negated = false;


    public Boolean getNegated() {
        return negated;
    }

    public void setNegated(Boolean negated) {
        this.negated = negated;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }
}
