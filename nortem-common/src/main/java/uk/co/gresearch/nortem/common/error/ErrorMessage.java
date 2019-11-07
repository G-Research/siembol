package uk.co.gresearch.nortem.common.error;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import java.util.UUID;

import static java.nio.charset.StandardCharsets.UTF_8;

public class ErrorMessage {
    private static final ObjectWriter JSON_WRITER = new ObjectMapper()
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .writerFor(ErrorMessage.class);

    @JsonProperty("guid")
    private String guid = UUID.randomUUID().toString();
    @JsonProperty("message")
    private String message;
    @JsonProperty("raw_message")
    private String rawMessage;
    @JsonProperty("failed_sensor_type")
    private String failedSensorType;
    @JsonProperty("error_type")
    private ErrorType errorType = ErrorType.DEFAULT_ERROR;
    @JsonProperty("stack_trace")
    private String stackTrace;
    @JsonProperty("timestamp")
    private Long timestamp = System.currentTimeMillis();
    @JsonProperty("source.type")
    private String sourceType = "error";

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getRawMessage() {
        return rawMessage;
    }

    public void setRawMessage(String rawMessage) {
        this.rawMessage = rawMessage;
    }

    public String getFailedSensorType() {
        return failedSensorType;
    }

    public void setFailedSensorType(String failedSensorType) {
        this.failedSensorType = failedSensorType;
    }

    public ErrorType getErrorType() {
        return errorType;
    }

    public void setErrorType(ErrorType errorType) {
        this.errorType = errorType;
    }

    public String getStackTrace() {
        return stackTrace;
    }

    public void setStackTrace(String stackTrace) {
        this.stackTrace = stackTrace;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public void setRawMessage(byte[] message) {
        rawMessage = new String(message, UTF_8);
    }

    @Override
    public String toString() {
        try {
            return JSON_WRITER.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
