package uk.co.gresearch.siembol.alerts.engine;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import uk.co.gresearch.siembol.common.testing.InactiveTestingLogger;
import uk.co.gresearch.siembol.common.utils.EvaluationLibrary;
import uk.co.gresearch.siembol.alerts.common.AlertingResult;
import uk.co.gresearch.siembol.common.testing.TestingLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public abstract class AbstractRule {
    private final String ruleName;
    private final String fullRuleName;

    private final List<Pair<String, Object>> outputFields;
    private final List<Pair<String, String>> variableOutputFields;

    protected final TestingLogger logger;
    protected AbstractRule(Builder<?> builder) {
        this.ruleName = builder.ruleName;
        this.fullRuleName = builder.fullRuleName;
        this.outputFields = builder.outputFields;
        this.variableOutputFields = builder.variableOutputFields;
        this.logger = builder.logger;
    }

    public String getRuleName() {
        return ruleName;
    }

    public String getFullRuleName() {
        return fullRuleName;
    }

    public void addOutputFieldsToEvent(Map<String, Object> event) {
        outputFields.forEach(x -> event.put(x.getKey(), x.getValue()));
        for (Pair<String, String> variableOutputField : variableOutputFields) {
            Optional<String> value = EvaluationLibrary.substitute(event, variableOutputField.getValue());
            if (value.isPresent()) {
                event.put(variableOutputField.getKey(), value.get());
            }
        }
    }

    public abstract AlertingResult match(Map<String, Object> log);

    public static abstract class Builder<T extends AbstractRule> {
        private static final String MISSING_ARGUMENTS = "Missing required rule properties";
        private String ruleName;
        private String fullRuleName;
        private Integer ruleVersion;
        private List<Pair<String, String>> tags = new ArrayList<>();
        private List<Pair<String, Object>> protections = new ArrayList<>();
        private List<Pair<String, Object>> outputFields = new ArrayList<>();
        private List<Pair<String, String>> variableOutputFields = new ArrayList<>();
        private TestingLogger logger =  new InactiveTestingLogger();

        protected abstract T buildInternally();

        public T build() {
            if (ruleName == null
                    || ruleVersion == null) {
                throw new IllegalArgumentException(MISSING_ARGUMENTS);
            }

            fullRuleName = String.format("%s_v%d", ruleName, ruleVersion);

            for (Pair<String, String> tag : tags) {
                if (EvaluationLibrary.containsVariables(tag.getValue())) {
                    variableOutputFields.add(ImmutablePair.of(tag.getLeft(), tag.getRight()));
                } else {
                    outputFields.add(ImmutablePair.of(tag.getLeft(), tag.getRight()));
                }
            }

            protections.forEach(x -> outputFields.add(ImmutablePair.of(x.getLeft(), x.getRight())));

            return buildInternally();
        }

        public Builder<T> name(String name) {
            this.ruleName = name;
            return this;
        }

        public Builder<T> version(Integer version) {
            this.ruleVersion = version;
            return this;
        }

        public Builder<T> tags(List<Pair<String, String>> tags) {
            this.tags = tags;
            return this;
        }

        public Builder<T> protections(List<Pair<String, Object>> protections) {
            this.protections = protections;
            return this;
        }

        public Builder<T> logger(TestingLogger logger) {
            this.logger = logger;
            return this;
        }
    }

}
