package uk.co.gresearch.siembol.alerts.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.reinert.jjschema.Attributes;

import java.util.List;

@Attributes(title = "correlation rule", description = "Correlation rule for real-time correlation alert matching")
public class CorrelationRuleDto {
    @JsonProperty("rule_name")
    @Attributes(required = true, description = "Rule name that uniquely identifies the rule")
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

    @JsonProperty("correlation_attributes")
    @Attributes(description = "The attributes that specify the alert correlation evaluation")
    private CorrelationAttributesDto correlationAttributes;

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

    public CorrelationAttributesDto getCorrelationAttributes() {
        return correlationAttributes;
    }

    public void setCorrelationAttributes(CorrelationAttributesDto correlationAttributes) {
        this.correlationAttributes = correlationAttributes;
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
