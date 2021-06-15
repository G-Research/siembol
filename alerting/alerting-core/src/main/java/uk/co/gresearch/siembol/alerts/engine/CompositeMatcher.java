package uk.co.gresearch.siembol.alerts.engine;

import uk.co.gresearch.siembol.alerts.common.EvaluationResult;
import uk.co.gresearch.siembol.alerts.compiler.MatcherType;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class CompositeMatcher implements Matcher {
    private final Function<Map<String, Object>, EvaluationResult> evaluationFunction;
    private final boolean negated;

    public CompositeMatcher(Builder builder) {
        this.evaluationFunction = builder.evaluationFunction;
        this.negated = builder.negated;
    }

    @Override
    public EvaluationResult match(Map<String, Object> log) {
        EvaluationResult matchersResult = evaluationFunction.apply(log);
        return negated ? EvaluationResult.negate(matchersResult) : matchersResult;
    }

    @Override
    public boolean canModifyEvent() {
        return false;
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
        private MatcherType matcherType;
        private Boolean negated;
        private List<Matcher> matchers;
        private Function<Map<String, Object>, EvaluationResult> evaluationFunction;

        public Builder(MatcherType matcherType) {
            this.matcherType = matcherType;
        }

        public Builder negated(boolean negated) {
            this.negated = negated;
            return this;
        }

        public Builder matchers(List<Matcher> matchers) {
            this.matchers = matchers;
            return this;
        }

        public Matcher build() {
            if (negated == null
                    || matchers == null || matchers.isEmpty()) {
                throw new IllegalArgumentException(WRONG_ARGUMENTS);
            }
            matchers.forEach(x -> {
                if (x.canModifyEvent()) {
                    throw new IllegalArgumentException();
                }
            });
            switch (matcherType) {
                case COMPOSITE_OR:
                    evaluationFunction = x -> evaluateOr(matchers, x);
                    break;
                case COMPOSITE_AND:
                    evaluationFunction = x -> evaluateAnd(matchers, x);
                default:
                    throw new IllegalArgumentException();
            }
            return new CompositeMatcher(this);
        }
    }
}
