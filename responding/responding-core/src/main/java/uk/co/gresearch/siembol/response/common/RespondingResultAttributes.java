package uk.co.gresearch.siembol.response.common;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.JsonNode;
import uk.co.gresearch.siembol.response.engine.ResponseEngine;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class RespondingResultAttributes {
    private ResponseEvaluationResult result;
    private ResponseAlert alert;
    private Evaluable respondingEvaluator;
    private String message;
    private String attributesSchema;
    private String ruleName;
    @JsonProperty("rules_schema")
    @JsonRawValue
    private String rulesSchema;
    @JsonProperty("test_schema")
    @JsonRawValue
    private String testSpecificationSchema;
    private String evaluatorType;
    private Integer rulesVersion;
    private Integer numberOfRules;
    @JsonProperty("json_rules")
    @JsonRawValue
    private String jsonRules;
    private Long compiledTime;
    private ResponseEngine responseEngine;
    private List<RespondingEvaluatorFactory> respondingEvaluatorFactories;
    @JsonProperty("test_specification")
    @JsonRawValue
    private String testSpecification;

    public ResponseEvaluationResult getResult() {
        return result;
    }

    public void setResult(ResponseEvaluationResult result) {
        this.result = result;
    }

    public ResponseAlert getAlert() {
        return alert;
    }

    public void setAlert(ResponseAlert alert) {
        this.alert = alert;
    }

    public Evaluable getRespondingEvaluator() {
        return respondingEvaluator;
    }

    public void setRespondingEvaluator(Evaluable respondingEvaluator) {
        this.respondingEvaluator = respondingEvaluator;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getAttributesSchema() {
        return attributesSchema;
    }

    public void setAttributesSchema(String attributesSchema) {
        this.attributesSchema = attributesSchema;
    }

    public List<RespondingEvaluatorFactory> getRespondingEvaluatorFactories() {
        return respondingEvaluatorFactories;
    }

    public void setRespondingEvaluatorFactories(List<RespondingEvaluatorFactory> respondingEvaluatorFactories) {
        this.respondingEvaluatorFactories = respondingEvaluatorFactories;
    }

    public String getRulesSchema() {
        return rulesSchema;
    }

    @JsonSetter("rules_schema")
    public void setTestSchema(JsonNode rulesSchema) {
        this.rulesSchema = rulesSchema.toString();
    }

    public void setRulesSchema(String rulesSchema) {
        this.rulesSchema = rulesSchema;
    }

    public String getEvaluatorType() {
        return evaluatorType;
    }

    public void setEvaluatorType(String evaluatorType) {
        this.evaluatorType = evaluatorType;
    }

    public ResponseEngine getResponseEngine() {
        return responseEngine;
    }

    public void setResponseEngine(ResponseEngine responseEngine) {
        this.responseEngine = responseEngine;
    }

    public Integer getRulesVersion() {
        return rulesVersion;
    }

    public void setRulesVersion(Integer rulesVersion) {
        this.rulesVersion = rulesVersion;
    }

    public Integer getNumberOfRules() {
        return numberOfRules;
    }

    public void setNumberOfRules(Integer numberOfRules) {
        this.numberOfRules = numberOfRules;
    }

    @JsonSetter("json_rules")
    public void setJsonRules(JsonNode jsonRules) {
        this.jsonRules = jsonRules.toString();
    }

    public String getJsonRules() {
        return jsonRules;
    }

    public void setJsonRules(String jsonRules) {
        this.jsonRules = jsonRules;
    }

    public Long getCompiledTime() {
        return compiledTime;
    }

    public void setCompiledTime(Long compiledTime) {
        this.compiledTime = compiledTime;
    }

    public String getTestSpecificationSchema() {
        return testSpecificationSchema;
    }

    @JsonSetter("test_schema")
    public void setTestSpecificationSchema(JsonNode testSpecificationSchema) {
        this.testSpecificationSchema = testSpecificationSchema.toString();
    }

    public void setTestSpecificationSchema(String testSpecificationSchema) {
        this.testSpecificationSchema = testSpecificationSchema;
    }

    @JsonSetter("test_specification")
    public void setTestSpecification(JsonNode testSpecification) {
        this.testSpecification = testSpecification.toString();
    }

    public String getTestSpecification() {
        return testSpecification;
    }

    public void setTestSpecification(String testSpecification) {
        this.testSpecification = testSpecification;
    }

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }
}
