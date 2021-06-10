package uk.co.gresearch.siembol.configeditor.service.alerts.sigma.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.reinert.jjschema.Attributes;
import uk.co.gresearch.siembol.alerts.model.TagDto;

import java.util.List;

@Attributes(title = "sigma rule metadata mapping", description = "Attributes for mapping sigma rule metadata")
public class SigmaRuleMetadataMappingDto {
    @JsonProperty("rule_name")
    @Attributes(required = true, description = "Rule name that uniquely identifies the rule")
    private String ruleName = "imported_from_${title}_{id}";

    @JsonProperty("rule_description")
    @Attributes(description = "The description of the rule")
    private String ruleDescription = "${description}";

    @JsonProperty("source_type")
    @Attributes(required = true,
            description = "The source type (source:type in data), use '*' for an all source type rule")
    private String sourceType;

    @JsonProperty("tags")
    @Attributes(description = "The tags of the rule that will be appended to the event after matching")
    private List<TagDto> tags;

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
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

    public List<TagDto> getTags() {
        return tags;
    }

    public void setTags(List<TagDto> tags) {
        this.tags = tags;
    }
}
