package uk.co.gresearch.siembol.configeditor.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ConfigEditorAttributes {
    private String exception;
    private String message;
    @JsonProperty("rules_schema")
    @JsonRawValue
    private String rulesSchema;

    @JsonProperty("pull_request_pending")
    private Boolean pendingPullRequest;

    @JsonProperty("pull_request_url")
    private String pullRequestUrl;

    @JsonProperty("user_name")
    private String userName;

    private List<ConfigEditorFile> files;

    @JsonProperty("rules_version")
    private Integer rulesVersion;

    @JsonProperty("rules_repositories")
    ConfigEditorRepositories rulesRepositories;
	
	@JsonProperty("fields")
    @JsonRawValue
    private String fields;

    @JsonProperty("template_fields")
    private List<TemplateField> templateFields;

    @JsonProperty("sensor_template_fields")
    private List<SensorTemplateFields> sensorTemplateFields;

    @JsonProperty("test_result_output")
    private String testResultOutput;

    @JsonProperty("test_result_complete")
    private Boolean testResultComplete;

    @JsonProperty("test_case_result")
    private ConfigEditorTestCaseResult testCaseResult;

    @JsonProperty("test_schema")
    @JsonRawValue
    private String testSchema;

    @JsonProperty("test_result_raw_output")
    @JsonRawValue
    private String testResultRawOutput;

    @JsonProperty("test_specification")
    @JsonRawValue
    private String testSpecification;

    private String event;

    private List<ConfigEditorService> services;

    public String getException() {
        return exception;
    }

    public void setException(String exception) {
        this.exception = exception;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getRulesSchema() {
        return rulesSchema;
    }

    public void setRulesSchema(String rulesSchema) {
        this.rulesSchema = rulesSchema;
    }

    public List<ConfigEditorFile> getFiles() {
        return files;
    }

    public void setFiles(List<ConfigEditorFile> files) {
        this.files = files;
    }

    public Boolean getPendingPullRequest() {
        return pendingPullRequest;
    }

    public void setPendingPullRequest(Boolean pendingPullRequest) {
        this.pendingPullRequest = pendingPullRequest;
    }

    public String getPullRequestUrl() {
        return pullRequestUrl;
    }

    public void setPullRequestUrl(String pullRequestUrl) {
        this.pullRequestUrl = pullRequestUrl;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Integer getRulesVersion() {
        return rulesVersion;
    }

    public void setRulesVersion(Integer rulesVersion) {
        this.rulesVersion = rulesVersion;
    }

    public ConfigEditorRepositories getRulesRepositories() {
        return rulesRepositories;
    }

    public void setRulesRepositories(ConfigEditorRepositories rulesRepositories) {
        this.rulesRepositories = rulesRepositories;
    }

    public List<TemplateField> getTemplateFields() {
        return templateFields;
    }

    public void setTemplateFields(List<TemplateField> templateFields) {
        this.templateFields = templateFields;
    }

    public List<SensorTemplateFields> getSensorTemplateFields() {
        return sensorTemplateFields;
    }

    public void setSensorTemplateFields(List<SensorTemplateFields> sensorTemplateFields) {
        this.sensorTemplateFields = sensorTemplateFields;
    }
	
	 public String getFields() {
        return fields;
    }

    public void setFields(String fields) {
        this.fields = fields;
    }

    public String getTestResultOutput() {
        return testResultOutput;
    }

    public void setTestResultOutput(String testResultOutput) {
        this.testResultOutput = testResultOutput;
    }

    public Boolean getTestResultComplete() {
        return testResultComplete;
    }

    public void setTestResultComplete(Boolean testResultComplete) {
        this.testResultComplete = testResultComplete;
    }

    public String getEvent() {
        return event;
    }

    @JsonSetter("event")
    public void setEvent(JsonNode event) {
        this.event = event.toString();
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public List<ConfigEditorService> getServices() {
        return services;
    }

    public void setServices(List<ConfigEditorService> services) {
        this.services = services;
    }

    public ConfigEditorTestCaseResult getTestCaseResult() {
        return testCaseResult;
    }

    public void setTestCaseResult(ConfigEditorTestCaseResult testCaseResult) {
        this.testCaseResult = testCaseResult;
    }

    public String getTestSchema() {
        return testSchema;
    }

    public void setTestSchema(String testSchema) {
        this.testSchema = testSchema;
    }

    public String getTestResultRawOutput() {
        return testResultRawOutput;
    }

    public void setTestResultRawOutput(String testResultRawOutput) {
        this.testResultRawOutput = testResultRawOutput;
    }

    public String getTestSpecification() {
        return testSpecification;
    }

    @JsonSetter("test_specification")
    public void setTestSpecification(JsonNode testSpecification) {
        this.testSpecification = testSpecification.toString();
    }
}
