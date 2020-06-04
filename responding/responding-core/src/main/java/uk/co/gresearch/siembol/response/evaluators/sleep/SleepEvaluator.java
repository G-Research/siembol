package uk.co.gresearch.siembol.response.evaluators.sleep;

import uk.co.gresearch.siembol.response.common.Evaluable;
import uk.co.gresearch.siembol.response.common.RespondingResult;
import uk.co.gresearch.siembol.response.common.ResponseAlert;
import uk.co.gresearch.siembol.response.common.ResponseEvaluationResult;
import uk.co.gresearch.siembol.response.model.SleepEvaluatorAttributesDto;

public class SleepEvaluator implements Evaluable {
    private final long sleepingTimeInMs;

    public SleepEvaluator(SleepEvaluatorAttributesDto attributesDto) {
        this.sleepingTimeInMs = attributesDto.getTimeUnitType().convertToMs(attributesDto.getSleepingTime());
    }

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
