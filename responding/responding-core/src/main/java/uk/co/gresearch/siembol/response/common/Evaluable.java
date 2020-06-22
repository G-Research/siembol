package uk.co.gresearch.siembol.response.common;

/**
 * Interface for evaluating response alerts
 */
public interface Evaluable {
    /**
     * Create an evaluator instance from json attributes
     * @param alert alert for evaluation
     * @return RespondingResult with OK status code with result alert in attributes or
     *                               ERROR status code with message otherwise
     */
    RespondingResult evaluate(ResponseAlert alert);
}
