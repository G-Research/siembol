package uk.co.gresearch.siembol.alerts.correlationengine;
import org.apache.commons.lang3.exception.ExceptionUtils;
import uk.co.gresearch.siembol.alerts.common.EvaluationResult;
import uk.co.gresearch.siembol.alerts.common.AlertingAttributes;
import uk.co.gresearch.siembol.alerts.common.AlertingFields;
import uk.co.gresearch.siembol.alerts.common.AlertingResult;
import uk.co.gresearch.siembol.alerts.engine.AbstractRule;
import uk.co.gresearch.siembol.common.constants.SiembolConstants;

import java.util.*;
import java.util.stream.Collectors;

import static uk.co.gresearch.siembol.alerts.common.AlertingTags.CORRELATION_KEY_TAG_NAME;
/**
 * An object for representing correlation alerting rule
 *
 * <p>This derived class of AbstractRule is implementing a correlation alerting rule
 *
 *
 * @author  Marian Novotny
 * @see AbstractRule
 */
public class CorrelationRule extends AbstractRule {
    public enum Flags {
        USE_EVENT_TIME,
    }
    private static final String EVENT_TIMESTAMP_FIELD = SiembolConstants.TIMESTAMP;
    private final EnumSet<Flags> flags;
    private final int alertsThresholds;
    private final long timeWindowInMs;
    private final long maxLagTimeInMs;

    private final ArrayList<AlertCounterMetadata> alertCountersMetadata;
    private final Map<String, Integer> alertToCounterIndex;
    private final Map<String, ArrayList<AlertCounter>> alertCounters = new HashMap<>();

    private final List<String> fieldNamesToSend;

    /**
     * Creates correlation rule using builder pattern.
     *
     * @param builder CorrelationRule builder
     */
    protected CorrelationRule(Builder<?> builder) {
        super(builder);
        this.alertsThresholds = builder.alertsThresholds;
        this.timeWindowInMs = builder.timeWindowInMs;
        this.maxLagTimeInMs = builder.maxLagTimeInMs;
        this.flags = builder.flags;
        this.alertCountersMetadata = builder.alertCountersMetadata;
        this.alertToCounterIndex = builder.alertToCounterIndex;
        this.fieldNamesToSend = builder.fieldNamesToSend;
    }

    /**
     * Evaluates the rule by correlating the alert with other alerts based on the correlation key.
     * It uses alert counters to count alerts with thresholds specified in the alert counter metadata.
     * It includes the matching result with attributes in alerting result.
     * It includes correlated alerts into the attributes after triggering the rule.
     *
     *
     * @param alert map of string to object
     * @return alerting result after evaluation
     * @see AlertingResult
     * @see AlertCounter
     * @see AlertCounterMetadata
     *
     */
    @Override
    public AlertingResult match(Map<String, Object> alert) {
        String alertName = (String)alert.get(AlertingFields.RULE_NAME.getAlertingName());
        String key = (String)alert.get(CORRELATION_KEY_TAG_NAME.toString());
        long processingTime = (Long)alert.get(AlertingFields.PROCESSING_TIME.getCorrelationAlertingName());
        long eventTime = flags.contains(Flags.USE_EVENT_TIME)
                && (alert.get(EVENT_TIMESTAMP_FIELD) instanceof Number)
                ? ((Number)alert.get(EVENT_TIMESTAMP_FIELD)).longValue()
                : processingTime;

        Object[] fieldsToSend = new Object[fieldNamesToSend.size()];
        for (int i = 0; i < fieldNamesToSend.size(); i++) {
            fieldsToSend[i] = alert.get(fieldNamesToSend.get(i));
        }
        AlertContext alertContext = new AlertContext(eventTime, fieldsToSend);
        try {
            if (EvaluationResult.NO_MATCH == evaluate(key, alertName, alertContext, processingTime)) {
                return AlertingResult.fromEvaluationResult(EvaluationResult.NO_MATCH, alert);
            }

            Map<String, Object> outAlert = createOutputAlert(alert, key);
            alertCounters.remove(key);
            return AlertingResult.fromEvaluationResult(EvaluationResult.MATCH, outAlert);
        } catch (Exception e) {
            AlertingAttributes attr = new AlertingAttributes();
            Map<String, Object> outAlert = createOutputAlert(alert, key);
            outAlert.put(AlertingFields.EXCEPTION.getCorrelationAlertingName(), ExceptionUtils.getStackTrace(e));
            attr.setEvent(outAlert);
            return new AlertingResult(AlertingResult.StatusCode.ERROR, attr);
        }
    }

