package uk.co.gresearch.siembol.parsers.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.reinert.jjschema.Attributes;

import java.util.List;
/**
 * A data transfer object for representing attributes for fields filter transformation
 *
 * <p>This class is used for json (de)serialisation of a fields filter transformation attributes and
 * for generating json schema from this class using annotations.
 *
 * @author  Marian Novotny
 * @see com.github.reinert.jjschema.Attributes
 * @see com.fasterxml.jackson.annotation.JsonProperty
 */
@Attributes(title = "fields filter", description = "Specification for fields filter")
public class FieldsFilterDto {
    @JsonProperty("including_fields")
    @Attributes(required = true, description = "Including patterns of the filter", minItems = 1)
    private List<String> includingFields;
    @JsonProperty("excluding_fields")
    @Attributes(description = "Excluding patterns of the filter", minItems = 1)
    private List<String> excludingFields;

    public List<String> getIncludingFields() {
        return includingFields;
    }

    public void setIncludingFields(List<String> includingFields) {
        this.includingFields = includingFields;
    }

    public List<String> getExcludingFields() {
        return excludingFields;
    }

    public void setExcludingFields(List<String> excludingFields) {
        this.excludingFields = excludingFields;
    }
}
