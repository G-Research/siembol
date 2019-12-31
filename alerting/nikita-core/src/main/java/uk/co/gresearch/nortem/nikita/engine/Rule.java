package uk.co.gresearch.nortem.nikita.engine;

import uk.co.gresearch.nortem.nikita.common.EvaluationResult;
import uk.co.gresearch.nortem.nikita.common.NikitaResult;
import java.util.*;

public class Rule extends AbstractRule {
    public enum RuleFlags {
        CAN_MODIFY_EVENT,
    }

    private static final String RULE_MATCH_FORMAT_STR = "Rule: %s matches with the event:";
    private final List<RuleMatcher> matchers;
    private final EnumSet<RuleFlags> flags;

    protected Rule(Builder<?> builder) {
        super(builder);
        this.matchers = builder.matchers;
        this.flags = builder.flags;
    }

    @Override
    public NikitaResult match(Map<String, Object> log) {
        Map<String, Object> current = canModifyEvent() ? new HashMap<>(log) : log;
        for (RuleMatcher matcher : matchers) {
            EvaluationResult result = matcher.match(current);
            if (result == EvaluationResult.NO_MATCH) {
                return NikitaResult.fromEvaluationResult(EvaluationResult.NO_MATCH, current);
            }
        }

        if (logger.isActive()) {
            logger.appendMessage(String.format(RULE_MATCH_FORMAT_STR, getFullRuleName()));
            logger.appendMap(current);
        }

        return NikitaResult.fromEvaluationResult(EvaluationResult.MATCH, current);
    }


    public boolean canModifyEvent() {
        return flags.contains(RuleFlags.CAN_MODIFY_EVENT);
    }

    public static abstract class Builder<T extends Rule> extends AbstractRule.Builder<T>{
        protected static final String MISSING_MATCHERS = "Missing matchers in nikita rule builder";
        protected List<RuleMatcher> matchers;
        protected EnumSet<RuleFlags> flags = EnumSet.noneOf(RuleFlags.class);

        public Builder<T> matchers(List<RuleMatcher> matchers) {
            this.matchers = matchers;
            return this;
        }

        public Builder<T> flags(EnumSet<RuleFlags> flags) {
            this.flags = EnumSet.copyOf(flags);
            return this;
        }

        protected void prepareBuild() {
            if (matchers == null || matchers.isEmpty()) {
                throw new IllegalArgumentException(MISSING_MATCHERS);
            }
            for (RuleMatcher matcher : matchers) {
                if (matcher.CanModifyEvent()) {
                    flags.add(RuleFlags.CAN_MODIFY_EVENT);
                    break;
                }
            }
        }
    }

    public static Builder<Rule> builder() {

        return new Builder<Rule>() {
            @Override
            protected Rule buildInternally() {
                prepareBuild();
                return new Rule(this);
            }
        };
    }
}
