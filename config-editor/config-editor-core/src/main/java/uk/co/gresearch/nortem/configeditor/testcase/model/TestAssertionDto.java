package uk.co.gresearch.nortem.configeditor.testcase.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.reinert.jjschema.Attributes;

@Attributes(title = "test assertion", description = "Test assertion used in test case")
public class TestAssertionDto {
    @JsonProperty("assertion_type")
    @Attributes(required = true, description = "The type of assertion")
    private AssertionTypeDto assertionType;
    @JsonProperty("json_path")
    @Attributes(required = true, description = "Json path for obtaing an actual value for assertion evaluation")
    private String jsonPath;
    @JsonProperty("expected_pattern")
    @Attributes(required = true, description = "Regular expression pattern of expectedPattern value")
    private String expectedPattern;
    @Attributes(description = "The pattern is negatedPattern")
    @JsonProperty("negated_pattern")
    private Boolean negatedPattern = false;
    @Attributes(description = "The description of the assertion")
    private String description;
    @Attributes(description = "The pattern is active and included in test case evaluation")
    Boolean active = true;

    public AssertionTypeDto getAssertionType() {
        return assertionType;
    }

    public void setAssertionType(AssertionTypeDto assertionType) {
        this.assertionType = assertionType;
    }

    public String getJsonPath() {
        return jsonPath;
    }

    public void setJsonPath(String jsonPath) {
        this.jsonPath = jsonPath;
    }

    public String getExpectedPattern() {
        return expectedPattern;
    }

    public void setExpectedPattern(String expectedPattern) {
        this.expectedPattern = expectedPattern;
    }

    public Boolean getNegatedPattern() {
        return negatedPattern;
    }

    public void setNegatedPattern(Boolean negatedPattern) {
        this.negatedPattern = negatedPattern;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
