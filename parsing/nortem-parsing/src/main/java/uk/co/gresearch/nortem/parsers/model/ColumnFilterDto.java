package uk.co.gresearch.nortem.parsers.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.reinert.jjschema.Attributes;

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
