package uk.co.gresearch.siembol.response.compiler;

import uk.co.gresearch.siembol.common.testing.InactiveTestingLogger;
import uk.co.gresearch.siembol.common.testing.TestingLogger;

import uk.co.gresearch.siembol.response.common.RespondingResult;
/**
 * An object that validates, tests and compiles responding rules
 *
 * <p>This interface provides functionality for validating, testing and compiling response rules.
 * Moreover, it computes and provides json schema for response rules.
 *
 *
 * @author  Marian Novotny
 */
public interface RespondingCompiler {
    /**
     * Compiles rules into a response engine
     *
     * @param rules json string with response rules
     * @param logger logger for debugging
     * @return alerting result with response engine
     * @see RespondingResult
     * @see uk.co.gresearch.siembol.response.engine.ResponseEngine
     */
    RespondingResult compile(String rules, TestingLogger logger);

    /**
     * Compiles rules into a response engine
     *
     * @param rules json string with response rules
     * @return alerting result with response engine
     * @see RespondingResult
     * @see uk.co.gresearch.siembol.response.engine.ResponseEngine
     */
    default RespondingResult compile(String rules) {
        return compile(rules, new InactiveTestingLogger());
    }

    /**
     * Provides json schema for response rules
     *
     * @return RespondingResult with json schema for alerting rules
     * @see RespondingResult
     *
     */
    RespondingResult getSchema();

    /**
     * Provides json schema for response testing specification
     *
     * @return RespondingResult with json schema for response testing specification
     * @see RespondingResult
     *
     */
    RespondingResult getTestSpecificationSchema();

    /**
     * Compiles rules into response engine and evaluates a test specification using the engine
     *
     * @param rules json string with alerting rules
     * @param testSpecification json string for test specification
     * @return alerting result with testing result
     * @see RespondingResult
     */
    RespondingResult testConfigurations(String rules, String testSpecification);

    /**
     * Validates a rule by trying to compile it
     *
     * @param rule json string with a response rule
     * @return responding result with status OK if the rule was able to compile
     * @see RespondingResult
     */
    RespondingResult validateConfiguration(String rule);

    /**
     * Validates rules by trying to compile them
     *
     * @param rules json string with response rules
     * @return responding result with status OK if rules were able to compile
     * @see RespondingResult
     */
    default RespondingResult validateConfigurations(String rules) {
        try {
            return compile(rules);
        } catch (Exception e) {
            return RespondingResult.fromException(e);
        }
    }

    /**
     * Gets evaluator factories
     *
     * @return responding result with evaluator factories
     * @see uk.co.gresearch.siembol.response.common.RespondingEvaluatorFactory
     */
    RespondingResult getRespondingEvaluatorFactories();
}
