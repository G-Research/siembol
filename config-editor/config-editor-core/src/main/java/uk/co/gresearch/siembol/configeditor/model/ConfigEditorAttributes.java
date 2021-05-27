package uk.co.gresearch.siembol.configeditor.model;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.JsonNode;
import uk.co.gresearch.siembol.common.model.StormTopologyDto;
import uk.co.gresearch.siembol.configeditor.common.ConfigInfoType;

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

    @JsonProperty("configs_files")
    private List<ConfigEditorFile> configsFiles;

    @JsonProperty("test_cases_files")
    private List<ConfigEditorFile> testCasesFiles;

    @JsonProperty("rules_version")
    private Integer rulesVersion;

    @JsonProperty("config_version")
    private Integer configVersion;

    @JsonProperty("rules_repositories")
    ConfigEditorRepositories rulesRepositories;

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

    private List<ConfigEditorService> services;

    @JsonProperty("admin_config_schema")
    @JsonRawValue
    private String adminConfigSchema;

    private ConfigEditorServiceContext serviceContext;

    private List<StormTopologyDto> topologies;
    @JsonProperty("topology_name")
    private String topologyName;

    @JsonProperty("config_importers")
    private List<ConfigImporterDto> configImporters;
    @JsonProperty("imported_configuration")
    @JsonRawValue
    private String importedConfiguration;
    private String configImporterAttributesSchema;

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

    public void setReleaseVersion(Integer version, ConfigInfoType type) {
        if (type == ConfigInfoType.ADMIN_CONFIG) {
            configVersion = version;
        } else {
            rulesVersion = version;
        }
    }

    public Integer getReleaseVersion(ConfigInfoType type) {
        return type == ConfigInfoType.ADMIN_CONFIG
                ? configVersion : rulesVersion;
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

    public String getAdminConfigSchema() {
        return adminConfigSchema;
    }

    public void setAdminConfigSchema(String adminConfigSchema) {
        this.adminConfigSchema = adminConfigSchema;
    }

    public Integer getConfigVersion() {
        return configVersion;
    }

    public void setConfigVersion(Integer configVersion) {
        this.configVersion = configVersion;
    }

    public ConfigEditorServiceContext getServiceContext() {
        return serviceContext;
    }

    public void setServiceContext(ConfigEditorServiceContext serviceContext) {
        this.serviceContext = serviceContext;
    }

    public void setTestSpecification(String testSpecification) {
        this.testSpecification = testSpecification;
    }

    public List<StormTopologyDto> getTopologies() {
        return topologies;
    }

    public void setTopologies(List<StormTopologyDto> topologies) {
        this.topologies = topologies;
    }

    public String getTopologyName() {
        return topologyName;
    }

    public void setTopologyName(String topologyName) {
        this.topologyName = topologyName;
    }

    public List<ConfigEditorFile> getConfigsFiles() {
        return configsFiles;
    }

    public void setConfigsFiles(List<ConfigEditorFile> configsFiles) {
        this.configsFiles = configsFiles;
    }

    public List<ConfigEditorFile> getTestCasesFiles() {
        return testCasesFiles;
    }

    public void setTestCasesFiles(List<ConfigEditorFile> testCasesFiles) {
        this.testCasesFiles = testCasesFiles;
    }

    public List<ConfigImporterDto> getConfigImporters() {
        return configImporters;
    }

    public void setConfigImporters(List<ConfigImporterDto> configImporters) {
        this.configImporters = configImporters;
    }

    public String getImportedConfiguration() {
        return importedConfiguration;
    }

    public void setImportedConfiguration(String importedConfiguration) {
        this.importedConfiguration = importedConfiguration;
    }

    public String getConfigImporterAttributesSchema() {
        return configImporterAttributesSchema;
    }

    public void setConfigImporterAttributesSchema(String configImporterAttributesSchema) {
        this.configImporterAttributesSchema = configImporterAttributesSchema;
    }
}
