package uk.co.gresearch.siembol.parsers.extractors;

import java.util.AbstractMap.SimpleEntry;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class PatternExtractor extends ParserExtractor {
    public enum PatternExtractorFlags {
        SHOULD_MATCH,
        DOTALL
    }

    private static final Pattern VARIABLE_PATTERN =
            Pattern.compile("\\(\\?<([a-zA-Z][a-zA-Z0-9:_]*)>");
    private static final String VARIABLE_NAME = "var";
    private static final int VAR_PREFIX_SIZE = "(\\<".length();

    private final List<SimpleEntry<Pattern, List<String>>> patterns;
    private final EnumSet<PatternExtractorFlags> patternExtractorFlags;

    private PatternExtractor(Builder<?> builder) {
        super(builder);
        this.patterns = builder.compiledPatterns;
        this.patternExtractorFlags = builder.patternExtractorFlags;
    }

    @Override
    public Map<String, Object> extractInternally(String str) {

        HashMap<String, Object> ret = new HashMap<>();

        for (SimpleEntry<Pattern, List<String>> pattern :  patterns) {
            Matcher matcher = pattern.getKey().matcher(str);
            if (!matcher.matches()) {
                continue;
            }

            int index = 1;
            for (String groupName : pattern.getValue()) {
                ret.put(groupName, matcher.group(index++));
            }
        }

        if (ret.isEmpty()
                && patternExtractorFlags.contains(PatternExtractorFlags.SHOULD_MATCH)
                && shouldThrowExceptionOnError()) {
            throw new IllegalStateException("Pattern should match");
        }
        return ret;
    }

    private static SimpleEntry<Pattern, List<String>> transformPattern(String strPattern, int flags) {
        //NOTE: java regex does not support : _ in variable names but we want it
        List<String> names = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        Matcher matcher = VARIABLE_PATTERN.matcher(strPattern);
        int lastIndex = 0;

        while(matcher.find()) {
            sb.append(strPattern, lastIndex, matcher.start() + VAR_PREFIX_SIZE);
            lastIndex = matcher.end() - 1;
            String name = matcher.group(1);

            if (name == null || name.isEmpty()
                    || names.contains(name)) {
                throw new IllegalArgumentException(
                        String.format("Wrong names of variables in %s", strPattern));
            }

            //NOTE: we rename variables since java does not support '_', ':'
            sb.append(VARIABLE_NAME).append(names.size());
            names.add(name);
        }

        if (names.isEmpty()) {
            throw new IllegalArgumentException(
                    String.format("No variables found in pattern %s", strPattern));
        }

        sb.append(strPattern, lastIndex, strPattern.length());
        Pattern pattern = Pattern.compile(sb.toString(), flags);

        return new SimpleEntry<>(pattern, names);
    }

    public static Builder<PatternExtractor> builder() {
        return new Builder<PatternExtractor>()
        {
            @Override
            public PatternExtractor build()
            {
                if (this.patterns == null || patterns.isEmpty()) {
                    throw new IllegalArgumentException("Empty patterns");
                }

                final int regexFlags = patternExtractorFlags.contains(PatternExtractorFlags.DOTALL)
                        ? Pattern.DOTALL
                        : 0;

                compiledPatterns = patterns.stream()
                        .map(x -> transformPattern(x, regexFlags))
                        .collect(Collectors.toList());

                return new PatternExtractor(this);
            }
        };
    }

    public static abstract class Builder<T extends PatternExtractor>
            extends ParserExtractor.Builder<T> {

        protected List<String> patterns;
        protected List<SimpleEntry<Pattern, List<String>>> compiledPatterns;
        protected EnumSet<PatternExtractorFlags> patternExtractorFlags =
                EnumSet.noneOf(PatternExtractorFlags.class);

        public Builder<T> patterns(
                List<String> patterns) {
            this.patterns = patterns;
            return this;
        }

        public Builder<T> patternExtractorFlags(
                EnumSet<PatternExtractorFlags> flags) {
            this.patternExtractorFlags = flags;
            return this;
        }
    }
}
