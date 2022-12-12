package uk.co.gresearch.siembol.response.common;

import uk.co.gresearch.siembol.common.metrics.SiembolMetricsRegistrar;
/**
 * An object for creating a response evaluator
 *
 * <p>This interface is for creating a response evaluator and providing metadata such as a type and attributes schema.
 * Moreover, it provides the functionality for validating the evaluator attributes.
 *
 * @author  Marian Novotny
 */
public interface RespondingEvaluatorFactory {
    /**
     * Creates an evaluator instance from json attributes
     *
     * @param attributes a json string with attributes used for creating the evaluator instance
     * @return a responding result with OK status code and evaluator in attributes on success, otherwise
     *         the result with ERROR status.
     */
    RespondingResult createInstance(String attributes);

    /**
     * Gets the type of the evaluator including its name.
     * The name should be unique in the Siembol response instance.
     *
     * @return a responding result with the evaluator type in attributes on success, or
     *          the result with ERROR status code and the error message otherwise.
     */
    RespondingResult getType();

    /**
     * Gets a json schema of evaluator attributes
     *
     * @return a responding result with attributes schema on success, otherwise
     *         the result with ERROR status code and the error message.
     */
    RespondingResult getAttributesJsonSchema();

    /**
     * Validates evaluator attributes
     *
     * @return a responding result with OK status code if the attributes are valid, otherwise
     *         the result with ERROR status code.
     */
    default RespondingResult validateAttributes(String attributes) {
        try {
            return createInstance(attributes);
        } catch (Exception e) {
            return RespondingResult.fromException(e);
        }
    }

    /**
     * Registers metric factory for registering metrics
     *
     * @return a responding result with OK status code on success, or
     *          the result with ERROR status code otherwise.
     */
    default RespondingResult registerMetrics(SiembolMetricsRegistrar metricRegistrar) {
        return new RespondingResult(RespondingResult.StatusCode.OK, new RespondingResultAttributes());
    }
}
