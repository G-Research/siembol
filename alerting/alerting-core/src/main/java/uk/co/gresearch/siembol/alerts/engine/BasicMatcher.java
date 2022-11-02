package uk.co.gresearch.siembol.alerts.engine;

import uk.co.gresearch.siembol.alerts.common.EvaluationResult;
import java.util.Map;

/**
 * An object for basic matching an event
 *
 * <p>This abstract class is using template pattern for handling common functionality of all basic matchers.
 * The matcher is created for a field.
 *
 *
 * @author  Marian Novotny
 * @see Matcher
 * @see IsInSetMatcher
 * @see ContainsMatcher
 * @see RegexMatcher
 * @see NumericCompareMatcher
 */
public abstract class BasicMatcher implements Matcher {
    private final static String MISSING_FIELD_NAME = "Missing field name in a basic matcher";
    private final String fieldName;
    private final boolean isNegated;

    /**
     * Creates basic matcher using builder pattern.
     *
     * @param builder Basic matcher builder
     */
    protected BasicMatcher(Builder<?> builder) {
        if (builder.fieldName == null) {
            throw new IllegalArgumentException(MISSING_FIELD_NAME);
        }

        this.fieldName = builder.fieldName;
        this.isNegated = builder.isNegated;
    }

    /**
     * Extracts fieldValue from the event and calls matchInternally abstract method.
     * It negates the result if the matcher is negated.
     *
     * @param map map of string to object
     * @return the evaluation result after evaluation
     * @see EvaluationResult
     */
    public EvaluationResult match(Map<String, Object> map) {
        if (map.get(fieldName) == null) {
            return isNegated ? EvaluationResult.MATCH : EvaluationResult.NO_MATCH;
        }

        var fieldValue = map.get(fieldName);
        EvaluationResult result = matchInternally(map, fieldValue);

        if (isNegated) {
            result = EvaluationResult.negate(result);
        }

        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canModifyEvent() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isNegated() {
        return isNegated;
    }

    /**
     * Abstract method to be implemented in derived classes. Evaluates fieldValue internally using event if necessary.
     * It returns matching statues without considering negated property.
     *
     *
     * @param map event as map of string to object
     * @param fieldValue value of the field for matching
     * @return matching result after evaluation
     *
     */
    protected abstract EvaluationResult matchInternally(Map<String, Object> map, Object fieldValue);

    /**
     * An abstract builder for basic matchers
     *
     * <p>This abstract class is using Builder pattern.
     *
     *
     * @author  Marian Novotny
     */
    public static abstract class Builder<T extends BasicMatcher> {
        private String fieldName;
        private boolean isNegated = false;

        /**
         * Sets fieldName in builder
         *
         * @param name field in which matcher will be registered
         * @return this builder
         */
        public Builder<T> fieldName(String name) {
            this.fieldName = name;
            return this;
        }

        /**
         * Sets negated in builder
         *
         * @param isNegated matcher is negated
         * @return this builder
         */
        public Builder<T> isNegated(boolean isNegated) {
            this.isNegated = isNegated;
            return this;
        }
        public abstract T build();
    }
}

