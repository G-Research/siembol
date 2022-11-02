package uk.co.gresearch.siembol.alerts.engine;

import uk.co.gresearch.siembol.alerts.common.EvaluationResult;
import uk.co.gresearch.siembol.alerts.common.AlertingResult;
import java.util.*;
/**
 * An object for alerting rule
 *
 * <p>This derived class of AbstractRule is implementing a standard alerting rule
 *
 *
 * @author  Marian Novotny
 * @see AbstractRule
 */
public class Rule extends AbstractRule {
    public enum RuleFlags {
        CAN_MODIFY_EVENT,
    }

    private static final String RULE_MATCH_FORMAT_STR = "Rule: %s matches with the event:";
    private final List<Matcher> matchers;
    private final EnumSet<RuleFlags> flags;

    /**
     * Creates rule using builder pattern.
     *
     * @param builder Rule builder
     */
    protected Rule(Builder<?> builder) {
        super(builder);
        this.matchers = builder.matchers;
        this.flags = builder.flags;
    }

    /**
     * Evaluates the rule by calling underlying matchers - all matchers are required to match to return MATCH result.
     * It includes the matching result with attributes in alerting result.
     * It creates a copy of the event if the rule can modify the event during the evaluation.
     *
     * @param event map of string to object
     * @return alerting result after evaluation
     * @see AlertingResult
     *
     */
    @Override
    public AlertingResult match(Map<String, Object> event) {
        Map<String, Object> current = canModifyEvent() ? new HashMap<>(event) : event;
        for (Matcher matcher : matchers) {
            EvaluationResult result = matcher.match(current);
            if (result == EvaluationResult.NO_MATCH) {
                return AlertingResult.fromEvaluationResult(EvaluationResult.NO_MATCH, current);
            }
        }

        if (logger.isActive()) {
            logger.appendMessage(String.format(RULE_MATCH_FORMAT_STR, getFullRuleName()));
            logger.appendMap(current);
        }

        return AlertingResult.fromEvaluationResult(EvaluationResult.MATCH, current);
    }

    /**
     * Provides information whether the rule can modify the event during evaluation.
     * It is used by match method
     *
     * @return true if the rule can modify the event otherwise false
     */
    public boolean canModifyEvent() {
        return flags.contains(RuleFlags.CAN_MODIFY_EVENT);
    }

    /**
     * A builder for an alerting rule
     *
     * <p>This abstract class is derived from AbstractRule.Builder class
     *
     *
     * @author  Marian Novotny
     */
    public static abstract class Builder<T extends Rule> extends AbstractRule.Builder<T>{
        protected static final String MISSING_MATCHERS = "Empty matchers in a rule";
        protected static final String NEGATED_MATCHERS_ONLY = "The rule contains negated matchers only";
        protected List<Matcher> matchers;
        protected EnumSet<RuleFlags> flags = EnumSet.noneOf(RuleFlags.class);

        /**
         * Sets the list of matchers
         *
         * @param matchers list of matchers
         * @return this builder
         */
        public Builder<T> matchers(List<Matcher> matchers) {
            this.matchers = matchers;
            return this;
        }

        /**
         * Sets the flags of the rule
         *
         * @param flags flags of the rule
         * @return this builder
         * @see RuleFlags
         */
        public Builder<T> flags(EnumSet<RuleFlags> flags) {
            this.flags = EnumSet.copyOf(flags);
            return this;
        }

        protected void prepareBuild() {
            if (matchers == null || matchers.isEmpty()) {
                throw new IllegalArgumentException(MISSING_MATCHERS);
            }

            boolean allNegatedMatchers = true;
            for (Matcher matcher : matchers) {
                if (matcher.canModifyEvent()) {
                    flags.add(RuleFlags.CAN_MODIFY_EVENT);
                }
                allNegatedMatchers &= matcher.isNegated();
            }
            if (allNegatedMatchers) {
                throw new IllegalArgumentException(NEGATED_MATCHERS_ONLY);
            }
        }
    }

    /**
     * Creates Rule builder instance
     *
     * @return Rule builder
     */
    public static Builder<Rule> builder() {

        return new Builder<>() {
            @Override
            protected Rule buildInternally() {
                prepareBuild();
                return new Rule(this);
            }
        };
    }
}
