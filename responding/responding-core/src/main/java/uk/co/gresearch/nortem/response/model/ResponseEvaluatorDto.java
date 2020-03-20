package uk.co.gresearch.nortem.response.model;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.reinert.jjschema.Attributes;
import com.github.reinert.jjschema.SchemaIgnore;
import uk.co.gresearch.nortem.common.jsonschema.JsonRawStringDto;

@Attributes(title = "response evaluator", description = "Response evaluator used in response rules")
public class ResponseEvaluatorDto {
    @JsonProperty("evaluator_type")
    @Attributes(required = true, description = "Type of response evaluator")
    private String evaluatorType;

    @JsonProperty("evaluator_attributes")
    @Attributes(required = true, description = "The attributes of the evaluator")
    private JsonRawStringDto evaluatorAttributes;

    @JsonIgnore
    @SchemaIgnore
    private String evaluatorAttributesContent;

    @JsonSetter
    public void setEvaluatorAttributes(JsonNode evaluatorAttributes) {
        this.evaluatorAttributesContent = evaluatorAttributes.toString();
    }

    public String getEvaluatorType() {
        return evaluatorType;
    }

    public void setEvaluatorType(String evaluatorType) {
        this.evaluatorType = evaluatorType;
    }

    public JsonRawStringDto getEvaluatorAttributes() {
        return evaluatorAttributes;
    }

    public void setEvaluatorAttributes(JsonRawStringDto evaluatorAttributes) {
        this.evaluatorAttributes = evaluatorAttributes;
    }

    @JsonIgnore
    public String getEvaluatorAttributesContent() {
        return evaluatorAttributesContent;
    }

}
