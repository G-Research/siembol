package uk.co.gresearch.siembol.response.common;

import org.springframework.plugin.core.Plugin;

/**
 * An object for representing a response plugin
 *
 * <p>This interface is for integrating evaluators into siembol response.
 * It supports implementing custom evaluators that can be loaded into Siembol response application and
 * integrated in Siembol UI.
 *
 * @author  Marian Novotny
 */
public interface ResponsePlugin extends Plugin<String> {
    /**
     * Gets responding evaluator factories provided by the plugin
     *
     * @return a responding result with OK status code and responding evaluator factories on success, or
     *         the result with ERROR status code otherwise.
     * @see RespondingResult
     */
    RespondingResult getRespondingEvaluatorFactories();

    @Override
    default boolean supports(String str) {
        return true;
    }
}
