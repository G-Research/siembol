package uk.co.gresearch.siembol.alerts.engine;

import uk.co.gresearch.siembol.alerts.common.EvaluationResult;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexMatcher extends BasicMatcher {
    private static final String EMPTY_PATTERN = "Empty pattern";
    private static final Pattern VARIABLE_PATTERN =
            Pattern.compile("\\(\\?<([a-zA-Z][a-zA-Z0-9:_]*)>");
    private static final String VARIABLE_NAME = "var";
    private static final int VAR_PREFIX_SIZE = "(\\<".length();

    private final Pattern pattern;
    private final List<String> variableNames;

    private RegexMatcher(Builder<?> builder) {
        super(builder);
        this.pattern = builder.pattern;
        this.variableNames = builder.variableNames;
    }

    @Override
    public boolean canModifyEvent() {
        return !variableNames.isEmpty();
    }

    @Override
    protected EvaluationResult matchInternally(Map<String, Object> map, String fieldValue) {
        Matcher matcher = pattern.matcher(fieldValue);
        if (!matcher.matches()) {
            return EvaluationResult.NO_MATCH;
        }

        int index = 1;
        for (String groupName : variableNames) {
            map.put(groupName, matcher.group(index++));
        }

        return EvaluationResult.MATCH;
    }

    public static RegexMatcher.Builder<RegexMatcher> builder() {

        return new RegexMatcher.Builder<RegexMatcher>() {
            @Override
            public RegexMatcher build() {
                if (pattern == null || variableNames == null) {
                    throw new IllegalArgumentException(EMPTY_PATTERN);
                }
                return new RegexMatcher(this);
            }
        };
    }

    public static abstract class Builder<T extends RegexMatcher>
            extends BasicMatcher.Builder<T> {
        protected Pattern pattern;
        protected List<String> variableNames;

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
                sb.append(VARIABLE_NAME + variableNames.size());
                variableNames.add(name);
            }

            String finalPatternStr = sb.length() == 0 ? patternStr
                    : sb.append(patternStr, lastIndex, patternStr.length()).toString();
            pattern = Pattern.compile(finalPatternStr, Pattern.DOTALL);
            return this;
        }
    }
}
