package uk.co.gresearch.siembol.response.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.reinert.jjschema.Attributes;

@Attributes(title = "array reducer attributes",
        description = "Attributes for reducing fields form an array")
public class ArrayReducerEvaluatorAttributesDto {
    @JsonProperty("array_reducer_type")
    @Attributes(required = true,
            description = "The type of the array reducer")
    private ArrayReducerTypeDto arrayReducerType = ArrayReducerTypeDto.FIRST_FIELD;

    @JsonProperty("array_field")
    @Attributes(required = true,
            description = "The field name of the array that will be used for computing fields")
    private String arrayField;

    @JsonProperty("prefix_name")
    @Attributes(required = true,
            description = "The prefix for creating field names where the results will be stored")
    private String prefixName;

    @JsonProperty("field_name_delimiter")
    @Attributes(required = true,
            description = "The delimiter that is used for generating field name from the prefix and an array field name")
    private String fieldNameDelimiter = "_";

    @JsonProperty("field_filter")
    @Attributes(required = true, description = "The field filter used for specifying fields for computation")
    FieldFilterDto fieldFilter = new FieldFilterDto();

    public ArrayReducerTypeDto getArrayReducerType() {
        return arrayReducerType;
    }

    public void setArrayReducerType(ArrayReducerTypeDto arrayReducerType) {
        this.arrayReducerType = arrayReducerType;
    }

    public String getArrayField() {
        return arrayField;
    }

    public void setArrayField(String arrayField) {
        this.arrayField = arrayField;
    }

    public String getPrefixName() {
        return prefixName;
    }

    public void setPrefixName(String prefixName) {
        this.prefixName = prefixName;
    }

    public String getFieldNameDelimiter() {
        return fieldNameDelimiter;
    }

    public void setFieldNameDelimiter(String fieldNameDelimiter) {
        this.fieldNameDelimiter = fieldNameDelimiter;
    }

    public FieldFilterDto getFieldFilter() {
        return fieldFilter;
    }

    public void setFieldFilter(FieldFilterDto fieldFilter) {
        this.fieldFilter = fieldFilter;
    }
}
