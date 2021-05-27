package uk.co.gresearch.siembol.alerts.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.reinert.jjschema.Attributes;
import java.util.List;

@Attributes(title = "rule", description = "Rule for real-time alert matching")
public class RuleDto {
    @JsonProperty("rule_name")
    @Attributes(required = true, description = "Rule name that uniquely identifies the rule", pattern = "^[a-zA-Z0-9_\\-]+$")
    private String ruleName;

    @JsonProperty("rule_author")
    @Attributes(required = true, description = "The owner of the rule")
    private String ruleAuthor;

    @JsonProperty("rule_version")
    @Attributes(required = true, description = "The version of the rule")
    private int ruleVersion;

    @JsonProperty("rule_description")
    @Attributes(description = "The description of the rule")
    private String ruleDescription;

    @JsonProperty("source_type")
    @Attributes(required = true,
            description = "The source type (source:type in data), use '*' for an all source type rule")
    private String sourceType;

    @JsonProperty("matchers")
    @Attributes(required = true, description = "Matchers of the rule", minItems = 1)
    private List<MatcherDto> matchers;

    @JsonProperty("tags")
    @Attributes(description = "The tags of the rule that will be appended to the event after matching")
    private List<TagDto> tags;

    @JsonProperty("rule_protection")
    @Attributes(description = "Protection specification for the rule that override the global protection settings")
    RuleProtectionDto ruleProtection = new RuleProtectionDto();

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public String getRuleAuthor() {
        return ruleAuthor;
    }

    public void setRuleAuthor(String ruleAuthor) {
        this.ruleAuthor = ruleAuthor;
    }

    public int getRuleVersion() {
        return ruleVersion;
    }

    public void setRuleVersion(int ruleVersion) {
        this.ruleVersion = ruleVersion;
    }

    public String getRuleDescription() {
        return ruleDescription;
    }

    public void setRuleDescription(String ruleDescription) {
        this.ruleDescription = ruleDescription;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public List<MatcherDto> getMatchers() {
        return matchers;
    }

    public void setMatchers(List<MatcherDto> matchers) {
        this.matchers = matchers;
    }

    public List<TagDto> getTags() {
        return tags;
    }

    public void setTags(List<TagDto> tags) {
        this.tags = tags;
    }

    public RuleProtectionDto getRuleProtection() {
        return ruleProtection;
    }

    public void setRuleProtection(RuleProtectionDto ruleProtection) {
        this.ruleProtection = ruleProtection;
    }
}
