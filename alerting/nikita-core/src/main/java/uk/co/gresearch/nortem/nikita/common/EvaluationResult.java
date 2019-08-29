package uk.co.gresearch.nortem.nikita.common;

public enum EvaluationResult {
    MATCH,
    NO_MATCH;

    public static EvaluationResult negate(EvaluationResult result) {
        return result == MATCH ? NO_MATCH : MATCH;
    }
}
