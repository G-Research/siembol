package uk.co.gresearch.siembol.alerts.engine;

import uk.co.gresearch.siembol.alerts.common.EvaluationResult;
import uk.co.gresearch.siembol.alerts.compiler.MatcherType;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class CompositeMatcher implements Matcher {
    private final Function<Map<String, Object>, EvaluationResult> evaluationFunction;
    private final boolean negated;
    private final boolean canModifyEvent;

    public CompositeMatcher(Builder builder) {
        this.evaluationFunction = builder.evaluationFunction;
        this.negated = builder.negated;
        this.canModifyEvent = builder.canModifyEvent;
    }

    @Override
    public EvaluationResult match(Map<String, Object> log) {
        EvaluationResult matchersResult = evaluationFunction.apply(log);
        return negated ? EvaluationResult.negate(matchersResult) : matchersResult;
    }

    @Override
    public boolean canModifyEvent() {
        return canModifyEvent;
    }

    public static Builder builder() {
        return new Builder();
    }

    private static EvaluationResult evaluateOr(List<Matcher> matchers, Map<String, Object> log) {
        for (Matcher matcher : matchers) {
            if (EvaluationResult.MATCH == matcher.match(log)) {
                return EvaluationResult.MATCH;
            }
        }

        return EvaluationResult.NO_MATCH;
    }

    private static EvaluationResult evaluateAnd(List<Matcher> matchers, Map<String, Object> log) {
        for (Matcher matcher : matchers) {
            if (EvaluationResult.NO_MATCH == matcher.match(log)) {
                return EvaluationResult.NO_MATCH;
            }
        }

        return EvaluationResult.MATCH;
    }

    public static class Builder {
        private static final String WRONG_ARGUMENTS = "wrong arguments in the composite matcher";
        private static final String COMPOSITE_OR_MODIFY_EVENT_MSG = "COMPOSITE_OR matcher includes a matcher " +
                "that can modify an event";
        private static final String COMPOSITE_AND_MODIFY_EVENT_MSG = "COMPOSITE_AND matcher is negated and includes " +
                "a matcher that can modify an event and";

        private MatcherType matcherType;
        private Boolean negated;
        private List<Matcher> matchers;
        private Function<Map<String, Object>, EvaluationResult> evaluationFunction;
        private boolean canModifyEvent;

        public Builder matcherType(MatcherType matcherType) {
            this.matcherType = matcherType;
            return this;
        }

        public Builder isNegated(boolean negated) {
            this.negated = negated;
            return this;
        }

        public Builder matchers(List<Matcher> matchers) {
            this.matchers = matchers;
            return this;
        }

        public CompositeMatcher build() {
            if (negated == null
                    || matchers == null || matchers.isEmpty()
                    || matcherType == null) {
                throw new IllegalArgumentException(WRONG_ARGUMENTS);
            }

            canModifyEvent = false;
            matchers.forEach(x -> canModifyEvent |= x.canModifyEvent());

            switch (matcherType) {
                case COMPOSITE_OR:
                    if (canModifyEvent) {
                        throw new IllegalArgumentException(COMPOSITE_OR_MODIFY_EVENT_MSG);
                    }
                    evaluationFunction = x -> evaluateOr(matchers, x);
                    break;
                case COMPOSITE_AND:
                    if (negated && canModifyEvent) {
                        throw new IllegalArgumentException(COMPOSITE_AND_MODIFY_EVENT_MSG);
                    }
                    evaluationFunction = x -> evaluateAnd(matchers, x);
                    break;
                default:
                    throw new IllegalArgumentException(WRONG_ARGUMENTS);
            }
            return new CompositeMatcher(this);
        }
    }
}
