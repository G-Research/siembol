package uk.co.gresearch.nortem.parsers.extractors;

import java.util.*;
import java.util.function.Function;

import static uk.co.gresearch.nortem.parsers.extractors.ParserExtractor.ParserExtractorFlags.*;

public abstract class ParserExtractor  {
    public enum ParserExtractorFlags {
        SHOULD_REMOVE_FIELD,
        SHOULD_OVERWRITE_FIELDS,
        THROWN_EXCEPTION_ON_ERROR,
        REMOVE_QUOTES,
        SKIP_EMPTY_VALUES,
    }

    protected ParserExtractor(Builder<?> builder) {
        this.name = builder.name;
        this.field = builder.field;
        this.parserExtractorFlags = builder.extractorFlags;
        this.preProcessing = builder.preProcessing;
        this.postProcessing = builder.postProcessing;
    }

    private final String name;
    private final String field;
    private final EnumSet<ParserExtractorFlags> parserExtractorFlags;
    private final Function<String, String> preProcessing;
    private final List<Function<Map<String, Object>, Map<String, Object>>> postProcessing;


    protected abstract Map<String, Object> extractInternally(String message);

    public String getName() {
        return name;
    }

    public String getField() {
        return field;
    }

    public boolean shouldRemoveField() {
        return parserExtractorFlags
                .contains(SHOULD_REMOVE_FIELD);
    }
    
    public boolean shouldOverwiteFields() {
        return parserExtractorFlags
                .contains(SHOULD_OVERWRITE_FIELDS);
    }

    public boolean shouldThrowExceptionOnError() {
        return parserExtractorFlags
                .contains(THROWN_EXCEPTION_ON_ERROR);
    }

    public boolean shouldSkipEmptyValues() {
        return parserExtractorFlags
                .contains(SKIP_EMPTY_VALUES);
    }

    public Map<String, Object> extract(String str) {
        String message = preProcessing != null
                ? preProcessing.apply(str)
                : str;

        if (message == null) {
            if (shouldThrowExceptionOnError()) {
                new IllegalStateException("Empty message for extraction");
            }
            return new HashMap<>();
        }

        Map<String, Object> extracted = extractInternally(message);

        for (Function<Map<String, Object>, Map<String, Object>> fun : postProcessing) {
            extracted = fun.apply(extracted);
        }

        return extracted;
    }

    protected Object getValue(String value) {
        if (parserExtractorFlags.contains(ParserExtractorFlags.REMOVE_QUOTES)
                && value.length() >= 2
                && (value.charAt(0) == '\'' || value.charAt(0) == '"')
                && value.charAt(0) == value.charAt(value.length() - 1)) {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }

    public static abstract class Builder<T extends ParserExtractor> {
        private String name;
        private String field;
        private EnumSet<ParserExtractorFlags> extractorFlags =
                EnumSet.of(ParserExtractorFlags.REMOVE_QUOTES);
        private Function<String, String> preProcessing;
        private List<Function<Map<String, Object>, Map<String, Object>>> postProcessing =
                new ArrayList<>();

        public Builder<T> name(String name) {
            this.name = name;
            return this;
        }

        public Builder<T> field(String field) {
            this.field = field;
            return this;
        }

        public Builder<T> extractorFlags(EnumSet<ParserExtractorFlags> flags) {
            this.extractorFlags = flags;
            return this;
        }

        public Builder<T> preProcessing(Function<String, String> preProcessing) {
            this.preProcessing = preProcessing;
            return this;
        }

        public Builder<T> addPostProcessing(Function<Map<String, Object>,
                Map<String, Object>> postProcessing) {
            this.postProcessing.add(postProcessing);
            return this;
        }

        public Builder<T> postProcessing(List<Function<Map<String, Object>,
                        Map<String, Object>>> postProcessing) {
            this.postProcessing = postProcessing;
            return this;
        }

        public abstract T build();
    }

    public static Map<String, Object> extract(
            List<ParserExtractor> extractors,
            Map<String, Object> messageObject) {
        Map<String, Object> current = messageObject;

        for (ParserExtractor extractor : extractors) {
            String field = extractor.getField();

            if (!(current.get(field) instanceof String)) {
                continue;
            }

            String message = (String)current.get(field);
            Map<String, Object> parsed = extractor.extract(message);
            for (String key : parsed.keySet()) {
                if (current.putIfAbsent(key, parsed.get(key)) != null) {
                    String newName = extractor.shouldOverwiteFields()
                            ? key
                            : String.format("%s_%s", extractor.getName(), key);
                    current.put(newName, parsed.get(key));
                }
            }

            if (extractor.shouldRemoveField()) {
                current.remove(field);
            }
        }
        return current;
    }
}
