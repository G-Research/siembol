package uk.co.gresearch.nortem.response.evaluators.fixed;

import uk.co.gresearch.nortem.response.common.Evaluable;
import uk.co.gresearch.nortem.response.common.ResponseEvaluationResult;
import uk.co.gresearch.nortem.response.common.RespondingResult;
import uk.co.gresearch.nortem.response.common.ResponseAlert;

public class FixedEvaluator implements Evaluable {
    private final ResponseEvaluationResult returnResult;
    public FixedEvaluator(ResponseEvaluationResult returnResult) {
        this.returnResult = returnResult;
    }

    @Override
    public RespondingResult evaluate(ResponseAlert alert) {
        return RespondingResult.fromEvaluationResult(returnResult, alert);
    }
}
