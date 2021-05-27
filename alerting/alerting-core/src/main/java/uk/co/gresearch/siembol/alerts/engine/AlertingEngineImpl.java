package uk.co.gresearch.siembol.alerts.engine;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import uk.co.gresearch.siembol.alerts.common.*;

import java.util.*;

public class AlertingEngineImpl implements AlertingEngine {
    private final String sourceField;
    private final Map<String, List<Rule>> sourceToRulesTable;
    private final List<Rule> allSourceRules;
    private final List<Pair<String, Object>> outputFields;

    private AlertingEngineImpl(Builder builder) {
        this.sourceToRulesTable = builder.sourceToRulesTable;
        this.outputFields = builder.outputFields;
        this.sourceField = builder.sourceField;
        this.allSourceRules = builder.allSourceRules;
    }

    @Override
    public AlertingResult evaluate(Map<String, Object> event) {
        if (!(event.get(sourceField) instanceof String)) {
            return AlertingResult.fromEvaluationResult(EvaluationResult.NO_MATCH, event);
        }
        String sensor = (String)event.get(sourceField);

        List<Map<String, Object>> outputEvents = new ArrayList<>();
        List<Map<String, Object>> exceptionsEvents = new ArrayList<>();

        List<Rule> sourceRules = sourceToRulesTable.get(sensor);
        if (sourceRules != null) {
            sourceRules.forEach(x -> evaluateRuleInternally(x, event, outputEvents, exceptionsEvents));
        }

        allSourceRules.forEach(x -> evaluateRuleInternally(x, event, outputEvents, exceptionsEvents));

        AlertingAttributes attributes = new AlertingAttributes();
        attributes.setEvaluationResult(outputEvents.isEmpty()
                ? EvaluationResult.NO_MATCH
                : EvaluationResult.MATCH);

        if (!exceptionsEvents.isEmpty()) {
            attributes.setExceptionEvents(exceptionsEvents);
        }

        if (!outputEvents.isEmpty()) {
            attributes.setOutputEvents(outputEvents);
        }

        return new AlertingResult(AlertingResult.StatusCode.OK, attributes);
    }

    @Override
    public AlertingEngineType getAlertingEngineType() {
        return AlertingEngineType.SIEMBOL_ALERTS;
    }

    private Map<String, Object> createEventFromRule(Rule rule, Map<String, Object> event) {
        Map<String, Object> ret = new HashMap<>(event);
        ret.put(AlertingFields.RULE_NAME.getAlertingName(), rule.getRuleName());
        ret.put(AlertingFields.FULL_RULE_NAME.getAlertingName(), rule.getFullRuleName());
        return ret;
    }

    private void evaluateRuleInternally(Rule rule,
                                        Map<String, Object> event,
                                        List<Map<String, Object>> outputEvents,
                                        List<Map<String, Object>> exceptionsEvents) {
        try {
            AlertingResult result = rule.match(event);
            if (result.getAttributes().getEvaluationResult() != EvaluationResult.MATCH) {
                return;
            }

            Map<String, Object> outEvent = createEventFromRule(rule,
                    result.getAttributes().getEvent());
            outputFields.forEach(x -> outEvent.put(x.getKey(), x.getValue()));
            rule.addOutputFieldsToEvent(outEvent);
            outputEvents.add(outEvent);
        } catch (Exception e) {
            Map<String, Object> outEvent = createEventFromRule(rule, event);
            outEvent.put(AlertingFields.EXCEPTION.getAlertingName(), ExceptionUtils.getStackTrace(e));
            exceptionsEvents.add(outEvent);
        }
    }

    public static class Builder {
        private static final String MISSING_ARGUMENTS = "Missing required alerting engine properties";
        private String sourceField = "source_type";
        private String wildcardSource = "*";
        private List<Pair<String, Rule>> rules;
        private Map<String, List<Rule>> sourceToRulesTable = new HashMap<>();
        private List<Rule> allSourceRules = new ArrayList<>();
        private List<Pair<String, String>> constants;
        private List<Pair<String, Object>> protections;
        private List<Pair<String, Object>> outputFields = new ArrayList<>();

        public Builder sourceField(String sourceField) {
            this.sourceField = sourceField;
            return this;
        }

        public Builder wildcardSource(String wildcardSource) {
            this.wildcardSource = wildcardSource;
            return this;
        }

        public Builder rules(List<Pair<String, Rule>> rules) {
            this.rules = rules;
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

        public AlertingEngine build() {
            if (rules == null
                    || rules.isEmpty()
                    || constants == null
                    || protections == null) {
                throw new IllegalArgumentException(MISSING_ARGUMENTS);
            }

            constants.forEach(x -> outputFields.add(ImmutablePair.of(x.getLeft(), x.getRight())));
            protections.forEach(x -> outputFields.add(ImmutablePair.of(x.getLeft(), x.getRight())));

            rules.forEach(x -> {
                if (sourceToRulesTable.containsKey(x.getLeft())) {
                    sourceToRulesTable.get(x.getLeft()).add(x.getRight());
                } else {
                    List<Rule> newList = new ArrayList<>();
                    newList.add(x.getRight());
                    sourceToRulesTable.put(x.getLeft(), newList);
                }
            });

            if (sourceToRulesTable.containsKey(wildcardSource)) {
                allSourceRules = sourceToRulesTable.get(wildcardSource);
                sourceToRulesTable.remove(wildcardSource);
            }

            return new AlertingEngineImpl(this);
        }
    }
}
