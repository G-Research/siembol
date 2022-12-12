package uk.co.gresearch.siembol.response.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.reinert.jjschema.Attributes;
import uk.co.gresearch.siembol.response.common.ResponseEvaluationResult;
/**
 * A data transfer object for representing a fixed evaluator attributes
 *
 * <p>This class is used for json (de)serialisation of a fixed evaluator attributes and
 * for generating json schema from this class using annotations.
 * The evaluator returns always the same result.
 *
 * @author  Marian Novotny
 * @see com.github.reinert.jjschema.Attributes
 * @see com.fasterxml.jackson.annotation.JsonProperty
 * @see ResponseEvaluationResult
 */
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
