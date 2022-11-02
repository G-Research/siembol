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
/**
 * An object for alerting rule
 *
 * <p>This abstract class is using template pattern for handling common functionality of all alerting rules.
 *
 *
 * @author  Marian Novotny
 * @see Rule
 * @see uk.co.gresearch.siembol.alerts.correlationengine.CorrelationRule
 */
public abstract class AbstractRule {
    private final String ruleName;
    private final String fullRuleName;

    private final List<Pair<String, Object>> outputFields;
    private final List<Pair<String, String>> variableOutputFields;

    protected final TestingLogger logger;
    /**
     * Creates rule using builder pattern
     *
     * @param builder abstract rule builder
     */
    protected AbstractRule(Builder<?> builder) {
        this.ruleName = builder.ruleName;
        this.fullRuleName = builder.fullRuleName;
        this.outputFields = builder.outputFields;
        this.variableOutputFields = builder.variableOutputFields;
        this.logger = builder.logger;
    }

    /**
     * Provides rule name
     *
     * @return the name of the rule
     */
    public String getRuleName() {
        return ruleName;
    }

    /**
     * Provides full rule name including the rule version
     *
     * @return the name of the rule including the version
     */
    public String getFullRuleName() {
        return fullRuleName;
    }

    /**
     * Puts metadata about the rule into the event
     *
     * @param event the metadata will be put in the event map
     */
    public void addOutputFieldsToEvent(Map<String, Object> event) {
        outputFields.forEach(x -> event.put(x.getKey(), x.getValue()));
        for (Pair<String, String> variableOutputField : variableOutputFields) {
            Optional<String> value = EvaluationLibrary.substitute(event, variableOutputField.getValue());
            value.ifPresent(x -> event.put(variableOutputField.getKey(), x));
        }
    }

    /**
     * Abstract method to be implemented in derived classes.
     * Evaluates the rule and includes the matching result with attributes in alerting result.
     *
     * @param event map of string to object
     * @return alerting result after evaluation
     * @see AlertingResult
     *
     */
    public abstract AlertingResult match(Map<String, Object> event);

    /**
     * An abstract builder for alerting rules
     *
     * <p>This abstract class is using Builder pattern.
     *
     *
     * @author  Marian Novotny
     */
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

        /**
         * Builds the alerting rule
         *
         * @return alerting rule built from the builder state and by calling buildInternally method
         * @throws IllegalArgumentException in case of wrong arguments
         */
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

        /**
         * Sets name of the rule in builder
         *
         * @param name name of the rule
         * @return this builder
         */
        public Builder<T> name(String name) {
            this.ruleName = name;
            return this;
        }

        /**
         * Sets version of the rule in builder
         *
         * @param version version of the rule
         * @return this builder
         */
        public Builder<T> version(Integer version) {
            this.ruleVersion = version;
            return this;
        }

        /**
         * Sets the tags - list of key value pairs
         *
         * @param tags list of key value pairs. Values can include variables for substitution.
         * @return this builder
         */
        public Builder<T> tags(List<Pair<String, String>> tags) {
            this.tags = tags;
            return this;
        }

        /**
         * Sets the protections - list of  key value pairs with maximum allowed matches
         *
         * @param protections list of key value pairs for rule protection
         * @return this builder
         */
        public Builder<T> protections(List<Pair<String, Object>> protections) {
            this.protections = protections;
            return this;
        }

        /**
         * Sets the testing logger
         *
         * @param logger testing logger with debugging information about matching
         * @return this builder
         * @see TestingLogger
         */
        public Builder<T> logger(TestingLogger logger) {
            this.logger = logger;
            return this;
        }
    }
}
