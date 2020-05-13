package uk.co.gresearch.siembol.response.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.reinert.jjschema.Attributes;

@Attributes(title = "table formatter evaluator attributes",
        description = "Attributes for formatting markdown table from json object")
public class TableFormatterEvaluatorAttributesDto {
    @JsonProperty("field_name")
    @Attributes(required = true,
            description = "The name of the field in which the computed markdown table will be stored")
    private String fieldName;

    @JsonProperty("table_name")
    @Attributes(required = true, description = "The name of the table")
    private String tableName;

    @JsonProperty("fields_column_name")
    @Attributes(required = true, description = "The name of the column of the generated table with field names")
    private String fieldsColumnName = "Field Name";

    @JsonProperty("values_column_name")
    @Attributes(required = true, description = "The name of the column of the generated table with object values")
    private String valuesColumnName = "Field Value";

    @JsonProperty("field_filter")
    @Attributes(description = "The field filter used for filtering the alert fields in the table")
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

    public FieldFilterDto getFieldFilterDto() {
        return fieldFilterDto;
    }

    public void setFieldFilterDto(FieldFilterDto fieldFilterDto) {
        this.fieldFilterDto = fieldFilterDto;
    }

    public String getFieldsColumnName() {
        return fieldsColumnName;
    }

    public void setFieldsColumnName(String fieldsColumnName) {
        this.fieldsColumnName = fieldsColumnName;
    }

    public String getValuesColumnName() {
        return valuesColumnName;
    }

    public void setValuesColumnName(String valuesColumnName) {
        this.valuesColumnName = valuesColumnName;
    }
}
