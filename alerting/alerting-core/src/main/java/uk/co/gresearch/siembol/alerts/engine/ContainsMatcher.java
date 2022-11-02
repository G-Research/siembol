package uk.co.gresearch.siembol.alerts.engine;

import uk.co.gresearch.siembol.alerts.common.EvaluationResult;
import uk.co.gresearch.siembol.common.utils.EvaluationLibrary;

import java.util.*;
import java.util.function.BiPredicate;
/**
 * An object for basic matching of substring for an event
 *
 * <p>This derived class of BasicMatcher provides functionality for matching a substring.
 * It supports case-insensitive string comparisons and
 * substituting variables using current map and string search after the substitution and
 * specifying matching at start or end of the string.
 *
 * @author  Marian Novotny
 * @see BasicMatcher
 */
public class ContainsMatcher extends BasicMatcher {
    enum Flags {
        CONTAINS_VARIABLE,
        CASE_INSENSITIVE,
        STARTS_WITH,
        ENDS_WITH
    }

    private final static String EMPTY_PATTERN_MSG = "Empty pattern in the Contains matcher";
    protected final EnumSet<Flags> flags;
    protected final String pattern;
    protected final BiPredicate<String, String> checkPredicate;

    /**
     * Creates contains matcher using builder pattern.
     *
     * @param builder ContainsMatcher builder
     */
    private ContainsMatcher(ContainsMatcher.Builder<?> builder) {
        super(builder);
        this.flags = builder.flags;
        this.pattern = builder.pattern;
        this.checkPredicate = builder.checkPredicate;
    }

    /**
     * Evaluates fieldValue internally using substring search. It substitutes the variables if needed.
     * it supports case-insensitive compare if specified and checks for starting or ending requirements if needed.
     *
     * @param map event as map of string to object
     * @param fieldValue value of the field for matching
     * @return EvaluationResult.MATCH if field value contains the string otherwise EvaluationResult.NO_MATCH
     *
     */
    @Override
    protected EvaluationResult matchInternally(Map<String, Object> map, Object fieldValue) {
        var fieldStringValue = fieldValue.toString();
        var stringToCheck = flags.contains(Flags.CASE_INSENSITIVE)
                ? fieldStringValue.toLowerCase()
                : fieldStringValue;

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

    /**
     * Creates Contains matcher builder instance.
     *
     * @return Contains matcher builder
     */
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

    /**
     * A builder for Contains matchers
     *
     * <p>This abstract class is derived from BasicMatcher.Builder class.
     *
     *
     * @author  Marian Novotny
     */
    public static abstract class Builder<T extends ContainsMatcher>
            extends BasicMatcher.Builder<T> {
        protected EnumSet<Flags> flags = EnumSet.noneOf(Flags.class);
        protected String pattern;
        protected BiPredicate<String, String> checkPredicate;

        /**
         * Sets startsWith flag in builder
         *
         * @param startsWith matcher is checking whether the substring starts with the string
         * @return this builder
         */
        public ContainsMatcher.Builder<T> isStartsWith(boolean startsWith) {
            if (startsWith) {
                flags.add(Flags.STARTS_WITH);
            }
            return this;
        }

        /**
         * Sets endsWith flag in builder
         *
         * @param endsWith matcher is checking whether the substring ends with the string
         * @return this builder
         */
        public ContainsMatcher.Builder<T> isEndsWith(boolean endsWith) {
            if (endsWith) {
                flags.add(Flags.ENDS_WITH);
            }
            return this;
        }

        /**
         * Sets case-insensitive comparing in builder
         *
         * @param caseInsensitiveCompare matcher is comparing strings case-insensitively
         * @return this builder
         */
        public ContainsMatcher.Builder<T> isCaseInsensitiveCompare(boolean caseInsensitiveCompare) {
            if (caseInsensitiveCompare) {
                flags.add(Flags.CASE_INSENSITIVE);
            }
            return this;
        }

        /**
         * Sets input string to search
         *
         * @param data string to search
         * @return this builder
         */
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
