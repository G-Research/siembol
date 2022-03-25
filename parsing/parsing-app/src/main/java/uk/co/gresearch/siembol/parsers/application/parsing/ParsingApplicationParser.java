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
import java.util.*;
import java.util.stream.Collectors;

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
    private static final String MISSING_ARGUMENTS = "Missing arguments required for Parsing application parser";
    private static final String UNKNOWN_SOURCE = "unknown";

    private final EnumSet<Flags> flags;
    private final String name;
    private final String metadataFormatMsg;
    private final String errorTopic;
    private final String sourceType;
    private final String processingTimeField;
    private final TimeProvider timeProvider;

    protected ParsingApplicationParser(Builder<?> builder) {
        this.name = builder.name;
        this.metadataFormatMsg = builder.metadataFormatMsg;
        this.errorTopic = builder.errorTopic;
        this.sourceType = builder.name;
        this.processingTimeField = builder.processingTimeField;
        this.flags = builder.flags;
        this.timeProvider = builder.timeProvider;
        if (name == null
                || errorTopic == null
                || processingTimeField == null
                || timeProvider == null) {
            throw new IllegalArgumentException(MISSING_ARGUMENTS);
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

    protected abstract List<ParserResult> parseInternally(String source, String metadata, byte[] message);

    public ArrayList<ParsingApplicationResult> parse(String metadata, byte[] message) {
        return parse(UNKNOWN_SOURCE, metadata, message);
    }

    public ArrayList<ParsingApplicationResult> parse(String source, String metadata, byte[] message) {
        ArrayList<ParsingApplicationResult> ret = new ArrayList<>();
        try {
            Map<String, Object> metadataObject = flags.contains(Flags.PARSE_METADATA)
                    ? JSON_READER.readValue(metadata.trim())
                    : null;

            long timestamp = timeProvider.getCurrentTimeInMs();
            for (ParserResult parserResult : parseInternally(source, metadata, message)) {
                var currentResult = new ParsingApplicationResult(parserResult.getSourceType());
                if (parserResult.getException() != null) {
                    currentResult.setResultType(ParsingApplicationResult.ResultType.ERROR);
                    currentResult.setTopic(errorTopic);
                    currentResult.setMessage(getErrorMessage(
                            parserResult.getException(), parserResult.getSourceType(), message));
                    ret.add(currentResult);
                    continue;
                }

                currentResult.setTopic(parserResult.getTopic());

                var parsed = parserResult.getParsedMessages();
                parsed.removeIf(Map::isEmpty);
                if (parsed.isEmpty()) {
                    currentResult.setResultType(ParsingApplicationResult.ResultType.FILTERED);
                    ret.add(currentResult);
                    continue;
                }

                parsed.forEach(x -> {
                    x.put(processingTimeField, timestamp);
                    x.put(SiembolMessageFields.SENSOR_TYPE.toString(), parserResult.getSourceType());
                    if (flags.contains(Flags.ADD_GUID_TO_MESSAGES)) {
                        x.put(SiembolMessageFields.GUID.toString(), UUID.randomUUID().toString());
                    }
                });

                if (metadataObject != null) {
                    parsed.forEach(x -> metadataObject.keySet().forEach(y -> x.put(
                            String.format(metadataFormatMsg, y), metadataObject.get(y))));
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
                currentResult.setMessages(serialised);
                currentResult.setResultType(ParsingApplicationResult.ResultType.PARSED);
                ret.add(currentResult);
            }

            return ret;
        } catch (Exception e) {
            LOG.debug(ERROR_MESSAGE, name, new String(message), metadata, ExceptionUtils.getMessage(e));
            var errorResult = new ParsingApplicationResult(sourceType);
            errorResult.setTopic(errorTopic);
            errorResult.setMessage(getErrorMessage(e, sourceType, message));
            errorResult.setResultType(ParsingApplicationResult.ResultType.ERROR);
            ret.add(errorResult);
            return ret;
        }
    }

    public String getName() {
        return name;
    }

    public static abstract class Builder<T extends ParsingApplicationParser> implements Serializable {
        private static final long serialVersionUID = 1L;
        private static final String METADATA_FORMAT_MSG = "%s";
        protected EnumSet<Flags> flags = EnumSet.noneOf(Flags.class);
        protected String name;
        protected String metadataFormatMsg = METADATA_FORMAT_MSG;
        protected String errorTopic;
        protected String processingTimeField = SiembolMessageFields.PARSING_TIME.toString();
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

        public Builder<T> addGuidToMessages(boolean addGuidToMessages) {
            if (addGuidToMessages) {
                flags.add(Flags.ADD_GUID_TO_MESSAGES);
            }
            return this;
        }

        public Builder<T> metadataPrefix(String metadataPrefix) {
            if (metadataPrefix != null) {
                this.metadataFormatMsg = metadataPrefix + METADATA_FORMAT_MSG;
            }
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
