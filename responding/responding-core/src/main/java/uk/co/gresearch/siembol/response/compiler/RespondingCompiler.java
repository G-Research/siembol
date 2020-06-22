package uk.co.gresearch.siembol.response.compiler;

import uk.co.gresearch.siembol.common.testing.InactiveTestingLogger;
import uk.co.gresearch.siembol.common.testing.TestingLogger;

import uk.co.gresearch.siembol.response.common.RespondingResult;

public interface RespondingCompiler {

    RespondingResult compile(String rules, TestingLogger logger);

    default RespondingResult compile(String rules) {
        return compile(rules, new InactiveTestingLogger());
    }

    RespondingResult getSchema();

    RespondingResult getTestSpecificationSchema();

    RespondingResult testConfigurations(String rules, String testSpecification);

    RespondingResult validateConfiguration(String rule);

    default RespondingResult validateConfigurations(String rules) {
        try {
            return compile(rules);
        } catch (Exception e) {
            return RespondingResult.fromException(e);
        }
    }

    RespondingResult getRespondingEvaluatorFactories();
}
