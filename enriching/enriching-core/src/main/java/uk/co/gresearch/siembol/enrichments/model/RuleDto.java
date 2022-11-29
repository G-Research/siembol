package uk.co.gresearch.siembol.enrichments.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.reinert.jjschema.Attributes;
import java.util.List;
/**
 * A data transfer object for representing an enrichment rule
 *
 * <p>This class is used for json (de)serialisation of an enrichment rule and
 * for generating json schema from this class using annotations.
 *
 * @author  Marian Novotny
 * @see com.github.reinert.jjschema.Attributes
 * @see com.fasterxml.jackson.annotation.JsonProperty
 * @see MatcherDto
 * @see TableMappingDto
 */
@Attributes(title = "rule", description = "Rule for real-time enriching events")
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

    @JsonProperty("table_mapping")
    @Attributes(required = true, description = "Mappings for enriching events")
    private TableMappingDto tableMapping;

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

    public TableMappingDto getTableMapping() {
        return tableMapping;
    }

    public void setTableMapping(TableMappingDto tableMapping) {
        this.tableMapping = tableMapping;
    }
}
