package uk.co.gresearch.siembol.alerts.engine;

import uk.co.gresearch.siembol.common.utils.EvaluationLibrary;
import uk.co.gresearch.siembol.alerts.common.EvaluationResult;

import java.util.*;
import java.util.stream.Collectors;

public class IsInSetMatcher extends BasicMatcher {
    private final static String EMPTY_SET_OF_STRING = "Empty constantStrings of string in the matcher";
    private final Set<String> constantStrings;
    private final List<String> variableStrings;
    private final boolean caseInsensitiveCompare;

    private IsInSetMatcher(Builder<?> builder) {
        super(builder);
        this.constantStrings = builder.constantStrings;
        this.variableStrings = builder.variableStrings;
        this.caseInsensitiveCompare = builder.caseInsensitiveCompare;
    }

    @Override
    protected EvaluationResult matchInternally(Map<String, Object> map, String fieldValue) {
        String stringToMatch = caseInsensitiveCompare && fieldValue != null
                ? fieldValue.toLowerCase()
                : fieldValue;

        boolean matchedVariable = false;
        for (String variableString : variableStrings) {
            Optional<String> substituted = EvaluationLibrary.substitute(map, variableString);
            if (!substituted.isPresent()) {
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

    public static Builder<IsInSetMatcher> builder() {

        return new Builder<IsInSetMatcher>() {
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

    public static abstract class Builder<T extends IsInSetMatcher>
            extends BasicMatcher.Builder<T> {
        private String wordDelimiter = "\n";
        protected boolean caseInsensitiveCompare = false;
        protected List<String> words;
        protected Set<String> constantStrings;
        protected List<String> variableStrings;

        public IsInSetMatcher.Builder<T> wordDelimiter(String wordDelimiter) {
            this.wordDelimiter = wordDelimiter;
            return this;
        }

        public IsInSetMatcher.Builder<T> isCaseInsensitiveCompare(boolean caseInsensitiveCompare) {
            this.caseInsensitiveCompare = caseInsensitiveCompare;
            return this;
        }

        public IsInSetMatcher.Builder<T> data(String data) {
            String[] words = data.split(wordDelimiter);
            return words(Arrays.asList(words));
        }

        public IsInSetMatcher.Builder<T> words(List<String> words) {
            this.words = words;
            return this;
        }
    }
}
