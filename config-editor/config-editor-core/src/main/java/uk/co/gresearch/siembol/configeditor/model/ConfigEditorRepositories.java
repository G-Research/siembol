package uk.co.gresearch.siembol.configeditor.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A data transfer object that represents config editor repositories
 *
 * <p>This class represents config editor repositories.
 *
 * @author  Marian Novotny
 * @see JsonProperty
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ConfigEditorRepositories {
    @JsonProperty("rule_store_url")
    private String ruleStoreUrl;
    @JsonProperty("test_case_store_url")
    private String testCaseStoreUrl;
    @JsonProperty("rules_release_url")
    private String rulesReleaseUrl;
    @JsonProperty("admin_config_url")
    private String adminConfigUrl;
    @JsonProperty("rule_store_directory_url")
    private String ruleStoreDirectoryUrl;
    @JsonProperty("test_case_store_directory_url")
    private String testCaseStoreDirectoryUrl;
    @JsonProperty("rules_release_directory_url")
    private String rulesReleaseDirectoryUrl;
    @JsonProperty("admin_config_directory_url")
    private String adminConfigDirectoryUrl;

    public String getRuleStoreUrl() {
        return ruleStoreUrl;
    }

    public void setRuleStoreUrl(String ruleStoreUrl) {
        this.ruleStoreUrl = ruleStoreUrl;
    }

    public String getRulesReleaseUrl() {
        return rulesReleaseUrl;
    }

    public void setRulesReleaseUrl(String rulesReleaseUrl) {
        this.rulesReleaseUrl = rulesReleaseUrl;
    }

    public String getTestCaseStoreUrl() {
        return testCaseStoreUrl;
    }

    public void setTestCaseStoreUrl(String testCaseStoreUrl) {
        this.testCaseStoreUrl = testCaseStoreUrl;
    }

    public String getRuleStoreDirectoryUrl() {
        return ruleStoreDirectoryUrl;
    }

    public void setRuleStoreDirectoryUrl(String ruleStoreDirectoryUrl) {
        this.ruleStoreDirectoryUrl = ruleStoreDirectoryUrl;
    }

    public String getTestCaseStoreDirectoryUrl() {
        return testCaseStoreDirectoryUrl;
    }

    public void setTestCaseStoreDirectoryUrl(String testCaseStoreDirectoryUrl) {
        this.testCaseStoreDirectoryUrl = testCaseStoreDirectoryUrl;
    }

    public String getRulesReleaseDirectoryUrl() {
        return rulesReleaseDirectoryUrl;
    }

    public void setRulesReleaseDirectoryUrl(String rulesReleaseDirectoryUrl) {
        this.rulesReleaseDirectoryUrl = rulesReleaseDirectoryUrl;
    }

    public String getAdminConfigUrl() {
        return adminConfigUrl;
    }

    public void setAdminConfigUrl(String adminConfigUrl) {
        this.adminConfigUrl = adminConfigUrl;
    }

    public String getAdminConfigDirectoryUrl() {
        return adminConfigDirectoryUrl;
    }

    public void setAdminConfigDirectoryUrl(String adminConfigDirectoryUrl) {
        this.adminConfigDirectoryUrl = adminConfigDirectoryUrl;
    }
}
