package uk.co.gresearch.siembol.configeditor.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class ConfigEditorTestCaseResult {
    @JsonProperty("number_skipped_assertions")
    private Integer skippedAssertions;
    @JsonProperty("number_matched_assertions")
    private Integer matchedAssertions;
    @JsonProperty("number_failed_assertions")
    private Integer failedAssertions;

    @JsonProperty("assertion_results")
    private List<ConfigEditorAssertionResult> assertionResults;

    public Integer getSkippedAssertions() {
        return skippedAssertions;
    }

    public void setSkippedAssertions(Integer skippedAssertions) {
        this.skippedAssertions = skippedAssertions;
    }

    public Integer getMatchedAssertions() {
        return matchedAssertions;
    }

    public void setMatchedAssertions(Integer matchedAssertions) {
        this.matchedAssertions = matchedAssertions;
    }

    public Integer getFailedAssertions() {
        return failedAssertions;
    }

    public void setFailedAssertions(Integer failedAssertions) {
        this.failedAssertions = failedAssertions;
    }

    public List<ConfigEditorAssertionResult> getAssertionResults() {
        return assertionResults;
    }

    public void setAssertionResults(List<ConfigEditorAssertionResult> assertionResults) {
        this.assertionResults = assertionResults;
    }
}
