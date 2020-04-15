package uk.co.gresearch.siembol.response.common;
import java.util.Map;

public interface RespondingEvaluatorFactory extends RespondingEvaluatorValidator {

    RespondingResult createInstance(String attributes);

    default RespondingResult validateAttributes(String attributes) {
        try {
            return createInstance(attributes);
        } catch (Exception e) {
            return RespondingResult.fromException(e);
        }
    }

    default RespondingResult initialise(Map<String, Object> configuration) {
        return new RespondingResult(RespondingResult.StatusCode.OK, new RespondingResultAttributes());
    }

    default RespondingResult registerMetrics(MetricFactory metricFactory) {
        return new RespondingResult(RespondingResult.StatusCode.OK, new RespondingResultAttributes());
    }
}
