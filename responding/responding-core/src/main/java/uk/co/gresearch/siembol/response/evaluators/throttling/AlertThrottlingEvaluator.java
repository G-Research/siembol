package uk.co.gresearch.siembol.response.evaluators.throttling;

import uk.co.gresearch.siembol.common.utils.EvaluationLibrary;
import uk.co.gresearch.siembol.response.common.Evaluable;
import uk.co.gresearch.siembol.response.common.RespondingResult;
import uk.co.gresearch.siembol.response.common.ResponseAlert;
import uk.co.gresearch.siembol.response.common.ResponseEvaluationResult;
import uk.co.gresearch.siembol.response.model.AlertThrottlingEvaluatorAttributesDto;

import java.util.Map;
import java.util.HashMap;
import java.util.Optional;

public class AlertThrottlingEvaluator implements Evaluable {
    private final String suppressionKey;
    private final Map<String, Long> alertTimestamps = new HashMap<>();
    private final long timeWindowInMs;

    public AlertThrottlingEvaluator(AlertThrottlingEvaluatorAttributesDto attributesDto) {
        this.suppressionKey = attributesDto.getSuppressingKey();
        this.timeWindowInMs = attributesDto.getTimeUnitType().convertToMs(attributesDto.getSuppressionTime());
    }

    @Override
    public RespondingResult evaluate(ResponseAlert alert) {
        long timestamp = alert.getTimestamp();
        Optional<String> currentKey = EvaluationLibrary.substitute(alert, suppressionKey);
        if (!currentKey.isPresent()) {
            return RespondingResult.fromEvaluationResult(ResponseEvaluationResult.MATCH, alert);
        }

        if (!alertTimestamps.containsKey(currentKey.get())
                || timestamp > alertTimestamps.get(currentKey.get()) + timeWindowInMs) {
            alertTimestamps.put(currentKey.get(), timestamp);
            return RespondingResult.fromEvaluationResult(ResponseEvaluationResult.MATCH, alert);
        }

        return RespondingResult.fromEvaluationResult(ResponseEvaluationResult.FILTERED, alert);
    }

    public void clean(long timestamp) {
        alertTimestamps.keySet().removeIf(x -> alertTimestamps.get(x) < timestamp);
    }

}
