package uk.co.gresearch.nortem.response.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.reinert.jjschema.Attributes;

import java.util.List;

@Attributes(title = "rules", description = "Incident Response Rules")
public class RulesDto {
    @JsonProperty("rules_version")
    @Attributes(required = true, description = "Incident response rules version")
    private Integer rulesVersion = 0;
    @Attributes(required = true, minItems = 1, description = "Response rules")
    private List<RuleDto> rules;

    public Integer getRulesVersion() {
        return rulesVersion;
    }

    public void setRulesVersion(int rulesVersion) {
        this.rulesVersion = rulesVersion;
    }

    public List<RuleDto> getRules() {
        return rules;
    }

    public void setRules(List<RuleDto> rules) {
        this.rules = rules;
    }
}
