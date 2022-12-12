package uk.co.gresearch.siembol.response.evaluators.sleep;

import uk.co.gresearch.siembol.response.common.Evaluable;
import uk.co.gresearch.siembol.response.common.RespondingResult;
import uk.co.gresearch.siembol.response.common.ResponseAlert;
import uk.co.gresearch.siembol.response.common.ResponseEvaluationResult;
import uk.co.gresearch.siembol.response.model.SleepEvaluatorAttributesDto;
/**
 * An object for evaluating response alerts
 *
 * <p>This class implements Evaluable interface, and it is used in a response rule.
 * The sleep evaluator sleeps for certain time defined in the attributes. It is blocking the evaluation of the rule.
 *
 * @author  Marian Novotny
 * @see Evaluable
 */
public class SleepEvaluator implements Evaluable {
    private final long sleepingTimeInMs;

    public SleepEvaluator(SleepEvaluatorAttributesDto attributesDto) {
        this.sleepingTimeInMs = attributesDto.getTimeUnitType().convertToMs(attributesDto.getSleepingTime());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RespondingResult evaluate(ResponseAlert alert) {
        try {
            Thread.sleep(sleepingTimeInMs);
        } catch (InterruptedException e) {
            return RespondingResult.fromException(e);
        }
        return RespondingResult.fromEvaluationResult(ResponseEvaluationResult.MATCH, alert);
    }
}
