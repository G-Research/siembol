package uk.co.gresearch.siembol.response.common;

import uk.co.gresearch.siembol.response.evaluators.arrayreducers.ArrayReducerEvaluatorFactory;
import uk.co.gresearch.siembol.response.evaluators.assignment.AssignmentEvaluatorFactory;
import uk.co.gresearch.siembol.response.evaluators.fixed.FixedEvaluatorFactory;
import uk.co.gresearch.siembol.response.evaluators.markdowntable.ArrayTableFormatterEvaluatorFactory;
import uk.co.gresearch.siembol.response.evaluators.markdowntable.TableFormatterEvaluatorFactory;
import uk.co.gresearch.siembol.response.evaluators.matching.MatchingEvaluatorFactory;
import uk.co.gresearch.siembol.response.evaluators.throttling.AlertThrottlingEvaluatorFactory;

import java.util.Arrays;
import java.util.List;

import static uk.co.gresearch.siembol.response.common.RespondingResult.StatusCode.OK;

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

    public static RespondingResult getRespondingEvaluatorFactories() throws Exception{
        List<RespondingEvaluatorFactory> factories = Arrays.asList(
                new FixedEvaluatorFactory(),
                new MatchingEvaluatorFactory(),
                new AssignmentEvaluatorFactory(),
                new TableFormatterEvaluatorFactory(),
                new ArrayTableFormatterEvaluatorFactory(),
                new ArrayReducerEvaluatorFactory(),
                new AlertThrottlingEvaluatorFactory());

        RespondingResultAttributes attributes = new RespondingResultAttributes();
        attributes.setRespondingEvaluatorFactories(factories);
        return new RespondingResult(OK, attributes);
    }
}
