package uk.co.gresearch.nortem.configeditor.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ConfigEditorAssertionResult {
    @JsonProperty("assertion_type")
    private String assertionType;
    @JsonProperty("match")
    private Boolean match;
    @JsonProperty("actual_value")
    private String actualValue;
    @JsonProperty("expected_pattern")
    private String expectedPattern;
    @JsonProperty("negated_pattern")
    private Boolean negated;

    public String getAssertionType() {
        return assertionType;
    }

    public void setAssertionType(String assertionType) {
        this.assertionType = assertionType;
    }

    public Boolean getMatch() {
        return match;
    }

    public void setMatch(Boolean match) {
        this.match = match;
    }

    public String getActualValue() {
        return actualValue;
    }

    public void setActualValue(String actualValue) {
        this.actualValue = actualValue;
    }

    public String getExpectedPattern() {
        return expectedPattern;
    }

    public void setExpectedPattern(String expectedPattern) {
        this.expectedPattern = expectedPattern;
    }

    public Boolean getNegated() {
        return negated;
    }

    public void setNegated(Boolean negated) {
        this.negated = negated;
    }
}