    /**
     * Removes unused old internal state in rule counters
     *
     * @param currentTime current time in milliseconds
     */
    public void clean(long currentTime) {
        long waterMark = currentTime - timeWindowInMs - maxLagTimeInMs;
        alertCounters.keySet().removeIf(x -> cleanAlertCounters(alertCounters.get(x), waterMark));
    }

    public List<String> getAlertNames() {
        return new ArrayList<>(alertToCounterIndex.keySet());
    }


    private EvaluationResult evaluate(String key, String ruleName, AlertContext alertContext, long processingTime) {
        ArrayList<AlertCounter> currentCounterList = alertCounters.get(key);
        if (currentCounterList == null) {
            currentCounterList = createAlertCounters();
            alertCounters.put(key, currentCounterList);
        } else {
            cleanAlertCounters(currentCounterList, processingTime - timeWindowInMs - maxLagTimeInMs);
        }

        int index = alertToCounterIndex.get(ruleName);
        AlertCounter currentCounter = currentCounterList.get(index);

        currentCounter.update(alertContext);
        if (currentCounter.matchThreshold()) {
            return evaluateRule(currentCounterList);
        } else {
            return EvaluationResult.NO_MATCH;
        }
    }

    private Map<String, Object> createOutputAlert(Map<String, Object> alert, String key) {
        Map<String, Object> ret = new HashMap<>(alert);
        ret.put(AlertingFields.RULE_NAME.getCorrelationAlertingName(), getRuleName());
        ret.put(AlertingFields.FULL_RULE_NAME.getCorrelationAlertingName(), getFullRuleName());

        List<Map<String, Object>> correlatedAlerts = alertCounters.get(key).stream()
                .flatMap(x -> x.getCorrelatedAlerts(fieldNamesToSend).stream())
                .filter(x -> !x.isEmpty())
                .collect(Collectors.toList());

        if (!correlatedAlerts.isEmpty()) {
            ret.put(AlertingFields.CORRELATED_ALERTS.getCorrelationAlertingName(), correlatedAlerts);
        }
        return ret;
    }

    private EvaluationResult evaluateRule(ArrayList<AlertCounter> alertCounters) {
        int numMatches = 0;
        for (AlertCounter counter : alertCounters) {
            if (counter.matchThreshold()) {
                numMatches++;
            } else if (counter.isMandatory()) {
                return EvaluationResult.NO_MATCH;
            }
        }

        return numMatches >= alertsThresholds ? EvaluationResult.MATCH : EvaluationResult.NO_MATCH;
    }

    private boolean cleanAlertCounters(ArrayList<AlertCounter> alertCounters, long waterMark) {
        boolean empty = true;
        for (AlertCounter counter : alertCounters) {
            counter.clean(waterMark);
            empty = empty && counter.isEmpty();
        }

        return empty;
    }

    private ArrayList<AlertCounter> createAlertCounters() {
        ArrayList<AlertCounter> ret = new ArrayList<>(alertCountersMetadata.size());
        for (AlertCounterMetadata counterMetadata : alertCountersMetadata) {
            ret.add(new AlertCounter(counterMetadata));
        }
        return ret;
    }

    /**
     * A builder for a correlation alerting rule
     *
     * <p>This abstract class is derived from AbstractRule.Builder class
     *
     *
     * @author  Marian Novotny
     */
    public static abstract class Builder<T extends CorrelationRule> extends AbstractRule.Builder<T>{
        protected static final String ALERT_ALREADY_EXISTS_MSG = "Duplicate alert names for correlation";
        protected static final String INVALID_ALERT_COUNTER = "Invalid alert counter specification";
        protected static final String EMPTY_ALERT_COUNTERS_MSG = "Missing alert counters";
        protected static final String MISSING_REQUIRED_ATTRIBUTES = "Missing required attributes for alert correlation";
        protected static final String WRONG_ALERT_THRESHOLDS = "wrong alert thresholds";
        protected static final Integer PROCESSING_TIME_MAX_LAG_TIME = 0;
        protected static final long MILLI_MULTIPLIER = 1000L;
        protected static final int MAX_ALERT_THRESHOLD = 1000;
        protected Integer alertsThresholds;
        protected Long timeWindowInMs;
        protected Integer maxLagTimeInSec;
        protected long maxLagTimeInMs;
        protected ArrayList<AlertCounterMetadata> alertCountersMetadataTemp = new ArrayList<>();
        protected ArrayList<AlertCounterMetadata> alertCountersMetadata = new ArrayList<>();
        protected Map<String, Integer> alertToCounterIndex = new HashMap<>();
        protected EnumSet<Flags> flags = EnumSet.noneOf(Flags.class);
        protected List<String> fieldNamesToSend = new ArrayList<>();
        /**
         * Sets the number of alerts that needs to match
         *
         * @param alertThresholds threshold for number of alerts to match
         * @return this builder
         */
        public Builder<T> alertsThresholds(Integer alertThresholds) {
            this.alertsThresholds = alertThresholds;
            return this;
        }

