package uk.co.gresearch.siembol.alerts.engine;

import uk.co.gresearch.siembol.alerts.common.EvaluationResult;
import uk.co.gresearch.siembol.common.utils.EvaluationLibrary;

import java.util.*;
import java.util.function.BiPredicate;

public class ContainsMatcher extends BasicMatcher {
    enum Flags {
        CONTAINS_VARIABLE,
        CASE_INSENSITIVE,
        STARTS_WITH,
        ENDS_WITH
    }

    private final static String EMPTY_PATTERN_MSG = "Empty pattern in the Contains matcher";
    private final static String NULL_FIELD_VALUE = "Null field value during matching by Contains matcher";
    protected final EnumSet<Flags> flags;
    protected final String pattern;
    protected final BiPredicate<String, String> checkPredicate;

    private ContainsMatcher(ContainsMatcher.Builder<?> builder) {
        super(builder);
        this.flags = builder.flags;
        this.pattern = builder.pattern;
        this.checkPredicate = builder.checkPredicate;
    }

    @Override
    protected EvaluationResult matchInternally(Map<String, Object> map, String fieldValue) {
        if (fieldValue == null) {
            throw new IllegalStateException(NULL_FIELD_VALUE);
        }

        String stringToCheck = flags.contains(Flags.CASE_INSENSITIVE)
                ? fieldValue.toLowerCase()
                : fieldValue;

        String currentPattern = pattern;
        if (flags.contains(Flags.CONTAINS_VARIABLE)) {
            var evaluatedPattern = EvaluationLibrary.substitute(map, pattern);
            if (evaluatedPattern.isEmpty()) {
                return EvaluationResult.NO_MATCH;
            }
            currentPattern = flags.contains(Flags.CASE_INSENSITIVE)
                    ? evaluatedPattern.get().toLowerCase()
                    : evaluatedPattern.get();
        }

        return checkPredicate.test(stringToCheck, currentPattern)
                ? EvaluationResult.MATCH
                : EvaluationResult.NO_MATCH;
    }

    public static Builder<ContainsMatcher> builder() {

        return new Builder<>() {
            @Override
            public ContainsMatcher build() {
                if (pattern == null) {
                    throw new IllegalArgumentException(EMPTY_PATTERN_MSG);
                }

                if (flags.contains(Flags.CASE_INSENSITIVE) && !flags.contains(Flags.CONTAINS_VARIABLE)) {
                    pattern = pattern.toLowerCase();
                }

                checkPredicate = flags.containsAll(List.of(Flags.STARTS_WITH, Flags.ENDS_WITH))
                        ? String::equals : flags.contains(Flags.STARTS_WITH)
                        ? String::startsWith : flags.contains(Flags.ENDS_WITH)
                        ? String::endsWith : String::contains;

                return new ContainsMatcher(this);
            }
        };
    }

    public static abstract class Builder<T extends ContainsMatcher>
            extends BasicMatcher.Builder<T> {
        protected EnumSet<Flags> flags = EnumSet.noneOf(Flags.class);
        protected String pattern;
        protected BiPredicate<String, String> checkPredicate;

        public ContainsMatcher.Builder<T> isStartsWith(boolean startsWith) {
            if (startsWith) {
                flags.add(Flags.STARTS_WITH);
            }
            return this;
        }

        public ContainsMatcher.Builder<T> isEndsWith(boolean endsWith) {
            if (endsWith) {
                flags.add(Flags.ENDS_WITH);
            }
            return this;
        }

        public ContainsMatcher.Builder<T> isCaseInsensitiveCompare(boolean caseInsensitiveCompare) {
            if (caseInsensitiveCompare) {
                flags.add(Flags.CASE_INSENSITIVE);
            }
            return this;
        }

        public ContainsMatcher.Builder<T> data(String data) {
            if (data == null) {
                throw new IllegalArgumentException(EMPTY_PATTERN_MSG);
            }
            if (EvaluationLibrary.containsVariables(data)) {
                flags.add(Flags.CONTAINS_VARIABLE);
            }

            pattern = data;
            return this;
        }
    }
}
