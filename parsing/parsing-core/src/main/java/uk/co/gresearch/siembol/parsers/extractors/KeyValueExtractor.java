package uk.co.gresearch.siembol.parsers.extractors;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static uk.co.gresearch.siembol.parsers.extractors.KeyValueExtractor.KeyValueExtractorFlags.NEXT_KEY_STRATEGY;

public class KeyValueExtractor extends ParserExtractor {

    private static final String EXTRACTOR_ERROR_PREFIX = "kv_error";
    private final static String DUPLICATE_FORMAT_MSG = "duplicate_%s_%d";

    public enum KeyValueExtractorFlags {
        QUOTA_VALUE_HANDLING,
        RENAME_DUPLICATE_KEYS,
        ESCAPING_HANDLING,
        NEXT_KEY_STRATEGY
    }

    private final KeyValueIndices.IndexOf indexOf;
    private final char keyValueDelimiter;
    private final EnumSet<KeyValueExtractorFlags> flags;
    private final String errorKeyName;

    private KeyValueExtractor(Builder<?> builder) {
        super(builder);
        this.indexOf = builder.indexOfEnd;
        this.keyValueDelimiter = builder.keyValueDelimiter;
        this.flags = builder.keyValueFlags;
        this.errorKeyName = String.format("%s_%s", EXTRACTOR_ERROR_PREFIX, getName());
    }

    @Override
    protected Map<String, Object> extractInternally(String message){
        Map<String, Object> extracted = new HashMap<>();
        int offset = 0;
        DuplicatesFieldMap duplicatesMap = flags.contains(KeyValueExtractorFlags.RENAME_DUPLICATE_KEYS)
                ? new DuplicatesFieldMap() : null;

        while (offset < message.length()) {
            KeyValueIndices indices = indexOf.apply(message, offset);
            if (!indices.isValid()) {
                if (shouldThrowExceptionOnError()) {
                    throw new IllegalStateException("Empty or missing key");
                }

                extracted.put(errorKeyName, message.substring(offset));
                return extracted;
            }

            String key = message.substring(offset, indices.getKeyIndex());
            String value = message.substring(indices.getKeyIndex() + 1, indices.getValueIndex());

            if (extracted.containsKey(key)
                    && flags.contains(KeyValueExtractorFlags.RENAME_DUPLICATE_KEYS))
            {
                int index = duplicatesMap.getIndex(key);
                key = String.format(DUPLICATE_FORMAT_MSG, key, index);
            }

            extracted.put(key, getValue(value));
            offset = indices.getValueIndex() + 1;
        }

        return extracted;
    }

    public static Builder<KeyValueExtractor> builder() {
        return new Builder<KeyValueExtractor>() {
            private KeyValueIndices.IndexOf getDefaultIndexOfEnd() {
                return new KeyValueIndices.IndexOf() {
                    @Override
                    public KeyValueIndices apply(String str, int from) {
                        boolean quoteHandling = keyValueFlags.contains(
                                KeyValueExtractorFlags.QUOTA_VALUE_HANDLING);

                        Optional<Character> escaped = keyValueFlags.contains(
                                KeyValueExtractorFlags.ESCAPING_HANDLING)
                                ? Optional.of(escapedChar)
                                : Optional.empty();

                        int keyIndex = ParserExtractorLibrary.indexOfQuotedEscaped(str,
                                keyValueDelimiter, from, escaped, quoteHandling);
                        if (keyIndex == -1 || keyIndex == from) {
                            return KeyValueIndices.invalid();
                        }

                        int valueIndex = -1;
                        if (keyValueFlags.contains(NEXT_KEY_STRATEGY)) {
                            int nextKeyIndex = ParserExtractorLibrary.indexOfQuotedEscaped(str,
                                    keyValueDelimiter, keyIndex + 1, escaped, quoteHandling);

                            if (nextKeyIndex == -1) {
                                return new KeyValueIndices(keyIndex, -1, str.length());
                            }
                            valueIndex = str.lastIndexOf(wordDelimiter, nextKeyIndex);
                        } else {
                            valueIndex = ParserExtractorLibrary.indexOfQuotedEscaped(str,
                                    wordDelimiter, keyIndex + 1, escaped, quoteHandling);
                        }
                        return new KeyValueIndices(keyIndex, valueIndex, str.length());
                    }
                };
            }

            @Override
            public KeyValueExtractor build()
            {
                if (indexOfEnd == null) {
                    indexOfEnd = getDefaultIndexOfEnd();
                }
                return new KeyValueExtractor(this);
            }
        };
    }

    public static abstract class Builder<T extends KeyValueExtractor>
            extends ParserExtractor.Builder<T> {
        protected char keyValueDelimiter = '=';
        protected char wordDelimiter = ' ';
        protected char escapedChar = '\\';
        protected KeyValueIndices.IndexOf indexOfEnd;
        protected EnumSet<KeyValueExtractorFlags> keyValueFlags = EnumSet
                .of(KeyValueExtractorFlags.QUOTA_VALUE_HANDLING,
                        KeyValueExtractorFlags.RENAME_DUPLICATE_KEYS);

        public Builder<T> keyValueDelimiter(char keyValueDelimiter) {
            this.keyValueDelimiter = keyValueDelimiter;
            return this;
        }

        public Builder<T> wordDelimiter(char wordDelimiter) {
            this.wordDelimiter = wordDelimiter;
            return this;
        }

        public Builder<T> escapedChar(char escapedChar) {
            this.escapedChar = escapedChar;
            return this;
        }

        public Builder<T> indexOfEnd(KeyValueIndices.IndexOf indexOf) {
            this.indexOfEnd = indexOf;
            return this;
        }

        public Builder<T> keyValueExtractorFlags(
                EnumSet<KeyValueExtractorFlags> flags) {
            this.keyValueFlags = flags;
            return this;
        }
    }
}
