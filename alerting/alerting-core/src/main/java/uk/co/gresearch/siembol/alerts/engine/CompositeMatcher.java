package uk.co.gresearch.siembol.alerts.engine;

import uk.co.gresearch.siembol.alerts.common.EvaluationResult;
import uk.co.gresearch.siembol.alerts.compiler.MatcherType;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * An object for matching an event by evaluation of compositions of matchers
 *
 * <p>This class is for composing basic and composite matchers.
 * The matcher is not created for a field and can contain basic matchers registered on different fields.
 *
 *
 * @author  Marian Novotny
 * @see Matcher
 */
public class CompositeMatcher implements Matcher {
    private final Function<Map<String, Object>, EvaluationResult> evaluationFunction;
    private final boolean negated;
    private final boolean canModifyEvent;

    /**
     * Creates composite matcher using builder pattern.
     *
     * @param builder composite matcher builder
     */
    public CompositeMatcher(Builder builder) {
        this.evaluationFunction = builder.evaluationFunction;
        this.negated = builder.negated;
        this.canModifyEvent = builder.canModifyEvent;
    }
    /**
     * Match the event and returns evaluation result.
     * The event is evaluated by underlying matchers and combined by logical functions such as AND, OR.
     *
     * @param event map of string to object
     * @return      the evaluation result after evaluation
     * @see         EvaluationResult
     */
    @Override
    public EvaluationResult match(Map<String, Object> event) {
        EvaluationResult matchersResult = evaluationFunction.apply(event);
        return negated ? EvaluationResult.negate(matchersResult) : matchersResult;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canModifyEvent() {
        return canModifyEvent;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isNegated() {
        return negated;
    }

    /**
     * Creates Composite matcher builder instance.
     *
     * @return Contains matcher builder
     */
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
    /**
     * A builder for composite matchers
     *
     * <p>This class is using Builder pattern.
     *
     *
     * @author  Marian Novotny
     */
    public static class Builder {
        private static final String EMPTY_MATCHERS = "Empty matchers in the composite matcher";
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

        /**
         * Sets the matcher type for evaluating the composition
         *
         * @param matcherType COMPOSITE_OR or COMPOSITE_AND
         * @return this builder
         * @see MatcherType
         */
        public Builder matcherType(MatcherType matcherType) {
            this.matcherType = matcherType;
            return this;
        }

        /**
         * Sets the matcher is negated
         *
         * @param negated the matcher is negated
         * @return this builder
         */
        public Builder isNegated(boolean negated) {
            this.negated = negated;
            return this;
        }

        /**
         * Sets the list of underlying matchers that should be created in advance
         *
         * @param matchers the list of underlying matchers
         * @return this builder
         */
        public Builder matchers(List<Matcher> matchers) {
            this.matchers = matchers;
            return this;
        }

        /**
         * Builds the composite matcher
         *
         * @return composite matcher built from the builder state
         * @throws IllegalArgumentException in case of wrong arguments
         */
        public CompositeMatcher build() {
            if (matchers == null || matchers.isEmpty()) {
                throw new IllegalArgumentException(EMPTY_MATCHERS);
            }

            if (negated == null || matcherType == null) {
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
