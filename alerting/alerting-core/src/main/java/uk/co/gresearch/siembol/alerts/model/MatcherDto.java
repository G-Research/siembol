package uk.co.gresearch.siembol.alerts.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.reinert.jjschema.Attributes;

import java.util.List;
/**
 * A data transfer object for representing an alerting matcher used in alerting rules
 *
 * <p>This class is used for json (de)serialisation of an alerting matcher and
 * for generating json schema from this class using annotations.
 *
 * @author  Marian Novotny
 * @see com.github.reinert.jjschema.Attributes
 * @see com.fasterxml.jackson.annotation.JsonProperty
 * @see RuleDto
 */
@Attributes(title = "matcher", description = "Matcher for matching fields")
public class MatcherDto {
    @Attributes(description = "The matcher is enabled", required = false)
    @JsonProperty("is_enabled")
    private boolean enabled = true;

    @Attributes(description = "Description of the matcher", required = false)
    @JsonProperty("description")
    private String description;
    @JsonProperty("matcher_type")
    @Attributes(required = true, description = "Type of matcher, either Regex match or list of strings " +
            "(newline delimited) or a composite matcher composing several matchers")
    private MatcherTypeDto type;

    @JsonProperty("is_negated")
    @Attributes(description = "The matcher is negated")
    private Boolean negated = false;

    @Attributes(description = "Field on which the matcher will be evaluated")
    private String field;

    @JsonProperty("case_insensitive")
    @Attributes(description = "Use case insensitive string compare")
    private Boolean caseInsensitiveCompare = false;

    @JsonProperty("starts_with")
    @Attributes(description = "The field value starts with the pattern")
    private Boolean startsWith = false;

    @JsonProperty("ends_with")
    @Attributes(description = "The field value ends with the pattern")
    private Boolean endsWith = false;

    @JsonProperty("compare_type")
    @Attributes(description = "The type of comparing numbers")
    NumericCompareTypeDto compareType;
    @Attributes(description = "Matcher expression as defined by matcher type")
    private String data;

    @Attributes(description = "A field numeric value will be compared with the expression. " +
            "The expression can be a numeric constant or a string that contains a variable")
    private String expression;

    @Attributes(description = "List of matchers of the composite matcher")
    private List<MatcherDto> matchers;

    public MatcherTypeDto getType() {
        return type;
    }

    public void setType(MatcherTypeDto type) {
        this.type = type;
    }

    public Boolean getNegated() {
        return negated;
    }

    public void setNegated(Boolean negated) {
        this.negated = negated;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public Boolean getCaseInsensitiveCompare() {
        return caseInsensitiveCompare;
    }

    public void setCaseInsensitiveCompare(Boolean caseInsensitiveCompare) {
        this.caseInsensitiveCompare = caseInsensitiveCompare;
    }

    public List<MatcherDto> getMatchers() {
        return matchers;
    }

    public void setMatchers(List<MatcherDto> matchers) {
        this.matchers = matchers;
    }

    public Boolean getStartsWith() {
        return startsWith;
    }

    public void setStartsWith(Boolean startsWith) {
        this.startsWith = startsWith;
    }

    public Boolean getEndsWith() {
        return endsWith;
    }

    public void setEndsWith(Boolean endsWith) {
        this.endsWith = endsWith;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public NumericCompareTypeDto getCompareType() {
        return compareType;
    }

    public void setCompareType(NumericCompareTypeDto compareType) {
        this.compareType = compareType;
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }
}

