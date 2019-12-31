package uk.co.gresearch.nortem.enrichments.compiler;

import uk.co.gresearch.nortem.common.testing.InactiveTestingLogger;
import uk.co.gresearch.nortem.common.testing.TestingLogger;
import uk.co.gresearch.nortem.enrichments.common.EnrichmentResult;

public interface EnrichmentCompiler {

    EnrichmentResult compile(String rules, TestingLogger logger);

    default EnrichmentResult compile(String rules) {
        return compile(rules, new InactiveTestingLogger());
    }

    EnrichmentResult getSchema();

    EnrichmentResult getTestSpecificationSchema();

    EnrichmentResult validateConfiguration(String rule);

    default EnrichmentResult validateConfigurations(String rules) {
        try {
            return compile(rules);
        } catch (Exception e) {
            return EnrichmentResult.fromException(e);
        }
    }

    EnrichmentResult testConfiguration(String rule, String testSpecification);

    EnrichmentResult testConfigurations(String rules, String testSpecification);
}
