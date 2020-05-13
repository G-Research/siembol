package uk.co.gresearch.siembol.response.common;

public enum ProvidedEvaluators {
    FIXED_EVALUATOR("fixed_evaluator"),
    MATCHING_EVALUATOR("matching_evaluator"),
    ASSIGNMENT_EVALUATOR("assignment_evaluator"),
    TABLE_FORMATTER_EVALUATOR("table_formatter_evaluator"),
    ARRAY_TABLE_FORMATTER_EVALUATOR("array_table_formatter_evaluator"),
    ARRAY_REDUCER_EVALUATOR("array_reducer_evaluator"),
    ALERT_THROTTLING_EVALUATOR("alert_throttling_evaluator");

    private final String name;

    ProvidedEvaluators(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
