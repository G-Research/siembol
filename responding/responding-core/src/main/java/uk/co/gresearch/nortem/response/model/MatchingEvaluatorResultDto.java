package uk.co.gresearch.nortem.response.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.reinert.jjschema.Attributes;
import uk.co.gresearch.nortem.response.common.ResponseEvaluationResult;

@Attributes(title = "matching evaluator result", description = "Result after matching")
public enum MatchingEvaluatorResultDto {
    @JsonProperty("match") MATCH("match", ResponseEvaluationResult.MATCH),
    @JsonProperty("filtered") FILTERED("filtered", ResponseEvaluationResult.FILTERED);

    private final String name;
    private final ResponseEvaluationResult responseEvaluationResult;
    MatchingEvaluatorResultDto(String name, ResponseEvaluationResult responseEvaluationResult) {
        this.name = name;
        this.responseEvaluationResult = responseEvaluationResult;
    }

    @Override
    public String toString() {
        return name;
    }

    public ResponseEvaluationResult getResponseEvaluationResult() {
        return responseEvaluationResult;
    }
}
