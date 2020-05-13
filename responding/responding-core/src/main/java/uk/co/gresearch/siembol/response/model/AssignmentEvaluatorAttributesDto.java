package uk.co.gresearch.siembol.response.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.reinert.jjschema.Attributes;

@Attributes(title = "json path assignment", description = "json path assignment")
public class AssignmentEvaluatorAttributesDto {
    @JsonProperty("assignment_type")
    @Attributes(required = true, description = "The type of the assignment based on json path evaluation")
    private JsonPathAssignmentTypeDto assignmentType;

    @JsonProperty("field_name")
    @Attributes(required = true,
            description = "The name of the field in which the non empty result of the json path will be stored")
    private String fieldName;

    @JsonProperty("json_path")
    @Attributes(required = true, description = "Json path for evaluation")
    private String  jsonPath;

    public JsonPathAssignmentTypeDto getAssignmentType() {
        return assignmentType;
    }

    public void setAssignmentType(JsonPathAssignmentTypeDto assignmentType) {
        this.assignmentType = assignmentType;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getJsonPath() {
        return jsonPath;
    }

    public void setJsonPath(String jsonPath) {
        this.jsonPath = jsonPath;
    }
}
