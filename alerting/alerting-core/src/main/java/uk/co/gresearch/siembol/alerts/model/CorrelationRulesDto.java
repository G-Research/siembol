package uk.co.gresearch.siembol.alerts.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.reinert.jjschema.Attributes;
import uk.co.gresearch.siembol.alerts.common.AlertingTags;

import java.util.Arrays;
import java.util.List;

@Attributes(title = "correlation rules", description = "Correlation rules for real-time correlation alert matching")
public class CorrelationRulesDto {
    public CorrelationRulesDto() {
        TagDto tag = new TagDto();
        tag.setTagName(AlertingTags.DETECTION_SOURCE_TAG_NAME.toString());
        tag.setTagValue(AlertingTags.CORRELATION_ENGINE_DETECTION_SOURCE_TAG_VALUE.toString());
        tags = Arrays.asList(tag);
    }

    @JsonProperty("rules_version")
    @Attributes(required = true, description = "The version of the correlation correlationRules")
    Integer rulesVersion;

    @JsonProperty("tags")
    @Attributes(required = true, description = "The tags that will be added to the correlation alert")
    private List<TagDto> tags;
    @JsonProperty("rules_protection")
    @Attributes(description = "Global protection specification for correlationRules")
    RuleProtectionDto rulesProtection = new RuleProtectionDto();

    @JsonProperty("rules")
    @Attributes(required = true, description = "The version of the correlationRules release", minItems = 1)
    List<CorrelationRuleDto> rules;

    public Integer getRulesVersion() {
        return rulesVersion;
    }

    public void setRulesVersion(Integer rulesVersion) {
        this.rulesVersion = rulesVersion;
    }

    public List<TagDto> getTags() {
        return tags;
    }

    public void setTags(List<TagDto> tags) {
        this.tags = tags;
    }

    public RuleProtectionDto getRulesProtection() {
        return rulesProtection;
    }

    public void setRulesProtection(RuleProtectionDto rulesProtection) {
        this.rulesProtection = rulesProtection;
    }

    public List<CorrelationRuleDto> getRules() {
        return rules;
    }

    public void setRules(List<CorrelationRuleDto> rules) {
        this.rules = rules;
    }
}
