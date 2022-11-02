package uk.co.gresearch.siembol.alerts.engine;

import uk.co.gresearch.siembol.alerts.common.EvaluationResult;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 * An object for basic regular matching of an event
 *
 * <p>This derived class of BasicMatcher provides functionality for regular expression matching.
 * It supports extracting fields and put them into an event using regular expression named groups.
 *
 * @author  Marian Novotny
 * @see BasicMatcher
 */
public class RegexMatcher extends BasicMatcher {
    private static final String EMPTY_PATTERN = "Empty pattern";
    private static final Pattern VARIABLE_PATTERN =
            Pattern.compile("\\(\\?<([a-zA-Z][a-zA-Z\\d:_]*)>");
    private static final String VARIABLE_NAME = "var";
    private static final int VAR_PREFIX_SIZE = "(\\<".length();

    private final Pattern pattern;
    private final List<String> variableNames;

    /**
     * Creates regex matcher using builder pattern.
     *
     * @param builder Regex matcher builder
     */
    private RegexMatcher(Builder<?> builder) {
        super(builder);
        this.pattern = builder.pattern;
        this.variableNames = builder.variableNames;
    }

    /**
     * Provides information whether the matcher can modify the event and
     * the caller needs to consider it before matching.
     *
     * @return true if the regex matcher contains variables - named group matching
     */
    @Override
    public boolean canModifyEvent() {
        return !variableNames.isEmpty();
    }

    /**
     * Evaluates fieldValue internally using pattern. It puts extracted fields if the pattern contains named groups.
     *
     * @param map event as map of string to object
     * @param fieldValue value of the field for matching
     * @return EvaluationResult.MATCH if pattern matches otherwise EvaluationResult.NO_MATCH
     *
     */
    @Override
    protected EvaluationResult matchInternally(Map<String, Object> map, Object fieldValue) {
        var fieldStringValue = fieldValue.toString();
        Matcher matcher = pattern.matcher(fieldStringValue);
        if (!matcher.matches()) {
            return EvaluationResult.NO_MATCH;
        }

        int index = 1;
        for (String groupName : variableNames) {
            map.put(groupName, matcher.group(index++));
        }

        return EvaluationResult.MATCH;
    }

    /**
     * Creates regex matcher builder instance.
     *
     * @return regex matcher builder
     */
    public static RegexMatcher.Builder<RegexMatcher> builder() {

        return new RegexMatcher.Builder<>() {
            @Override
            public RegexMatcher build() {
                if (pattern == null || variableNames == null) {
                    throw new IllegalArgumentException(EMPTY_PATTERN);
                }
                return new RegexMatcher(this);
            }
        };
    }

    /**
     * A builder for regular expression matchers
     *
     * <p>This abstract class is derived from BasicMatcher.Builder class
     *
     *
     * @author  Marian Novotny
     */
    public static abstract class Builder<T extends RegexMatcher>
            extends BasicMatcher.Builder<T> {
        protected Pattern pattern;
        protected List<String> variableNames;

        /**
         * Compiles pattern from string in builder. Renames named groups since regular expression supports
         * characters: `_`, `:`, which are forbidden in Java patterns
         *
         * @param patternStr regular expression specification
         * @return this builder
         */
        public RegexMatcher.Builder<T> pattern(String patternStr) {
            //NOTE: java regex does not support : _ in variable names but we want it
            variableNames = new ArrayList<>();
            StringBuilder sb = new StringBuilder();
            Matcher matcher = VARIABLE_PATTERN.matcher(patternStr);
            int lastIndex = 0;

            while (matcher.find()) {
                sb.append(patternStr, lastIndex, matcher.start() + VAR_PREFIX_SIZE);
                lastIndex = matcher.end() - 1;
                String name = matcher.group(1);

                if (name == null || name.isEmpty()
                        || variableNames.contains(name)) {
                    throw new IllegalArgumentException(
                            String.format("Wrong names of variables in %s", patternStr));
                }

                //NOTE: we rename variables since java does not support '_', ':'
                sb.append(VARIABLE_NAME).append(variableNames.size());
                variableNames.add(name);
            }

            String finalPatternStr = sb.length() == 0 ? patternStr
                    : sb.append(patternStr, lastIndex, patternStr.length()).toString();
            pattern = Pattern.compile(finalPatternStr, Pattern.DOTALL);
            return this;
        }
    }
}
