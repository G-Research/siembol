package uk.co.gresearch.siembol.response.evaluators.timeexclusion;

import com.google.common.base.Strings;
import uk.co.gresearch.siembol.common.utils.EvaluationLibrary;
import uk.co.gresearch.siembol.response.common.Evaluable;
import uk.co.gresearch.siembol.response.common.RespondingResult;
import uk.co.gresearch.siembol.response.common.ResponseAlert;
import uk.co.gresearch.siembol.response.common.ResponseEvaluationResult;
import uk.co.gresearch.siembol.response.model.TimeExclusionEvaluatorAttributesDto;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
/**
 * An object for evaluating response alerts
 *
 * <p>This class implements Evaluable interface, and it is used in a response rule.
 * The time exclusion evaluator inspects a timestamp and evaluates an excluding pattern.
 * It filters the alert if the pattern matches.
 *
 * @author  Marian Novotny
 * @see Evaluable
 */
public class TimeExclusionEvaluator implements Evaluable {
    private static final String SUBSTITUTION_FAILED_MSG = "Substitution of time exclusion evaluator failed";
    private static final String EMPTY_FIELDS_IN_ATTRIBUTES_MSG = "Empty fields in time exclusion evaluator attributes";
    private static final String MISSING_TIMESTAMP_MSG = "Missing timestamp in alert";
    private final TimeExclusionEvaluatorAttributesDto attributes;

    public TimeExclusionEvaluator(TimeExclusionEvaluatorAttributesDto attributes) {
        this.attributes = attributes;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RespondingResult evaluate(ResponseAlert alert) {
        try {
            var substituted = EvaluationLibrary.cloneAndSubstituteBean(attributes, alert);
            if (substituted.isEmpty()
                    || !(substituted.get() instanceof TimeExclusionEvaluatorAttributesDto)) {
                throw new IllegalStateException(SUBSTITUTION_FAILED_MSG);
            }

            var currentAttr = (TimeExclusionEvaluatorAttributesDto) substituted.get();
            if (Strings.isNullOrEmpty(currentAttr.getTimestampField())
                    || Strings.isNullOrEmpty(currentAttr.getTimeZone())
                    || Strings.isNullOrEmpty(currentAttr.getHoursOfDayPattern())
                    || Strings.isNullOrEmpty(currentAttr.getDaysOfWeekPattern())
                    || Strings.isNullOrEmpty(currentAttr.getMonthsOfYearPattern())) {
                throw new IllegalStateException(EMPTY_FIELDS_IN_ATTRIBUTES_MSG);
            }

            Object timestampObj = alert.get(currentAttr.getTimestampField());
            if (!(timestampObj instanceof Number)) {
                throw new IllegalStateException(MISSING_TIMESTAMP_MSG);
            }

            var timestamp = (Number) timestampObj;
            Instant instant = Instant.ofEpochMilli(timestamp.longValue());
            ZoneId zoneId = ZoneId.of(currentAttr.getTimeZone());
            ZonedDateTime dateTime = ZonedDateTime.ofInstant(instant, zoneId);

            if (String.valueOf(dateTime.getHour()).matches(currentAttr.getHoursOfDayPattern())
                    && String.valueOf(dateTime.getDayOfWeek().getValue()).matches(currentAttr.getDaysOfWeekPattern())
                    && String.valueOf(dateTime.getMonthValue()).matches(currentAttr.getMonthsOfYearPattern())) {
                return RespondingResult.fromEvaluationResult(ResponseEvaluationResult.FILTERED, alert);
            }

            return RespondingResult.fromEvaluationResult(attributes.getResultIfNotExcluded(), alert);
        } catch (Exception e) {
            return RespondingResult.fromException(e);
        }
    }
}
