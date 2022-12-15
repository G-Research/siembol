package uk.co.gresearch.siembol.configeditor.testcase;

import uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult;
/**
 * An object for evaluating a test case
 *
 * <p>This interface is for representing a test case evaluator.
 *
 * @author  Marian Novotny
 *
 */
public interface TestCaseEvaluator {

    /**
     * Evaluates a test case on a testing result
     *
     * @param jsonResult a json string with a testing result
     * @param testCase a json string with test case specification
     * @return a config editor result with a test case result on success, otherwise
     *         the result with ERROR status code.
     */
    ConfigEditorResult evaluate(String jsonResult, String testCase);

    /**
     * Validates a test case
     *
     * @param testCase a json string with test case specification
     * @return a config editor result with OK status code if the testcase is valid, otherwise
     *         the result with ERROR status code.
     */
    ConfigEditorResult validate(String testCase);

    /**
     * Gets json schema for a test case
     *
     * @return a config editor result with a test case json schema
     */

    ConfigEditorResult getSchema();

    /**
     * Gets a test case evaluator with formatted error messages
     *
     * @return a test case evaluator with formatted error messages
     */
    default TestCaseEvaluator withErrorMessage() {
        return new TestCaseEvaluatorWithErrorMessage(this);
    }
}
