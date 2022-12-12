package uk.co.gresearch.siembol.response.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.reinert.jjschema.Attributes;

import java.util.List;
/**
 * A data transfer object for representing matching evaluator attributes
 *
 * <p>This class is used for json (de)serialisation of matching evaluator attributes and
 * for generating json schema from this class using annotations.
 *
 * @author  Marian Novotny
 * @see com.github.reinert.jjschema.Attributes
 * @see com.fasterxml.jackson.annotation.JsonProperty
 * @see MatcherDto
 * @see MatchingEvaluatorResultDto
 */
@Attributes(title = "matching evaluator attributes", description = "Attributes for matching evaluator")
public class MatchingEvaluatorAttributesDto {
    @JsonProperty("evaluation_result")
    @Attributes(required = true, description = "Evaluation result returned by the evaluator after matching")
    private MatchingEvaluatorResultDto evaluationResult = MatchingEvaluatorResultDto.MATCH;

    @JsonProperty("matchers")
    @Attributes(required = true, description = "Matchers of the evaluator", minItems = 1)
    private List<MatcherDto> matchers;

    public MatchingEvaluatorResultDto getEvaluationResult() {
        return evaluationResult;
    }

    public void setEvaluationResult(MatchingEvaluatorResultDto evaluationResult) {
        this.evaluationResult = evaluationResult;
    }

    public List<MatcherDto> getMatchers() {
        return matchers;
    }

    public void setMatchers(List<MatcherDto> matchers) {
        this.matchers = matchers;
    }
}
