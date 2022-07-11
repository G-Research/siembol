package uk.co.gresearch.siembol.alerts.engine;

import uk.co.gresearch.siembol.alerts.common.EvaluationResult;
import uk.co.gresearch.siembol.common.utils.EvaluationLibrary;

import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Function;

public class NumericCompareMatcher extends BasicMatcher {
    private static final String MISSING_ARGUMENTS_MSG = "Missing attributes in NumericMatcher";
    private static final String WRONG_CONSTANT_FORMAT = "Can not convert %s into a number";
    private final BiPredicate<Double, Double> comparator;
    private final Function<Map<String, Object>, Optional<Double>> valueSupplier;

    private NumericCompareMatcher(NumericCompareMatcher.Builder<?> builder) {
        super(builder);
        this.comparator = builder.comparator;
        this.valueSupplier = builder.valueSupplier;
    }

    @Override
    protected EvaluationResult matchInternally(Map<String, Object> map, Object fieldValue) {
        var doubleFieldValue = getDoubleFrom(fieldValue);
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

    private static Optional<Double> getDoubleFrom(Object obj) {
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

    private static Optional<Double> getValueFromVariable(Map<String, Object> map,
                                                        String expression) {
        var substituted = EvaluationLibrary.substitute(map, expression);
        if (substituted.isEmpty()) {
            return Optional.empty();
        }

        return getDoubleFrom(substituted.get());
    }

    public static NumericCompareMatcher.Builder<NumericCompareMatcher> builder() {

        return new NumericCompareMatcher.Builder<>() {
            @Override
            public NumericCompareMatcher build() {
                if (expression == null || comparator == null) {
                    throw new IllegalArgumentException(MISSING_ARGUMENTS_MSG);
                }

                if (EvaluationLibrary.containsVariables(expression)) {
                    valueSupplier = x -> getValueFromVariable(x, expression);
                } else {
                    final var constant = getDoubleFrom(expression);
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

    public static abstract class Builder<T extends NumericCompareMatcher> extends BasicMatcher.Builder<T> {
        protected BiPredicate<Double, Double> comparator;
        protected Function<Map<String, Object>, Optional<Double>> valueSupplier;
        protected String expression;

        public NumericCompareMatcher.Builder<T> comparator(BiPredicate<Double, Double> comparator) {
            this.comparator = comparator;
            return this;
        }

        public NumericCompareMatcher.Builder<T> expression(String expression) {
            this.expression = expression;
            return this;
        }
    }
}
