package uk.co.gresearch.siembol.response.common;

import uk.co.gresearch.siembol.response.evaluators.arrayreducers.ArrayReducerEvaluatorFactory;
import uk.co.gresearch.siembol.response.evaluators.assignment.JsonPathAssignmentEvaluatorFactory;
import uk.co.gresearch.siembol.response.evaluators.fixed.FixedResultEvaluatorFactory;
import uk.co.gresearch.siembol.response.evaluators.markdowntable.ArrayTableFormatterEvaluatorFactory;
import uk.co.gresearch.siembol.response.evaluators.markdowntable.TableFormatterEvaluatorFactory;
import uk.co.gresearch.siembol.response.evaluators.matching.MatchingEvaluatorFactory;
import uk.co.gresearch.siembol.response.evaluators.sleep.SleepEvaluatorFactory;
import uk.co.gresearch.siembol.response.evaluators.throttling.AlertThrottlingEvaluatorFactory;

import java.util.Arrays;
import java.util.List;

import static uk.co.gresearch.siembol.response.common.RespondingResult.StatusCode.OK;

public enum ProvidedEvaluators {
    FIXED_RESULT_EVALUATOR("fixed_result"),
    MATCHING_EVALUATOR("matching"),
    JSON_PATH_ASSIGNMENT_EVALUATOR("json_path_assignment"),
    MARKDOWN_TABLE_FORMATTER_EVALUATOR("markdown_table_formatter"),
    ARRAY_MARKDOWN_TABLE_FORMATTER_EVALUATOR("array_markdown_table_formatter"),
    ARRAY_REDUCER_EVALUATOR("array_reducer"),
    ALERT_THROTTLING_EVALUATOR("alert_throttling"),
    SLEEP_EVALUATOR("sleep");

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
                new FixedResultEvaluatorFactory(),
                new MatchingEvaluatorFactory(),
                new JsonPathAssignmentEvaluatorFactory(),
                new TableFormatterEvaluatorFactory(),
                new ArrayTableFormatterEvaluatorFactory(),
                new ArrayReducerEvaluatorFactory(),
                new AlertThrottlingEvaluatorFactory(),
                new SleepEvaluatorFactory());

        RespondingResultAttributes attributes = new RespondingResultAttributes();
        attributes.setRespondingEvaluatorFactories(factories);
        return new RespondingResult(OK, attributes);
    }
}
