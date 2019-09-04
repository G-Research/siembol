package uk.co.gresearch.nortem.nikita.correlationengine;
import org.apache.commons.lang3.exception.ExceptionUtils;
import uk.co.gresearch.nortem.nikita.common.EvaluationResult;
import uk.co.gresearch.nortem.nikita.common.NikitaAttributes;
import uk.co.gresearch.nortem.nikita.common.NikitaFields;
import uk.co.gresearch.nortem.nikita.common.NikitaResult;
import uk.co.gresearch.nortem.nikita.engine.AbstractRule;

import java.util.*;
import java.util.stream.Collectors;

import static uk.co.gresearch.nortem.nikita.common.NikitaTags.CORRELATION_KEY_TAG_NAME;

public class CorrelationRule extends AbstractRule {
    public enum Flags {
        USE_EVENT_TIME,
    }
    private static final String EVENT_TIMESTAMP_FIELD = "timestamp";
    private final EnumSet<Flags> flags;
    private final int alertsThresholds;
    private final long timeWindowInMs;
    private final long maxLagTimeInMs;

    private final ArrayList<AlertCounterMetadata> alertCountersMetadata;
    private final Map<String, Integer> alertToCounterIndex;
    private final Map<String, ArrayList<AlertCounter>> alertCounters = new HashMap<>();

    protected CorrelationRule(Builder<?> builder) {
        super(builder);
        this.alertsThresholds = builder.alertsThresholds;
        this.timeWindowInMs = builder.timeWindowInMs;
        this.maxLagTimeInMs = builder.maxLagTimeInMs;
        this.flags = builder.flags;
        this.alertCountersMetadata = builder.alertCountersMetadata;
        this.alertToCounterIndex = builder.alertToCounterIndex;
    }
    @Override
    public NikitaResult match(Map<String, Object> alert) {
        String alertName = (String)alert.get(NikitaFields.RULE_NAME.getNikitaName());
        String key = (String)alert.get(CORRELATION_KEY_TAG_NAME.toString());
        long processingTime = (Long)alert.get(NikitaFields.PROCESSING_TIME.getNikitaCorrelationName());
        long eventTime = flags.contains(Flags.USE_EVENT_TIME)
                && (alert.get(EVENT_TIMESTAMP_FIELD) instanceof Number)
                ? ((Number)alert.get(EVENT_TIMESTAMP_FIELD)).longValue()
                : processingTime;
        try {
            if (EvaluationResult.NO_MATCH == evaluate(key, alertName, eventTime, processingTime)) {
                return NikitaResult.fromEvaluationResult(EvaluationResult.NO_MATCH, alert);
            }

            Map<String, Object> outAlert = createOutputAlert(alert);
            addOutputFieldsToEvent(outAlert);
            alertCounters.remove(key);
            return NikitaResult.fromEvaluationResult(EvaluationResult.MATCH, outAlert);
        } catch (Exception e) {
            NikitaAttributes attr = new NikitaAttributes();
            Map<String, Object> outAlert = createOutputAlert(alert);
            outAlert.put(NikitaFields.EXCEPTION.getNikitaCorrelationName(), ExceptionUtils.getStackTrace(e));
            attr.setEvent(outAlert);
            return new NikitaResult(NikitaResult.StatusCode.ERROR, attr);
        }
    }

    public void clean(long currentTime) {
        long waterMark = currentTime - timeWindowInMs - maxLagTimeInMs;
        alertCounters.keySet().removeIf(x -> cleanAlertCounters(alertCounters.get(x), waterMark));
    }

    public List<String> getAlertNames() {
        return alertToCounterIndex
                .keySet()
                .stream()
                .collect(Collectors.toList());
    }


    private EvaluationResult evaluate(String key, String ruleName, long eventTime, long processingTime) {
        ArrayList<AlertCounter> currentCounterList = alertCounters.get(key);
        if (currentCounterList == null) {
            currentCounterList = createAlertCounters();
            alertCounters.put(key, currentCounterList);
        } else {
            cleanAlertCounters(currentCounterList, processingTime - timeWindowInMs - maxLagTimeInMs);
        }

        int index = alertToCounterIndex.get(ruleName);
        AlertCounter currentCounter = currentCounterList.get(index);
        currentCounter.update(eventTime);
        if (currentCounter.matchThreshold()) {
            return evaluateRule(currentCounterList);
        } else {
            return EvaluationResult.NO_MATCH;
        }
    }

    private Map<String, Object> createOutputAlert(Map<String, Object> alert) {
        Map<String, Object> ret = new HashMap<>(alert);
        ret.put(NikitaFields.RULE_NAME.getNikitaCorrelationName(), getRuleName());
        ret.put(NikitaFields.FULL_RULE_NAME.getNikitaCorrelationName(), getFullRuleName());
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

    public static abstract class Builder<T extends CorrelationRule> extends AbstractRule.Builder<T>{
        protected static final String ALERT_ALREADY_EXISTS_MSG = "Duplicate alert names for correlation";
        protected static final String INVALID_ALERT_COUNTER = "Invalid alert counter specification";
        protected static final String EMTPY_ALERT_COUNTERS_MSG = "Missing alert counters";
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

        public Builder<T> alertsThresholds(Integer alertThresholds) {
            this.alertsThresholds = alertThresholds;
            return this;
        }

        public Builder<T> timeWindowInMs(long timeWindowInMs) {
            this.timeWindowInMs = timeWindowInMs;
            return this;
        }

        public Builder<T> maxLagTimeInSec(Integer maxLagTimeInSec) {
            this.maxLagTimeInSec = maxLagTimeInSec;
            return this;
        }

        public Builder<T> flags(EnumSet<Flags> flags) {
            this.flags = flags;
            return this;
        }

        public Builder<T> addAlertCounter(String alertName, int threshold, EnumSet<AlertCounterMetadata.Flags> flags) {
            if (threshold <= 0 || threshold > MAX_ALERT_THRESHOLD || alertName == null) {
                throw new IllegalArgumentException(INVALID_ALERT_COUNTER);
            }

            AlertCounterMetadata metadata = new AlertCounterMetadata(alertName, threshold, 0, flags);
            alertCountersMetadataTemp.add(metadata);
            return this;
        }
    }

    public static CorrelationRule.Builder<CorrelationRule> builder() {

        return new CorrelationRule.Builder<CorrelationRule>() {
            @Override
            protected CorrelationRule buildInternally() {
                if (!flags.contains(Flags.USE_EVENT_TIME)) {
                    maxLagTimeInSec = PROCESSING_TIME_MAX_LAG_TIME;
                }
                if (alertCountersMetadataTemp.isEmpty()) {
                    throw new IllegalArgumentException(EMTPY_ALERT_COUNTERS_MSG);
                }
                if (timeWindowInMs == null || maxLagTimeInSec == null) {
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
