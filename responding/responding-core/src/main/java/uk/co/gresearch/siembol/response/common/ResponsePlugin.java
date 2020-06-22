package uk.co.gresearch.siembol.response.common;

import org.springframework.plugin.core.Plugin;

/**
 * Plugin interface responding evaluator factories provided by the plugin
 */
public interface ResponsePlugin extends Plugin<String> {
    /**
     * Get responding evaluator factories provided by the plugin
     *
     * @return RespondingResult with OK status code and respondingEvaluatorFactories in attributes or
     *                               ERROR status code with message otherwise
     */
    RespondingResult getRespondingEvaluatorFactories();

    @Override
    default boolean supports(String str) {
        return true;
    }
}
