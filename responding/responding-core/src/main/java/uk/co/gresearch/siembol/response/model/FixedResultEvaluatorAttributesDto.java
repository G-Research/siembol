package uk.co.gresearch.siembol.response.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.reinert.jjschema.Attributes;
import uk.co.gresearch.siembol.response.common.ResponseEvaluationResult;

@Attributes(title = "fixed evaluator attributes", description = "Attributes for fixed evaluator")
public class FixedResultEvaluatorAttributesDto {
    @JsonProperty("evaluation_result")
    @Attributes(required = true, description = "Evaluation result returned by the evaluator")
    private ResponseEvaluationResult responseEvaluationResult;

    public ResponseEvaluationResult getResponseEvaluationResult() {
        return responseEvaluationResult;
    }

    public void setResponseEvaluationResult(ResponseEvaluationResult responseEvaluationResult) {
        this.responseEvaluationResult = responseEvaluationResult;
    }

}
