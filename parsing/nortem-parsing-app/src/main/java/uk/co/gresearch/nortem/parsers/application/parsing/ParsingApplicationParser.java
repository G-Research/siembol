package uk.co.gresearch.nortem.parsers.application.parsing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.gresearch.nortem.common.error.ErrorMessage;
import uk.co.gresearch.nortem.common.error.ErrorType;
import uk.co.gresearch.nortem.common.utils.TimeProvider;
import uk.co.gresearch.nortem.parsers.common.ParserFields;
import uk.co.gresearch.nortem.parsers.common.ParserResult;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.*;
import java.util.stream.Collectors;

public abstract class ParsingApplicationParser implements Serializable {
    public enum Flags implements Serializable {
        PARSE_METADATA
    }

    private static final Logger LOG = LoggerFactory
            .getLogger(MethodHandles.lookup().lookupClass());
    private static final ObjectWriter JSON_WRITER = new ObjectMapper()
            .writerFor(new TypeReference<Map<String, Object>>() { });
    private static final ObjectReader JSON_READER = new ObjectMapper()
            .readerFor(new TypeReference<Map<String, Object>>() { });
    private static final String ERROR_MESSAGE = "Exception during parsing, parsing_app: {} message: {}, " +
            "metadata: {}, exception: {}";
    private static final String MISSING_ARGUMENTS = "Missing arguments required for Parsing application parser";
    private static final String METADATA_FORMAT_MSG = "%s:%s";

    private final EnumSet<Flags> flags;
    private final String name;
    private final String metadataPrefix;
    private final String errorTopic;
    private final String sourceType;
    private final String processingTimeField;
    private final TimeProvider timeProvider;

    protected ParsingApplicationParser(Builder<?> builder) {
        this.name = builder.name;
        this.metadataPrefix = builder.metadataPrefix;
        this.errorTopic = builder.errorTopic;
        this.sourceType = builder.name;
        this.processingTimeField = builder.processingTimeField;
        this.flags = builder.flags;
        this.timeProvider = builder.timeProvider;
        if (name == null
                || errorTopic == null
                || processingTimeField == null
                || timeProvider == null
                || (flags.contains(Flags.PARSE_METADATA) && metadataPrefix == null)) {
            throw new IllegalArgumentException(MISSING_ARGUMENTS);
        }
    }

    private String getErrorMessage(Throwable throwable, String sensorType) {
        ErrorMessage msg = new ErrorMessage();
        msg.setErrorType(ErrorType.PARSER_ERROR);
        msg.setMessage(throwable.getMessage());
        msg.setStackTrace(ExceptionUtils.getStackTrace(throwable));
        msg.setFailedSensorType(sensorType);
        return msg.toString();
    }

    protected abstract List<ParserResult> parseInternally(String metadata, byte[] message);

    public ArrayList<ParsingApplicationResult> parse(String metadata, byte[] message) {
        ArrayList<ParsingApplicationResult> ret = new ArrayList<>();
        try {
            Map<String, Object> metadataObject = flags.contains(Flags.PARSE_METADATA)
                    ? JSON_READER.readValue(metadata)
                    : null;


            long timestamp = timeProvider.getCurrentTimeInMs();
            for (ParserResult parserResult : parseInternally(metadata, message)) {
                if (parserResult.getException() != null) {
                    ret.add(new ParsingApplicationResult(
                            errorTopic,
                            getErrorMessage(parserResult.getException(), parserResult.getSourceType())));
                    continue;
                }

                List<Map<String, Object>> parsed = parserResult.getParsedMessages();
                parsed.removeIf(x -> x.isEmpty());
                if (parsed.isEmpty()) {
                    continue;
                }

                parsed.forEach(x -> {
                    x.put(processingTimeField, timestamp);
                    x.put(ParserFields.SENSOR_TYPE.toString(), parserResult.getSourceType());
                });

                if (metadataObject != null) {
                    parsed.forEach(x -> metadataObject.keySet().forEach(y -> x.put(
                            String.format(METADATA_FORMAT_MSG, metadataPrefix, y), metadataObject.get(y))));
                }

                ArrayList<String> serialised = parsed.stream()
                        .map(x -> {
                            try {
                                return JSON_WRITER.writeValueAsString(x);
                            } catch (JsonProcessingException e) {
                                throw new RuntimeException(e);
                            }
                        })
                        .collect(Collectors.toCollection(ArrayList::new));
                ret.add(new ParsingApplicationResult(parserResult.getTopic(), serialised));
            }
            return ret;
        } catch (Exception e) {
            LOG.error(ERROR_MESSAGE, name, new String(message), metadata, ExceptionUtils.getMessage(e));
            ret.add(new ParsingApplicationResult(errorTopic, getErrorMessage(e, sourceType)));
            return ret;
        }
    }

    public String getName() {
        return name;
    }

    public static abstract class Builder<T extends ParsingApplicationParser> implements Serializable {
        protected EnumSet<Flags> flags = EnumSet.noneOf(Flags.class);
        protected String name;
        protected String metadataPrefix;
        protected String errorTopic;
        protected String processingTimeField = ParserFields.PARSING_TIME.toString();
        protected TimeProvider timeProvider = new TimeProvider();

        public Builder<T> name(String name) {
            this.name = name;
            return this;
        }

        public Builder<T> parseMetadata(boolean parseMetadata) {
            if (parseMetadata) {
                flags.add(Flags.PARSE_METADATA);
            }
            return this;
        }

        public Builder<T> metadataPrefix(String metadataPrefix) {
            this.metadataPrefix = metadataPrefix;
            return this;
        }

        public Builder<T> errorTopic(String errorTopic) {
            this.errorTopic = errorTopic;
            return this;
        }

        public Builder<T> processingTimeField(String processingTimeField) {
            this.processingTimeField = processingTimeField;
            return this;
        }

        public Builder<T> timeProvider(TimeProvider timeProvider) {
            this.timeProvider = timeProvider;
            return this;
        }

        public abstract T build();
    }
}
