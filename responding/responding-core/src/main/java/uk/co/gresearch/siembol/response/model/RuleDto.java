package uk.co.gresearch.siembol.response.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.reinert.jjschema.Attributes;

import java.util.List;
/**
 * A data transfer object for representing a response rule
 *
 * <p>This class is used for json (de)serialisation of a response rule and
 * for generating json schema from this class using annotations.
 *
 * @author  Marian Novotny
 * @see com.github.reinert.jjschema.Attributes
 * @see com.fasterxml.jackson.annotation.JsonProperty
 * @see ResponseEvaluatorDto
 */
@Attributes(title = "rule", description = "Response rule that should handle response to a siembol alert")
public class RuleDto {
    @JsonProperty("rule_name")
    @Attributes(required = true, description = "ResponseRule name that uniquely identifies the rule", pattern = "^[a-zA-Z0-9_\\-]+$")
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

    @Attributes(required = true, minItems = 1, description = "Evaluators of the rule")
    private List<ResponseEvaluatorDto> evaluators;

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

    public List<ResponseEvaluatorDto> getEvaluators() {
        return evaluators;
    }

    public void setEvaluators(List<ResponseEvaluatorDto> evaluators) {
        this.evaluators = evaluators;
    }
}
