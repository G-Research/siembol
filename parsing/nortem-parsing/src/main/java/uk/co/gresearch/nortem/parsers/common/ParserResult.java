package uk.co.gresearch.nortem.parsers.common;

import java.util.List;
import java.util.Map;

public class ParserResult {
    private List<Map<String, Object>> parsedMessages;
    private Throwable exception;
    private String topic;
    private String sourceType;

    public List<Map<String, Object>> getParsedMessages() {
        return parsedMessages;
    }

    public void setParsedMessages(List<Map<String, Object>> parsedMessages) {
        this.parsedMessages = parsedMessages;
    }

    public Throwable getException() {
        return exception;
    }

    public void setException(Throwable exception) {
        this.exception = exception;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }
}
