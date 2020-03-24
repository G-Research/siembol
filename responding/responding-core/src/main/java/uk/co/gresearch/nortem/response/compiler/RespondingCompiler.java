package uk.co.gresearch.nortem.response.compiler;

import uk.co.gresearch.nortem.common.testing.InactiveTestingLogger;
import uk.co.gresearch.nortem.common.testing.TestingLogger;

import uk.co.gresearch.nortem.response.common.RespondingResult;

public interface RespondingCompiler {

    RespondingResult compile(String rules, TestingLogger logger);

    default RespondingResult compile(String rules) {
        return compile(rules, new InactiveTestingLogger());
    }

    RespondingResult getSchema();

    RespondingResult validateConfiguration(String rule);

    default RespondingResult validateConfigurations(String rules) {
        try {
            return compile(rules);
        } catch (Exception e) {
            return RespondingResult.fromException(e);
        }
    }

    RespondingResult getRespondingEvaluatorFactories();

    RespondingResult getRespondingEvaluatorValidators();
}
