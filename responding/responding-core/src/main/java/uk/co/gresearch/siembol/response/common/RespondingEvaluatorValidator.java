package uk.co.gresearch.siembol.response.common;

public interface RespondingEvaluatorValidator {
    RespondingResult getType();

    RespondingResult getAttributesJsonSchema();

    RespondingResult validateAttributes(String attributes);
}
