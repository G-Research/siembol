package uk.co.gresearch.nortem.nikita.correlationengine;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import uk.co.gresearch.nortem.common.utils.TimeProvider;
import uk.co.gresearch.nortem.nikita.common.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import static uk.co.gresearch.nortem.nikita.common.NikitaTags.CORRELATION_KEY_TAG_NAME;

public class CorrelationEngineImpl implements NikitaEngine {
    private static final String MISSING_CORRELATION_ATTRIBUTES = "Missing fields for alert correlation";
    private final Map<String, List<CorrelationRule>> alertToCorrelationRulesMap;
    private final List<CorrelationRule> correlationRules;
    private final TimeProvider timeProvider;
    private final List<Pair<String, Object>> outputFields;

    CorrelationEngineImpl(Builder builder) {
        alertToCorrelationRulesMap = builder.alertToCorrelationRulesMap;
        correlationRules = builder.correlationRules;
        timeProvider = builder.timeProvider;
        this.outputFields = builder.outputFields;
    }

    @Override
    public NikitaResult evaluate(Map<String, Object> alert) {
        if (!(alert.get(NikitaFields.RULE_NAME.getNikitaName()) instanceof String)
                || !(alert.get(CORRELATION_KEY_TAG_NAME.toString()) instanceof String)) {
            return  NikitaResult.fromErrorMessage(MISSING_CORRELATION_ATTRIBUTES);
        }

        String alertName = (String)alert.get(NikitaFields.RULE_NAME.getNikitaName());
        if (!alertToCorrelationRulesMap.containsKey(alertName)) {
            return NikitaResult.fromEvaluationResult(EvaluationResult.NO_MATCH, alert);
        }

        alert.put(NikitaFields.PROCESSING_TIME.getNikitaCorrelationName(), timeProvider.getCurrentTimeInMs());
        List<Map<String, Object>> outputCorrelationAlerts = new ArrayList<>();
        List<Map<String, Object>> exceptionsEvents = new ArrayList<>();

        for (CorrelationRule correlationRule : alertToCorrelationRulesMap.get(alertName)) {
            NikitaResult result = correlationRule.match(alert);
            if (result.getStatusCode() == NikitaResult.StatusCode.ERROR) {
                exceptionsEvents.add(result.getAttributes().getEvent());
            } else if (result.getAttributes().getEvaluationResult() == EvaluationResult.MATCH) {
                Map<String, Object> outAlert = result.getAttributes().getEvent();
                outputFields.forEach(x -> outAlert.putIfAbsent(x.getKey(), x.getValue()));
                outputCorrelationAlerts.add(outAlert);
            }
        }

        NikitaAttributes attributes = new NikitaAttributes();
        attributes.setEvaluationResult(outputCorrelationAlerts.isEmpty() ? EvaluationResult.NO_MATCH
                : EvaluationResult.MATCH);

        if (!outputCorrelationAlerts.isEmpty()) {
            attributes.setOutputEvents(outputCorrelationAlerts );
        }

        if (!exceptionsEvents.isEmpty()) {
            attributes.setExceptionEvents(exceptionsEvents);
        }

        return new NikitaResult(NikitaResult.StatusCode.OK, attributes);
    }

    @Override
    public void clean() {
        long currentTime = timeProvider.getCurrentTimeInMs();
        for (CorrelationRule rule : correlationRules) {
            rule.clean(currentTime);
        }
    }

    public static class Builder {
        private static final String MISSING_ARGUMENTS = "Missing required Nikita correlation engine properties";
        private Map<String, List<CorrelationRule>> alertToCorrelationRulesMap = new HashMap<>();
        private List<CorrelationRule> correlationRules;
        private TimeProvider timeProvider =  new TimeProvider();

        private List<Pair<String, String>> constants;
        private List<Pair<String, Object>> protections;
        private List<Pair<String, Object>> outputFields = new ArrayList<>();

        public Builder timeProvider(TimeProvider timeProvider) {
            this.timeProvider = timeProvider;
            return this;
        }

        public Builder correlationRules(List<CorrelationRule> rules) {
            this.correlationRules = rules;
            return this;
        }

        public Builder constants(List<Pair<String, String>> constants) {
            this.constants = constants;
            return this;
        }

        public Builder protections(List<Pair<String, Object>> protections) {
            this.protections = protections;
            return this;
        }

        public NikitaEngine build() {
            if (correlationRules == null
                    || correlationRules.isEmpty()
                    || constants == null
                    || protections == null
                    || timeProvider == null) {
                throw new IllegalArgumentException(MISSING_ARGUMENTS);
            }

            for (CorrelationRule rule : correlationRules) {
                List<String> alerts = rule.getAlertNames();
                for (String alert: alerts) {
                    if (!alertToCorrelationRulesMap.containsKey(alert)) {
                        alertToCorrelationRulesMap.put(alert, new ArrayList<>());
                    }
                    alertToCorrelationRulesMap.get(alert).add(rule);
                }
            }

            constants.forEach(x -> outputFields.add(ImmutablePair.of(x.getLeft(), x.getRight())));
            protections.forEach(x -> outputFields.add(ImmutablePair.of(x.getLeft(), x.getRight())));

            return new CorrelationEngineImpl(this);
        }
    }
}
