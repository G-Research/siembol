package uk.co.gresearch.siembol.alerts.common;

public enum EvaluationResult {
    MATCH,
    NO_MATCH;

    public static EvaluationResult negate(EvaluationResult result) {
        return result == MATCH ? NO_MATCH : MATCH;
    }
}
