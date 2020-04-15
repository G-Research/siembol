package uk.co.gresearch.siembol.configeditor.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ConfigEditorRepositories {
    @JsonProperty("rule_store_url")
    private String ruleStoreUrl;
    @JsonProperty("rules_release_url")
    private String rulesReleaseUrl;

    public ConfigEditorRepositories(String ruleStoreUrl, String rulesReleaseUrl) {
        this.ruleStoreUrl = ruleStoreUrl;
        this.rulesReleaseUrl = rulesReleaseUrl;
    }

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
}
