package uk.co.gresearch.siembol.alerts.correlationengine;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import uk.co.gresearch.siembol.common.utils.TimeProvider;
import uk.co.gresearch.siembol.alerts.common.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import static uk.co.gresearch.siembol.alerts.common.AlertingTags.CORRELATION_KEY_TAG_NAME;
/**
 * An object that evaluates alerts using correlation rules
 *
 * <p>This class implements AlertingEngine interface for evaluating alerts using correlation rules.
 *
 * @author  Marian Novotny
 * @see AlertingEngine
 *
 */
public class CorrelationEngineImpl implements AlertingEngine {
    private static final String MISSING_CORRELATION_ATTRIBUTES = "Missing fields for alert correlation";
    private final Map<String, List<CorrelationRule>> alertToCorrelationRulesMap;
    private final List<CorrelationRule> correlationRules;
    private final TimeProvider timeProvider;
    private final List<Pair<String, Object>> outputFields;

    /**
     * Creates correlation engine using builder pattern.
     *
     * @param builder correlation engine builder
     */
    CorrelationEngineImpl(Builder builder) {
        alertToCorrelationRulesMap = builder.alertToCorrelationRulesMap;
        correlationRules = builder.correlationRules;
        timeProvider = builder.timeProvider;
        this.outputFields = builder.outputFields;
    }

    /**
     * Evaluate alert using correlation rules and returns alerting result with
     * a matching result and additional attributes such as matching events or exceptions.
     * It returns matches of all rules.
     * The rule is correlated based on correlation key field in the alert and
     * alerting rule name field is used for counting alerts in the rules.
     *
     * @param alert deserialized event as map of string to object
     * @return      alerting result after evaluation
     * @see         AlertingResult
     */
    @Override
    public AlertingResult evaluate(Map<String, Object> alert) {
        if (!(alert.get(AlertingFields.RULE_NAME.getAlertingName()) instanceof String)
                || !(alert.get(CORRELATION_KEY_TAG_NAME.toString()) instanceof String)) {
            return  AlertingResult.fromErrorMessage(MISSING_CORRELATION_ATTRIBUTES);
        }

        String alertName = (String)alert.get(AlertingFields.RULE_NAME.getAlertingName());
        if (!alertToCorrelationRulesMap.containsKey(alertName)) {
            return AlertingResult.fromEvaluationResult(EvaluationResult.NO_MATCH, alert);
        }

        alert.put(AlertingFields.PROCESSING_TIME.getCorrelationAlertingName(), timeProvider.getCurrentTimeInMs());
        List<Map<String, Object>> outputCorrelationAlerts = new ArrayList<>();
        List<Map<String, Object>> exceptionsEvents = new ArrayList<>();

        for (CorrelationRule correlationRule : alertToCorrelationRulesMap.get(alertName)) {
            AlertingResult result = correlationRule.match(alert);
            if (result.getStatusCode() == AlertingResult.StatusCode.ERROR) {
                exceptionsEvents.add(result.getAttributes().getEvent());
            } else if (result.getAttributes().getEvaluationResult() == EvaluationResult.MATCH) {
                Map<String, Object> outAlert = result.getAttributes().getEvent();
                outputFields.forEach(x -> outAlert.put(x.getKey(), x.getValue()));
                correlationRule.addOutputFieldsToEvent(outAlert);
                outputCorrelationAlerts.add(outAlert);
            }
        }

        AlertingAttributes attributes = new AlertingAttributes();
        attributes.setEvaluationResult(outputCorrelationAlerts.isEmpty() ? EvaluationResult.NO_MATCH
                : EvaluationResult.MATCH);

        if (!outputCorrelationAlerts.isEmpty()) {
            attributes.setOutputEvents(outputCorrelationAlerts );
        }

        if (!exceptionsEvents.isEmpty()) {
            attributes.setExceptionEvents(exceptionsEvents);
        }

        return new AlertingResult(AlertingResult.StatusCode.OK, attributes);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AlertingEngineType getAlertingEngineType() {
        return AlertingEngineType.SIEMBOL_CORRELATION_ALERTS;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clean() {
        long currentTime = timeProvider.getCurrentTimeInMs();
        for (CorrelationRule rule : correlationRules) {
            rule.clean(currentTime);
        }
    }

    /**
     * A builder for correlation alerting engine
     *
     * <p>This class is using Builder pattern.
     *
     *
     * @author  Marian Novotny
     */
    public static class Builder {
        private static final String MISSING_ARGUMENTS = "Missing required correlation alerting engine properties";
        private Map<String, List<CorrelationRule>> alertToCorrelationRulesMap = new HashMap<>();
        private List<CorrelationRule> correlationRules;
        private TimeProvider timeProvider =  new TimeProvider();

        private List<Pair<String, String>> constants;
        private List<Pair<String, Object>> protections;
        private List<Pair<String, Object>> outputFields = new ArrayList<>();

        /**
         * Sets time provider for providing the current time
         *
         * @param timeProvider time provider
         * @return this builder
         */
        public Builder timeProvider(TimeProvider timeProvider) {
            this.timeProvider = timeProvider;
            return this;
        }

        /**
         * Sets correlation rules that should be prepared in advance
         *
         * @param rules list of correlation alerting rules
         * @return this builder
         */
        public Builder correlationRules(List<CorrelationRule> rules) {
            this.correlationRules = rules;
            return this;
        }

        /**
         * Sets key-value pairs that will be put into the event after matching a rule
         *
         * @param constants list of key-value pairs
         * @return this builder
         */
        public Builder constants(List<Pair<String, String>> constants) {
            this.constants = constants;
            return this;
        }

        /**
         * Sets key-value pairs with rule protection information
         * that will be put into the event after matching a rule
         *
         * @param protections list of key-value pairs
         * @return this builder
         */
        public Builder protections(List<Pair<String, Object>> protections) {
            this.protections = protections;
            return this;
        }

        /**
         * Builds the correlation alerting engine
         *
         * @return correlation engine built from the builder state
         * @throws IllegalArgumentException in case of wrong arguments
         */
        public AlertingEngine build() {
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
