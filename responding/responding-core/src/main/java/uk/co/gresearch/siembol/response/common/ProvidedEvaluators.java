package uk.co.gresearch.siembol.response.common;

import uk.co.gresearch.siembol.response.evaluators.arrayreducers.ArrayReducerEvaluatorFactory;
import uk.co.gresearch.siembol.response.evaluators.assignment.JsonPathAssignmentEvaluatorFactory;
import uk.co.gresearch.siembol.response.evaluators.fixed.FixedResultEvaluatorFactory;
import uk.co.gresearch.siembol.response.evaluators.kafkawriter.KafkaWriterEvaluatorFactory;
import uk.co.gresearch.siembol.response.evaluators.markdowntable.ArrayTableFormatterEvaluatorFactory;
import uk.co.gresearch.siembol.response.evaluators.markdowntable.TableFormatterEvaluatorFactory;
import uk.co.gresearch.siembol.response.evaluators.matching.MatchingEvaluatorFactory;
import uk.co.gresearch.siembol.response.evaluators.sleep.SleepEvaluatorFactory;
import uk.co.gresearch.siembol.response.evaluators.throttling.AlertThrottlingEvaluatorFactory;
import uk.co.gresearch.siembol.response.evaluators.timeexclusion.TimeExclusionEvaluatorFactory;
import uk.co.gresearch.siembol.response.model.ProvidedEvaluatorsProperties;

import java.util.ArrayList;
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
    SLEEP_EVALUATOR("sleep"),
    KAFKA_WRITER_EVALUATOR("kafka_writer"),
    TIME_EXCLUSION_EVALUATOR("time_exclusion");

    private final String name;
    ProvidedEvaluators(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    public static RespondingResult getRespondingEvaluatorFactories(
            ProvidedEvaluatorsProperties properties) throws Exception {
        List<RespondingEvaluatorFactory> factories =  new ArrayList<>(List.of(
                new FixedResultEvaluatorFactory(),
                new MatchingEvaluatorFactory(),
                new JsonPathAssignmentEvaluatorFactory(),
                new TableFormatterEvaluatorFactory(),
                new ArrayTableFormatterEvaluatorFactory(),
                new ArrayReducerEvaluatorFactory(),
                new AlertThrottlingEvaluatorFactory(),
                new SleepEvaluatorFactory(),
                new TimeExclusionEvaluatorFactory()));

        if (properties != null && properties.getKafkaWriter() != null) {
                factories.add(new KafkaWriterEvaluatorFactory(properties.getKafkaWriter()));
        }

        RespondingResultAttributes attributes = new RespondingResultAttributes();
        attributes.setRespondingEvaluatorFactories(factories);
        return new RespondingResult(OK, attributes);
    }
}
