package uk.co.gresearch.siembol.alerts.engine;

import uk.co.gresearch.siembol.alerts.common.EvaluationResult;
import uk.co.gresearch.siembol.common.utils.EvaluationLibrary;

import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Function;

/**
 * An object for numeric comparing a field with an expression - a constant or other field in an event
 *
 * <p>This derived class of BasicMatcher provides functionality for numeric comparing.
 * It supports custom comparator and
 * substituting variables using current map and comparing after the substitution.
 *
 * @author  Marian Novotny
 * @see BasicMatcher
 */
public class NumericCompareMatcher extends BasicMatcher {
    private static final String MISSING_ARGUMENTS_MSG = "Missing attributes in NumericMatcher";
    private static final String WRONG_CONSTANT_FORMAT = "Can not convert %s into a number";
    private final BiPredicate<Double, Double> comparator;
    private final Function<Map<String, Object>, Optional<Double>> valueSupplier;

    /**
     * Creates numeric comparison matcher using builder pattern.
     *
     * @param builder NumericCompare matcher builder
     */
    private NumericCompareMatcher(NumericCompareMatcher.Builder<?> builder) {
        super(builder);
        this.comparator = builder.comparator;
        this.valueSupplier = builder.valueSupplier;
    }

    /**
     * Interprets fieldValue as a number and compares it with an expression.
     * It substitutes the variable in expression if needed.
     *
     * @param map event as map of string to object
     * @param fieldValue value of the field for matching
     * @return EvaluationResult.MATCH if comparing numeric field with expression returns true,
     * otherwise EvaluationResult.NO_MATCH
     *
     */
    @Override
    protected EvaluationResult matchInternally(Map<String, Object> map, Object fieldValue) {
        var doubleFieldValue = getDoubleFromObject(fieldValue);
        if (doubleFieldValue.isEmpty()) {
            return EvaluationResult.NO_MATCH;
        }

        var valueToCompare = valueSupplier.apply(map);
        if (valueToCompare.isEmpty()) {
            return EvaluationResult.NO_MATCH;
        }

        return comparator.test(doubleFieldValue.get(), valueToCompare.get())
                ? EvaluationResult.MATCH
                : EvaluationResult.NO_MATCH;
    }

    private static Optional<Double> getDoubleFromObject(Object obj) {
        if (obj instanceof String) {
            try {
                var strValue = (String)obj;
                return Optional.of(Double.valueOf(strValue));
            } catch (Exception e) {
                return Optional.empty();
            }
        }

        if (obj instanceof Number) {
            return Optional.of(((Number)obj).doubleValue());
        }

        return Optional.empty();
    }

    private static Optional<Double> getDoubleFromVariableExpression(Map<String, Object> map,
                                                                    String expression) {
        var substituted = EvaluationLibrary.substitute(map, expression);
        if (substituted.isEmpty()) {
            return Optional.empty();
        }

        return getDoubleFromObject(substituted.get());
    }

    /**
     * Creates NumericCompare matcher builder instance.
     *
     * @return NumericCompare matcher builder
     */
    public static NumericCompareMatcher.Builder<NumericCompareMatcher> builder() {
        return new NumericCompareMatcher.Builder<>() {
            @Override
            public NumericCompareMatcher build() {
                if (expression == null || comparator == null) {
                    throw new IllegalArgumentException(MISSING_ARGUMENTS_MSG);
                }

                if (EvaluationLibrary.containsVariables(expression)) {
                    valueSupplier = x -> getDoubleFromVariableExpression(x, expression);
                } else {
                    final var constant = getDoubleFromObject(expression);
                    if (constant.isEmpty()) {
                        throw new IllegalArgumentException(
                                String.format(WRONG_CONSTANT_FORMAT, expression));
                    }
                    valueSupplier = x -> constant;
                }

                return new NumericCompareMatcher(this);
            }
        };
    }

    /**
     * A builder for NumericCompare matchers
     *
     * <p>This abstract class is derived from BasicMatcher.Builder class.
     *
     *
     * @author  Marian Novotny
     */
    public static abstract class Builder<T extends NumericCompareMatcher> extends BasicMatcher.Builder<T> {
        protected BiPredicate<Double, Double> comparator;
        protected Function<Map<String, Object>, Optional<Double>> valueSupplier;
        protected String expression;

        /**
         * Sets a numeric comparator in builder
         *
         * @param comparator numeric comparator bi-predicate that will be used during matching
         * @return this builder
         */
        public NumericCompareMatcher.Builder<T> comparator(BiPredicate<Double, Double> comparator) {
            this.comparator = comparator;
            return this;
        }

        /**
         * Sets a numeric expression in builder
         *
         * @param expression string numeric constant or a variable
         * @return this builder
         */
        public NumericCompareMatcher.Builder<T> expression(String expression) {
            this.expression = expression;
            return this;
        }
    }
}
