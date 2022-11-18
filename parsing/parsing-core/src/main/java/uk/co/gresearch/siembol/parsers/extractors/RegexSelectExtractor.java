package uk.co.gresearch.siembol.parsers.extractors;

import org.apache.commons.lang3.tuple.Pair;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


public class RegexSelectExtractor extends ParserExtractor {
    private static final String MISSING_ARGUMENTS = "output field and patterns are required for regex select extractor";
    private final List<Pair<String, Pattern>> patterns;
    private final String outputField;
    private final String defaultValue;

    private RegexSelectExtractor(Builder<?> builder) {
        super(builder);
        this.patterns = builder.patterns;
        this.outputField = builder.outputField;
        this.defaultValue = builder.defaultValue;
    }

    @Override
    protected Map<String, Object> extractInternally(String message) {
        Map<String, Object> map = new HashMap<>();

        for (Pair<String, Pattern> pattern : patterns) {
            Matcher m = pattern.getRight().matcher(message);
            if (m.find()) {
                map.put(outputField, pattern.getLeft());
                return map;
            }
        }

        if (defaultValue != null) {
            map.put(outputField, defaultValue);
        }

        return map;
    }

    public static Builder<RegexSelectExtractor> builder() {
        return new Builder<>() {
            @Override
            public RegexSelectExtractor build() {
                if (this.outputField == null
                        || patterns == null) {
                    throw new IllegalArgumentException(MISSING_ARGUMENTS);
                }
                return new RegexSelectExtractor(this);
            }
        };
    }

    public static abstract class Builder<T extends RegexSelectExtractor>
            extends ParserExtractor.Builder<T> {

        protected List<Pair<String, Pattern>> patterns;
        protected String outputField;
        protected String defaultValue;

        public Builder<T> patterns(List<Pair<String, String>> patterns) {
            this.patterns = patterns.stream()
                    .map(x -> Pair.of(x.getLeft(), Pattern.compile(x.getRight())))
                    .collect(Collectors.toList());
            return this;
        }

        public Builder<T> outputField(String outputField) {
            this.outputField = outputField;
            return this;
        }

        public Builder<T> defaultValue(String defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }
    }
}
