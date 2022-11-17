package uk.co.gresearch.siembol.parsers.extractors;

import java.util.*;
import java.util.function.Function;

import static uk.co.gresearch.siembol.parsers.extractors.ParserExtractor.ParserExtractorFlags.*;
/**
 * An object for extracting fields from the message
 *
 * <p>This abstract class is using template pattern for handling common functionality of all extractors.
 * The extractor is registered on a field and extracting the key value pairs during processing the field value.
 *
 * @author  Marian Novotny
 * @see JsonExtractor
 * @see JsonPathExtractor
 * @see KeyValueExtractor
 * @see CSVExtractor
 * @see PatternExtractor
 */
public abstract class ParserExtractor  {
    private static final String EMPTY_MSG_FOR_EXTRACTION_MSG = "Empty message for extraction";
    private static final String DUPLICATE_FORMAT_MSG = "duplicate_%s_%d";
    private static final String EMPTY_STRING = "";

    public enum ParserExtractorFlags {
        SHOULD_REMOVE_FIELD,
        SHOULD_OVERWRITE_FIELDS,
        THROWN_EXCEPTION_ON_ERROR,
        REMOVE_QUOTES,
        SKIP_EMPTY_VALUES,
    }

    /**
     * Creates parser extractor using builder pattern
     *
     * @param builder parser extractor builder
     */
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

    /**
     * Provides the name of the extractor
     *
     * @return extractor name
     */
    public String getName() {
        return name;
    }

    /**
     * Provides the field of the extractor
     *
     * @return field name
     */
    public String getField() {
        return field;
    }

    /**
     * Provides information whether the extractor should remove the field from the message after extraction
     *
     * @return true if the extractor should remove the field from the message after extraction, otherwise false
     */
    public boolean shouldRemoveField() {
        return parserExtractorFlags
                .contains(SHOULD_REMOVE_FIELD);
    }

    /**
     * Provides information whether the extractor should overwrite the fields of the message after extraction
     *
     * @return true if the extractor should overwrite the fields of the message after extraction, otherwise false
     */
    public boolean shouldOverwriteFields() {
        return parserExtractorFlags
                .contains(SHOULD_OVERWRITE_FIELDS);
    }

    /**
     * Provides information whether the extractor should throw an exception on an error
     *
     * @return true if the extractor should throw an exception on an error, otherwise false
     */
    public boolean shouldThrowExceptionOnError() {
        return parserExtractorFlags
                .contains(THROWN_EXCEPTION_ON_ERROR);
    }

    /**
     * Provides the information whether the extractor should throw an exception on an error
     *
     * @return true if the extractor should throw an exception on an error, otherwise false
     */
    public boolean shouldSkipEmptyValues() {
        return parserExtractorFlags
                .contains(SKIP_EMPTY_VALUES);
    }

    /**
     * Applies a pre-processing function on an input string.
     * Extracts key value pairs from the input string.
     * Applies the post-processing functions on extracted pairs.
     *
     * @param str an input string
     * @return extracted key value pairs as a map of String to Object
     */
    public Map<String, Object> extract(String str) {
        String message = preProcessing != null
                ? preProcessing.apply(str)
                : str;

        if (message == null) {
            if (shouldThrowExceptionOnError()) {
                throw new IllegalStateException(EMPTY_MSG_FOR_EXTRACTION_MSG);
            }
            return new HashMap<>();
        }

        Map<String, Object> extracted = extractInternally(message);

        for (Function<Map<String, Object>, Map<String, Object>> fun : postProcessing) {
            extracted = fun.apply(extracted);
        }

        return extracted;
    }

    /**
     * Template abstract method to provide extracting key value pairs from an input string
     *
     * @param str Input string
     * @return extracted key value pairs as a map of String to Object
     */
    protected abstract Map<String, Object> extractInternally(String str);

    protected Object getValue(String value) {
        if (parserExtractorFlags.contains(ParserExtractorFlags.REMOVE_QUOTES)
                && value.length() >= 2
                && (value.charAt(0) == '\'' || value.charAt(0) == '"')
                && value.charAt(0) == value.charAt(value.length() - 1)) {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }

    /**
     * An abstract builder for a parser extractor
     *
     * <p>This abstract class is using Builder pattern.
     *
     *
     * @author  Marian Novotny
     */
    public static abstract class Builder<T extends ParserExtractor> {
        private String name;
        private String field;
        private EnumSet<ParserExtractorFlags> extractorFlags =
                EnumSet.of(ParserExtractorFlags.REMOVE_QUOTES);
        private Function<String, String> preProcessing;
        private List<Function<Map<String, Object>, Map<String, Object>>> postProcessing =
                new ArrayList<>();

        /**
         * Sets the name of the extractor
         *
         * @param name the name of the extractor
         * @return this builder
         */
        public Builder<T> name(String name) {
            this.name = name;
            return this;
        }

        /**
         * Sets a field name of the extractor
         *
         * @param field a field name of the extractor
         * @return this builder
         */
        public Builder<T> field(String field) {
            this.field = field;
            return this;
        }

        /**
         * Sets extractor flags of the extractor
         *
         * @param flags Flags of the extractor
         * @return this builder
         * @see ParserExtractorFlags
         */
        public Builder<T> extractorFlags(EnumSet<ParserExtractorFlags> flags) {
            this.extractorFlags = flags;
            return this;
        }

        /**
         * Sets a pre-processing function of the extractor
         *
         * @param preProcessing A pre-processing function of the extractor
         * @return this builder
         */
        public Builder<T> preProcessing(Function<String, String> preProcessing) {
            this.preProcessing = preProcessing;
            return this;
        }

        /**
         * Sets a list of post-processing functions of the extractor
         *
         * @param postProcessing A list of post-processing functions of the extractor
         * @return this builder
         */
        public Builder<T> postProcessing(List<Function<Map<String, Object>,
                        Map<String, Object>>> postProcessing) {
            this.postProcessing = postProcessing;
            return this;
        }

        public abstract T build();
    }

    /**
     * Extracts pairs from a message object by executing a list of extractors
     *
     * @param extractors List of extractors to be executed in a chain
     * @param messageObject an initial message object that will be extended by calling a chain of extractors
     * @return the message object after executing all extractors
     */
    public static Map<String, Object> extract(
            List<ParserExtractor> extractors,
            Map<String, Object> messageObject) {
        DuplicatesFieldMap duplicatesMap = new DuplicatesFieldMap();

        for (ParserExtractor extractor : extractors) {
            String field = extractor.getField();
            if (!(messageObject.get(field) instanceof String)) {
                continue;
            }

            if (!extractor.shouldOverwriteFields()) {
                duplicatesMap.clear();
            }

            String message = (String) messageObject.get(field);
            Map<String, Object> parsed = extractor.extract(message);
            for (String key : parsed.keySet()) {
                if (extractor.shouldSkipEmptyValues() && EMPTY_STRING.equals(parsed.get(key))) {
                    continue;
                }

                if (messageObject.putIfAbsent(key, parsed.get(key)) != null) {
                    String currentName = key;
                    if(!extractor.shouldOverwriteFields()) {
                        int duplicateIndex = duplicatesMap.getIndex(key);
                        currentName = String.format(DUPLICATE_FORMAT_MSG, key, duplicateIndex);
                    }
                    messageObject.put(currentName, parsed.get(key));
                }
            }

            if (extractor.shouldRemoveField()) {
                messageObject.remove(field);
            }

        }
        return messageObject;
    }
}
