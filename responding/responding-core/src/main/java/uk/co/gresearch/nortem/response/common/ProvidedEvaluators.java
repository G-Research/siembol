package uk.co.gresearch.nortem.response.common;

public enum ProvidedEvaluators {
    FIXED_EVALUATOR("fixed_evaluator"),
    MATCHING_EVALUATOR("matching_evaluator"),
    ASSIGNMENT_EVALUATOR("assignment_evaluator");

    private final String name;

    ProvidedEvaluators(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
