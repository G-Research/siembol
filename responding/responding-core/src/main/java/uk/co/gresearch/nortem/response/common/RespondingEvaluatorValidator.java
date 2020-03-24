package uk.co.gresearch.nortem.response.common;

public interface RespondingEvaluatorValidator {
    RespondingResult getType();

    RespondingResult getAttributesJsonSchema();

    RespondingResult validateAttributes(String attributes);
}
