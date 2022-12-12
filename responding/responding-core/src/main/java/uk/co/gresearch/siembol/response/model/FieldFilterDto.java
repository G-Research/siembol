package uk.co.gresearch.siembol.response.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.reinert.jjschema.Attributes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
/**
 * A data transfer object for representing a field filter
 *
 * <p>This class is used for json (de)serialisation of a field filter and
 * for generating json schema from this class using annotations.
 *
 * @author  Marian Novotny
 * @see com.github.reinert.jjschema.Attributes
 * @see com.fasterxml.jackson.annotation.JsonProperty
 */
@Attributes(title = "field filter", description = "Field filter is used for filtering alert fields")
public class FieldFilterDto {
    @JsonProperty("including_fields")
    @Attributes(minItems = 1, description =
            "The list of regular expression patterns for including fields")
    private List<String> includingFields =
            new ArrayList<>(Arrays.asList(".*"));

    @JsonProperty("excluding_fields")
    @Attributes(description =
            "The list of regular expression patterns for excluding fields")
    List<String> excludingFields = new ArrayList<>();

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
