package uk.co.gresearch.siembol.response.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.reinert.jjschema.Attributes;

import uk.co.gresearch.siembol.alerts.common.EvaluationResult;
import uk.co.gresearch.siembol.response.common.ResponseEvaluationResult;

@Attributes(title = "matching evaluator result", description = "Result after matching")
public enum MatchingEvaluatorResultDto {
    @JsonProperty("match") MATCH("match"),
    @JsonProperty("filtered") FILTERED("filtered"),
    @JsonProperty("filtered_when_no_match") FILTERED_WHEN_NO_MATCH("filtered_when_no_match");

    private final String name;

    MatchingEvaluatorResultDto(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    public ResponseEvaluationResult computeFromEvaluationResult(EvaluationResult evaluationResult) {
        if (evaluationResult == EvaluationResult.NO_MATCH) {
            return this == FILTERED_WHEN_NO_MATCH
                    ? ResponseEvaluationResult.FILTERED
                    : ResponseEvaluationResult.NO_MATCH;
        }
        return this == FILTERED ? ResponseEvaluationResult.FILTERED : ResponseEvaluationResult.MATCH;
    }
}
