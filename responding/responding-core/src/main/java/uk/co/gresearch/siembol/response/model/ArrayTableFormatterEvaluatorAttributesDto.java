package uk.co.gresearch.siembol.response.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.reinert.jjschema.Attributes;

@Attributes(title = "format table from array evaluator attributes",
        description = "Attributes for formatting markdown table from json array")
public class ArrayTableFormatterEvaluatorAttributesDto {
    @JsonProperty("field_name")
    @Attributes(required = true,
            description = "The name of the field in which the string that contains markdown table will be stored")
    private String fieldName;

    @JsonProperty("table_name")
    @Attributes(required = true, description = "The name of the table")
    private String tableName;

    @JsonProperty("array_field")
    @Attributes(required = true, description = "The array field of the alert that will be printed in the table")
    private String arrayField;

    @JsonProperty("field_filter")
    @Attributes(description = "The field filter used for filtering the fields from the array in the table")
    private FieldFilterDto fieldFilterDto;

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getArrayField() {
        return arrayField;
    }

    public void setArrayField(String arrayField) {
        this.arrayField = arrayField;
    }

    public FieldFilterDto getFieldFilterDto() {
        return fieldFilterDto;
    }

    public void setFieldFilterDto(FieldFilterDto fieldFilterDto) {
        this.fieldFilterDto = fieldFilterDto;
    }
}
