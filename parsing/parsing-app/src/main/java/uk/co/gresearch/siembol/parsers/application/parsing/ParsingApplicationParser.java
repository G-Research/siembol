package uk.co.gresearch.siembol.parsers.application.parsing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.gresearch.siembol.common.error.ErrorMessage;
import uk.co.gresearch.siembol.common.error.ErrorType;
import uk.co.gresearch.siembol.common.utils.TimeProvider;
import uk.co.gresearch.siembol.common.constants.SiembolMessageFields;
import uk.co.gresearch.siembol.parsers.common.ParserResult;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
/**
 * An object for parsing application parser
 *
 * <p>This abstract class is using template pattern for handling common functionality of all parsing application parsers.
 *
 * @author  Marian Novotny
 */
public abstract class ParsingApplicationParser implements Serializable {
    public enum Flags implements Serializable {
        PARSE_METADATA,
        ADD_GUID_TO_MESSAGES;
        private static final long serialVersionUID = 1L;
    }

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = LoggerFactory
            .getLogger(MethodHandles.lookup().lookupClass());
    private static final ObjectWriter JSON_WRITER = new ObjectMapper()
            .writerFor(new TypeReference<Map<String, Object>>() { });
    private static final ObjectReader JSON_READER = new ObjectMapper()
            .readerFor(new TypeReference<Map<String, Object>>() { });
    private static final String ERROR_MESSAGE = "Exception during parsing, parsing_app: {} message: {}, " +
            "metadata: {}, exception: {}";
    private static final String MISSING_ARGUMENTS_MSG = "Missing arguments required for Parsing application parser";
    private static final String WRONG_MAX_NUM_FIELDS_MSG =
            "Property maxNumFields should be greater than number of siembol fields";
    private static final String UNKNOWN_SOURCE = "unknown";

    private final EnumSet<Flags> flags;
    private final String name;
    private final String metadataFormatMsg;
    private final String errorTopic;
    private final String originalStringTopic;
    private final String sourceType;
    private final String processingTimeField;
    private final TimeProvider timeProvider;
    private final HashSet<String> siembolFields;
    private final int maxFieldSize;
    private final int maxNumFields;

    protected ParsingApplicationParser(Builder<?> builder) {
        this.name = builder.name;
        this.metadataFormatMsg = builder.metadataFormatMsg;
        this.errorTopic = builder.errorTopic;
        this.sourceType = builder.name;
        this.processingTimeField = builder.processingTimeField;
        this.flags = builder.flags;
        this.timeProvider = builder.timeProvider;
        this.siembolFields = builder.siembolFields;
        if (name == null
                || builder.maxNumFields == null
                || builder.maxFieldSize == null
                || siembolFields == null
                || errorTopic == null
                || processingTimeField == null
                || timeProvider == null) {
            throw new IllegalArgumentException(MISSING_ARGUMENTS_MSG);
        }
        this.maxFieldSize = builder.maxFieldSize;
        this.maxNumFields = builder.maxNumFields;
        this.originalStringTopic = builder.originalStringTopic;
        if (siembolFields.size() > maxNumFields) {
            throw new IllegalArgumentException(WRONG_MAX_NUM_FIELDS_MSG);
        }
    }

    private String getErrorMessage(Throwable throwable, String sensorType, byte[] message) {
        ErrorMessage msg = new ErrorMessage();
        msg.setErrorType(ErrorType.PARSER_ERROR);
        msg.setMessage(throwable.getMessage());
        msg.setStackTrace(ExceptionUtils.getStackTrace(throwable));
        msg.setFailedSensorType(sensorType);
        msg.setRawMessage(message);
        return msg.toString();
    }

    /**
     * Parses the message using internal parser(s).
     * Template method to be implemented by descendant classes.
     *
     * @param source source of the message than can be used for selecting a parser for parsing
     * @param metadata metadata of the message as a json string
     * @param message byte array of message for parsing
     *
     * @return the list of parsing application results
     * @see ParsingApplicationResult
     */
    protected abstract ParserResult parseInternally(String source, String metadata, byte[] message);

    /**
     * Parses the message using internal parser(s)
     *
     * @param metadata metadata of the message as a json string
     * @param message byte array of message for parsing
     *
     * @return the list of parsing application results
     * @see ParsingApplicationResult
     */
    public ArrayList<ParsingApplicationResult> parse(String metadata, byte[] message) {
        return parse(UNKNOWN_SOURCE, metadata, message);
    }

