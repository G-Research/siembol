package uk.co.gresearch.siembol.parsers.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.reinert.jjschema.Attributes;
/**
 * A data transfer object for representing column filter in a csv extractor
 *
 * <p>This class is used for json (de)serialisation of a column filter and
 * for generating json schema from this class using annotations.
 *
 * @author  Marian Novotny
 * @see com.github.reinert.jjschema.Attributes
 * @see com.fasterxml.jackson.annotation.JsonProperty
 */
@Attributes(title = "column filter", description = "Filter that matches string on a provided index")
public class ColumnFilterDto {
    @Attributes(required = true, description = "Index in the array started from 0")
    private Integer index;
    @JsonProperty("required_value")
    @Attributes(required = true, description = "Required string on the index position")
    private String requiredValue;

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public String getRequiredValue() {
        return requiredValue;
    }

    public void setRequiredValue(String requiredValue) {
        this.requiredValue = requiredValue;
    }
}
