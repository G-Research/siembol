package uk.co.gresearch.siembol.response.common;

/**
 * Responding evaluator factory
 * - creates evaluator instances
 * - provides metadata about the evaluator such as attributes and type
 * - validates evaluator attributes
 */
public interface RespondingEvaluatorFactory {
    /**
     * Create an evaluator instance from json attributes
     *
     * @param attributes json attributes used for creating evaluator instance
     * @return RespondingResult with OK status code and evaluator in attributes or ERROR status code with message otherwise
     */
    RespondingResult createInstance(String attributes);

    /**
     * Get type of the evaluator including name. This name should be unique between factories.
     *
     * @return RespondingResult with evaluatorType in attributes or ERROR status code with message otherwise
     */
    RespondingResult getType();

    /**
     * Get json schema of evaluator attributes
     *
     * @return RespondingResult with attributesSchema in attributes or ERROR status code with message otherwise
     */
    RespondingResult getAttributesJsonSchema();

    /**
     * Validate evaluator attributes
     *
     * @return RespondingResult with OK status code if attributes are valid or ERROR status code with message otherwise
     */
    default RespondingResult validateAttributes(String attributes) {
        try {
            return createInstance(attributes);
        } catch (Exception e) {
            return RespondingResult.fromException(e);
        }
    }

    /**
     * Register metric factory that can be used for creating metrics
     *
     * @return RespondingResult with OK status code on success or ERROR status code with message otherwise
     */
    default RespondingResult registerMetrics(MetricFactory metricFactory) {
        return new RespondingResult(RespondingResult.StatusCode.OK, new RespondingResultAttributes());
    }
}
