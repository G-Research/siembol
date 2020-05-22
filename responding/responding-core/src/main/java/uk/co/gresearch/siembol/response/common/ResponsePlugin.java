package uk.co.gresearch.siembol.response.common;

import org.springframework.plugin.core.Plugin;

public interface ResponsePlugin extends Plugin<String> {
    RespondingResult getRespondingEvaluatorFactories();

    @Override
    default boolean supports(String str) {
        return true;
    }
}