        /**
         * Sets the time sliding window in milliseconds
         *
         * @param timeWindowInMs sliding window for evaluation in milliseconds
         * @return this builder
         */
        public Builder<T> timeWindowInMs(long timeWindowInMs) {
            this.timeWindowInMs = timeWindowInMs;
            return this;
        }

        /**
         * Sets the maximum lag of alerts in milliseconds.
         * The alerts older than this time can be cleaned and will be not considered during evaluation.
         *
         * @param maxLagTimeInSec maximum lag of alerts
         * @return this builder
         */
        public Builder<T> maxLagTimeInSec(Integer maxLagTimeInSec) {
            this.maxLagTimeInSec = maxLagTimeInSec;
            return this;
        }

        /**
         * Sets correlation rule flags
         *
         * @param flags correlation rule flags
         * @return this builder
         * @see Flags
         */
        public Builder<T> flags(EnumSet<Flags> flags) {
            this.flags = flags;
            return this;
        }

        /**
         * Adds alert counter into the rule
         *
         * @param alertName the name of the alert for correlation
         * @param threshold threshold of alerts for the counter
         * @param flags alert counter metadata flags
         * @return this builder
         * @see AlertCounterMetadata.Flags
         */
        public Builder<T> addAlertCounter(String alertName, int threshold, EnumSet<AlertCounterMetadata.Flags> flags) {
            if (threshold <= 0 || threshold > MAX_ALERT_THRESHOLD || alertName == null) {
                throw new IllegalArgumentException(INVALID_ALERT_COUNTER);
            }

            AlertCounterMetadata metadata = new AlertCounterMetadata(alertName, threshold, 0, flags);
            alertCountersMetadataTemp.add(metadata);
            return this;
        }

        /**
         * Sets alert field names that will be included in the correlated alerts' field after triggering the rule
         *
         * @param fieldNames the list of field names
         * @return this builder
         */
        public Builder<T> fieldNamesToSend(List<String> fieldNames) {
            this.fieldNamesToSend = fieldNames;
            return this;
        }
    }

    /**
     * Creates CorrelationRule builder instance
     *
     * @return CorrelationRule builder
     */
    public static CorrelationRule.Builder<CorrelationRule> builder() {

        return new CorrelationRule.Builder<>() {
            @Override
            protected CorrelationRule buildInternally() {
                if (!flags.contains(Flags.USE_EVENT_TIME)) {
                    maxLagTimeInSec = PROCESSING_TIME_MAX_LAG_TIME;
                }
                if (alertCountersMetadataTemp.isEmpty()) {
                    throw new IllegalArgumentException(EMPTY_ALERT_COUNTERS_MSG);
                }
                if (timeWindowInMs == null || maxLagTimeInSec == null || fieldNamesToSend == null) {
                    throw new IllegalArgumentException(MISSING_REQUIRED_ATTRIBUTES);
                }
                maxLagTimeInMs = maxLagTimeInSec * MILLI_MULTIPLIER;

                for (AlertCounterMetadata metadata : alertCountersMetadataTemp) {
                    AlertCounterMetadata current = new AlertCounterMetadata(metadata.getAlertName(),
                            metadata.getThreshold(),
                            maxLagTimeInMs + timeWindowInMs,
                            metadata.getFlags());
                    if (alertToCounterIndex.containsKey(current.getAlertName())) {
                        throw new IllegalArgumentException(ALERT_ALREADY_EXISTS_MSG);
                    }

                    alertToCounterIndex.put(current.getAlertName(), alertCountersMetadata.size());
                    alertCountersMetadata.add(current);
                }

                if (alertsThresholds == null) {
                    alertsThresholds = alertCountersMetadata.size();
                }

                if (alertsThresholds > alertCountersMetadata.size() || alertsThresholds <= 0) {
                    throw new IllegalArgumentException(WRONG_ALERT_THRESHOLDS);
                }

                return new CorrelationRule(this);
            }
        };
    }
}
