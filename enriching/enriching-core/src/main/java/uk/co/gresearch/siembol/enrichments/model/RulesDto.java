package uk.co.gresearch.siembol.enrichments.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.reinert.jjschema.Attributes;
import java.util.List;

@Attributes(title = "rules", description = "Rules for real-time enriching events")
public class RulesDto {

    @JsonProperty("rules_version")
    @Attributes(required = true, description = "The version of the rules")
    private Integer rulesVersion;

    @JsonProperty("rules")
    @Attributes(required = true, description = "Rules of the release", minItems = 1)
    private List<RuleDto> rules;

    public Integer getRulesVersion() {
        return rulesVersion;
    }

    public void setRulesVersion(Integer rulesVersion) {
        this.rulesVersion = rulesVersion;
    }

    public List<RuleDto> getRules() {
        return rules;
    }

    public void setRules(List<RuleDto> rules) {
        this.rules = rules;
    }
}
