package uk.co.gresearch.siembol.enrichments.compiler;

import uk.co.gresearch.siembol.common.testing.InactiveTestingLogger;
import uk.co.gresearch.siembol.common.testing.TestingLogger;
import uk.co.gresearch.siembol.enrichments.common.EnrichmentResult;
/**
 * An object that validates, tests and compiles enrichment rules
 *
 * <p>This interface provides functionality for validating, testing and compiling enrichment rules.
 * Moreover, it computes and provides json schema for enrichment rules.
 *
 * @author  Marian Novotny
 */
public interface EnrichmentCompiler {
    /**
     * Compiles rules into an enrichment evaluator
     *
     * @param rules json string with alerting rules
     * @param logger logger for debugging
     * @return enrichment result with an enrichment evaluator
     * @see EnrichmentResult
     */
    EnrichmentResult compile(String rules, TestingLogger logger);

    /**
     * Compiles rules into an enrichment evaluator using an inactive logger
     *
     * @param rules json string with alerting rules
     * @return enrichment result with an enrichment evaluator
     * @see EnrichmentResult
     */
    default EnrichmentResult compile(String rules) {
        return compile(rules, new InactiveTestingLogger());
    }

    /**
     * Provides json schema for enrichment rules
     *
     * @return enrichment result with json schema for enrichment rules
     * @see EnrichmentResult
     */
    EnrichmentResult getSchema();

    /**
     * Provides json schema for testing an enrichment rule
     *
     * @return enrichment result with json schema for testing an enrichment rule
     * @see EnrichmentResult
     */
    EnrichmentResult getTestSpecificationSchema();

    /**
     * Validates an enrichment rule
     *
     * @param rule json string with an enrichment rule
     * @return enrichment result with status OK if the rule is valid
     * @see EnrichmentResult
     */
    EnrichmentResult validateConfiguration(String rule);

    /**
     * Validates enrichment rules.
     * Default implementation tries to compile the rules and returns the status.
     *
     * @param rules json string with enrichment rules
     * @return enrichment result with status OK if the rules are valid
     * @see EnrichmentResult
     */
    default EnrichmentResult validateConfigurations(String rules) {
        try {
            return compile(rules);
        } catch (Exception e) {
            return EnrichmentResult.fromException(e);
        }
    }

    /**
     * Test an enrichment rule on input from a test specification
     *
     * @param rule json string with an enrichment rule
     * @param testSpecification a test specification for testing the rule
     * @return an enrichment result with the test result on success otherwise a result with an error status code
     * @see EnrichmentResult
     */
    EnrichmentResult testConfiguration(String rule, String testSpecification);

    /**
     * Test an enrichment rule on input from a test specification
     *
     * @param rules json string with an enrichment rule
     * @param testSpecification a test specification for testing the rule
     * @return an enrichment result with the test result on success otherwise a result with an error status code
     * @see EnrichmentResult
     */
    EnrichmentResult testConfigurations(String rules, String testSpecification);
}
