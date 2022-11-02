package uk.co.gresearch.siembol.alerts.engine;

import uk.co.gresearch.siembol.common.utils.EvaluationLibrary;
import uk.co.gresearch.siembol.alerts.common.EvaluationResult;

import java.util.*;
import java.util.stream.Collectors;
/**
 * An object for basic matching of a set of strings for an event
 *
 * <p>This derived class of BasicMatcher provides functionality for matching set of strings.
 * It supports case-insensitive string comparisons and
 * substituting variables using current map and matching the field after the substitution.
 *
 * @author  Marian Novotny
 * @see BasicMatcher
 */
public class IsInSetMatcher extends BasicMatcher {
    private final static String EMPTY_SET_OF_STRING = "Empty constantStrings of string in the matcher";
    private final Set<String> constantStrings;
    private final List<String> variableStrings;
    private final boolean caseInsensitiveCompare;

    /**
     * Creates is in set matcher using builder pattern.
     *
     * @param builder IsInSetMatcher builder
     */
    private IsInSetMatcher(Builder<?> builder) {
        super(builder);
        this.constantStrings = builder.constantStrings;
        this.variableStrings = builder.variableStrings;
        this.caseInsensitiveCompare = builder.caseInsensitiveCompare;
    }

    /**
     * Evaluates fieldValue internally using set of strings. It substitutes the variables if needed.
     * it supports case-insensitive compare if specified.
     *
     * @param map event as map of string to object
     * @param fieldValue value of the field for matching
     * @return EvaluationResult.MATCH if one string matches otherwise EvaluationResult.NO_MATCH
     *
     */
    @Override
    protected EvaluationResult matchInternally(Map<String, Object> map, Object fieldValue) {
        var fieldStringValue = fieldValue.toString();
        var stringToMatch = caseInsensitiveCompare
                ? fieldStringValue.toLowerCase()
                : fieldStringValue;

        boolean matchedVariable = false;
        for (String variableString : variableStrings) {
            Optional<String> substituted = EvaluationLibrary.substitute(map, variableString);
            if (substituted.isEmpty()) {
                continue;
            }

            String current = caseInsensitiveCompare ? substituted.get().toLowerCase() : substituted.get();
            if (current.equals(stringToMatch)) {
                matchedVariable = true;
                break;
            }
        }
        return matchedVariable || constantStrings.contains(stringToMatch)
                ? EvaluationResult.MATCH
                : EvaluationResult.NO_MATCH;
    }

    /**
     * Creates IsInSet matcher builder instance.
     *
     * @return IsInSet matcher builder
     */
    public static Builder<IsInSetMatcher> builder() {

        return new Builder<>() {
            @Override
            public IsInSetMatcher build() {
                if (words == null || words.isEmpty()) {
                    throw new IllegalArgumentException(EMPTY_SET_OF_STRING);
                }

                constantStrings = words.stream()
                        .filter(x -> !EvaluationLibrary.containsVariables(x))
                        .map(x -> caseInsensitiveCompare ? x.toLowerCase() : x)
                        .collect(Collectors.toCollection(HashSet::new));

                variableStrings = words.stream()
                        .filter(x -> !constantStrings.contains(x))
                        .collect(Collectors.toList());

                return new IsInSetMatcher(this);
            }
        };
    }

    /**
     * A builder for IsInSet matchers
     *
     * <p>This abstract class is derived from BasicMatcher.Builder class.
     *
     *
     * @author  Marian Novotny
     */
    public static abstract class Builder<T extends IsInSetMatcher>
            extends BasicMatcher.Builder<T> {
        private String wordDelimiter = "\n";
        protected boolean caseInsensitiveCompare = false;
        protected List<String> words;
        protected Set<String> constantStrings;
        protected List<String> variableStrings;

        /**
         * Sets wordDelimiter in builder
         *
         * @param wordDelimiter is used for splitting words from data string
         * @return this builder
         */
        public IsInSetMatcher.Builder<T> wordDelimiter(String wordDelimiter) {
            this.wordDelimiter = wordDelimiter;
            return this;
        }

        /**
         * Sets case-insensitive comparing in builder
         *
         * @param caseInsensitiveCompare matcher is comparing strings case-insensitively
         * @return this builder
         */
        public IsInSetMatcher.Builder<T> isCaseInsensitiveCompare(boolean caseInsensitiveCompare) {
            this.caseInsensitiveCompare = caseInsensitiveCompare;
            return this;
        }

        /**
         * Sets words as one string in builder
         *
         * @param data set of words in one string delimited using word delimiter
         * @return this builder
         */
        public IsInSetMatcher.Builder<T> data(String data) {
            String[] words = data.split(wordDelimiter);
            return words(Arrays.asList(words));
        }

        /**
         * Sets words as list of strings in builder
         *
         * @param words list of words for
         * @return this builder
         */
        public IsInSetMatcher.Builder<T> words(List<String> words) {
            this.words = words;
            return this;
        }
    }
}
