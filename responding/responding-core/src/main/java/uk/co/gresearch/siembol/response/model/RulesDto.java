package uk.co.gresearch.siembol.response.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.reinert.jjschema.Attributes;

import java.util.List;
/**
 * A data transfer object for representing response rules
 *
 * <p>This class is used for json (de)serialisation of response rules and
 * for generating json schema from this class using annotations.
 *
 * @author  Marian Novotny
 * @see com.github.reinert.jjschema.Attributes
 * @see com.fasterxml.jackson.annotation.JsonProperty
 * @see RuleDto
 */
@Attributes(title = "rules", description = "Incident Response Rules")
public class RulesDto {
    @JsonProperty("rules_version")
    @Attributes(required = true, description = "Incident response rules version", minimum = 0)
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