    /**
     * Parses the message using internal parser(s) with additional knowledge of a source of the message
     *
     * @param source source of the message than can be used for selecting a parser for parsing
     * @param metadata metadata of the message as a json string
     * @param message byte array of the message for parsing
     *
     * @return the list of parsing application results
     * @see ParsingApplicationResult
     */
    public ArrayList<ParsingApplicationResult> parse(String source, String metadata, byte[] message) {
        ArrayList<ParsingApplicationResult> ret = new ArrayList<>();
        try {
            Map<String, Object> metadataObject = flags.contains(Flags.PARSE_METADATA)
                    ? JSON_READER.readValue(metadata.trim())
                    : null;

            long timestamp = timeProvider.getCurrentTimeInMs();
            var parserResult = parseInternally(source, metadata, message);

            var currentResult = new ParsingApplicationResult(parserResult.getSourceType());
            if (parserResult.getException() != null) {
                currentResult.setResultFlags(EnumSet.of(ParsingApplicationResult.ResultFlag.ERROR));
                currentResult.setTopic(errorTopic);
                currentResult.setMessage(getErrorMessage(
                        parserResult.getException(), parserResult.getSourceType(), message));
                ret.add(currentResult);
                return ret;
            }

            currentResult.setTopic(parserResult.getTopic());

            var parsed = parserResult.getParsedMessages();
            parsed.removeIf(Map::isEmpty);
            if (parsed.isEmpty()) {
                currentResult.setResultFlags(EnumSet.of(ParsingApplicationResult.ResultFlag.FILTERED));
                ret.add(currentResult);
                return ret;
            }

           var resultFlags = EnumSet.of(ParsingApplicationResult.ResultFlag.PARSED);
            parsed.forEach(x -> {
                x.put(processingTimeField, timestamp);
                x.put(SiembolMessageFields.SENSOR_TYPE.toString(), parserResult.getSourceType());
                if (flags.contains(Flags.ADD_GUID_TO_MESSAGES)) {
                    x.put(SiembolMessageFields.GUID.toString(), UUID.randomUUID().toString());
                }
                if (metadataObject != null) {
                    metadataObject.keySet()
                            .forEach(y -> x.put(String.format(metadataFormatMsg, y), metadataObject.get(y)));
                }
                resultFlags.addAll(removeFields(x));
                resultFlags.addAll(truncateFields(x));
            });

            currentResult.setResultFlags(resultFlags);
            ArrayList<String> serialised = parsed.stream()
                    .map(x -> {
                        try {
                            return JSON_WRITER.writeValueAsString(x);
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .collect(Collectors.toCollection(ArrayList::new));
            currentResult.setMessages(serialised);
            ret.add(currentResult);

            if (resultFlags.contains(ParsingApplicationResult.ResultFlag.TRUNCATED_ORIGINAL_STRING)
                && this.originalStringTopic != null) {
                var originalMessage = new ParsingApplicationResult(parserResult.getSourceType());
                originalMessage.setMessage(new String(message, StandardCharsets.UTF_8));
                originalMessage.setTopic(originalStringTopic);
                originalMessage.setResultFlags(EnumSet.of(ParsingApplicationResult.ResultFlag.ORIGINAL_MESSAGE));
                ret.add(originalMessage);
            }

            return ret;
        } catch (Exception e) {
            LOG.debug(ERROR_MESSAGE, name, new String(message), metadata, ExceptionUtils.getMessage(e));
            var errorResult = new ParsingApplicationResult(sourceType);
            errorResult.setTopic(errorTopic);
            errorResult.setMessage(getErrorMessage(e, sourceType, message));
            errorResult.setResultFlags(EnumSet.of(ParsingApplicationResult.ResultFlag.ERROR));
            ret.add(errorResult);
            return ret;
        }
    }

    private EnumSet<ParsingApplicationResult.ResultFlag> removeFields(Map<String, Object> parsed) {
        if (parsed.size() <= maxNumFields) {
            return EnumSet.noneOf(ParsingApplicationResult.ResultFlag.class);
        }

        int toRemove = parsed.size() - maxNumFields;
        var queue = new PriorityQueue<>(toRemove, Comparator.comparingInt(String::length));
        for (var fieldName : parsed.keySet()) {
            if (siembolFields.contains(fieldName)
                    || fieldName == null) {
                continue;
            }

            if (queue.size() < toRemove) {
                queue.add(fieldName);
            } else if (fieldName.length() > queue.peek().length()) {
                queue.poll();
                queue.add(fieldName);
            }
        }
        queue.forEach(parsed::remove);

        return EnumSet.of(ParsingApplicationResult.ResultFlag.REMOVED_FIELDS);
    }

    private EnumSet<ParsingApplicationResult.ResultFlag> truncateFields(Map<String, Object> parsed) {
        var ret = EnumSet.noneOf(ParsingApplicationResult.ResultFlag.class);
        for (var pair : parsed.entrySet()) {
            if (!(pair.getValue() instanceof String)) {
                continue;
            }
            String currentValue = (String) pair.getValue();
            if (currentValue.length() <= maxFieldSize) {
                continue;
            }

            pair.setValue(currentValue.substring(0, maxFieldSize));
            ret.add(ParsingApplicationResult.ResultFlag.TRUNCATED_FIELDS);
            if (SiembolMessageFields.ORIGINAL.getName().equals(pair.getKey())) {
                ret.add(ParsingApplicationResult.ResultFlag.TRUNCATED_ORIGINAL_STRING);
            }
        }
        return ret;
    }

    /**
     * Gets name of the parsing application parser
     *
     * @return the name of the parsing application parser
     */
    public String getName() {
        return name;
    }

    /**
     * An abstract builder for parsing application parsers
     *
     * <p>This abstract class is using Builder pattern.
     *
     * @author  Marian Novotny
     */
    public static abstract class Builder<T extends ParsingApplicationParser> implements Serializable {
        private static final long serialVersionUID = 1L;
        private static final String METADATA_FORMAT_MSG = "%s";
        protected EnumSet<Flags> flags = EnumSet.noneOf(Flags.class);
        protected String name;
        protected String metadataFormatMsg = METADATA_FORMAT_MSG;
        protected String errorTopic;
        protected String originalStringTopic;
        protected String processingTimeField = SiembolMessageFields.PARSING_TIME.toString();
        protected TimeProvider timeProvider = new TimeProvider();
        protected Integer maxNumFields = 400;
        protected Integer maxFieldSize = 20000;
        protected HashSet<String> siembolFields = new HashSet<>(SiembolMessageFields.getMessageFieldsSet());

        /**
         * Sets the name of the parsing application parser
         *
         * @param name the name of the parsing application parser
         * @return this builder
         */
        public Builder<T> name(String name) {
            this.name = name;
            return this;
        }

        /**
         * Sets whether the application will parse the metadata as a json string
         *
         * @param parseMetadata the flag whether the application will parse the metadata as a json string
         * @return this builder
         */
        public Builder<T> parseMetadata(boolean parseMetadata) {
            if (parseMetadata) {
                flags.add(Flags.PARSE_METADATA);
            }
            return this;
        }

        /**
         * Sets whether the application will add guid field to the message after parsing
         *
         * @param addGuidToMessages a boolean flag which will determine whether the application will add
         *                          a guid field to the message after parsing
         * @return this builder
         */
        public Builder<T> addGuidToMessages(boolean addGuidToMessages) {
            if (addGuidToMessages) {
                flags.add(Flags.ADD_GUID_TO_MESSAGES);
            }
            return this;
        }

        /**
         * Sets the metadata prefix
         *
         * @param metadataPrefix the prefix that will be added to metadata fields after parsing the metadata json message
         * @return this builder
         */
        public Builder<T> metadataPrefix(String metadataPrefix) {
            if (metadataPrefix != null) {
                this.metadataFormatMsg = metadataPrefix + METADATA_FORMAT_MSG;
            }
            return this;
        }

        /**
         * Sets the error topic
         *
         * @param errorTopic the topic for sending error messages
         * @return this builder
         */
        public Builder<T> errorTopic(String errorTopic) {
            this.errorTopic = errorTopic;
            return this;
        }

        /**
         * Sets the original string topic
         *
         * @param originalStringTopic the topic for sending original strings in case of truncating the message
         * @return this builder
         */
        public Builder<T> originalStringTopic(String originalStringTopic) {
            this.originalStringTopic = originalStringTopic;
            return this;
        }

        /**
         * Sets the processing time field with the current time that will be added to the message after parsing
         *
         * @param processingTimeField field name that will be added after parsing a message
         * @return this builder
         */
        public Builder<T> processingTimeField(String processingTimeField) {
            this.processingTimeField = processingTimeField;
            return this;
        }

        /**
         * Sets the maximum number of fields
         *
         * @param maxNumFields maximum number of fields after parsing
         * @return this builder
         */
        public Builder<T> maxNumFields(int maxNumFields) {
            this.maxNumFields = maxNumFields;
            return this;
        }

        /**
         * Sets the maximum field size after parsing. Larger fields will be truncated to this size.
         *
         * @param maxFieldSize the maximum field size after parsing
         * @return this builder
         */
        public Builder<T> maxFieldSize(int maxFieldSize) {
            this.maxFieldSize = maxFieldSize;
            return this;
        }

        /**
         * Sets the time provider
         *
         * @param timeProvider time provider which can be used in testing
         * @return this builder
         */
        public Builder<T> timeProvider(TimeProvider timeProvider) {
            this.timeProvider = timeProvider;
            return this;
        }

        /**
         * Sets siembol fields that cannot be removed during evaluating maximum number of fields
         *
         * @param siembolFields set of fields that will be not removed during evaluating maximum number of fields
         * @return this builder
         */
        public Builder<T> siembolFields(HashSet<String> siembolFields) {
            this.siembolFields = siembolFields;
            return this;
        }
        public abstract T build();
    }
}
