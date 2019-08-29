package uk.co.gresearch.nortem.parsers.common;

import java.util.List;
import java.util.Map;

public class ParserResult {
    private List<Map<String, Object>> parsedMessages;
    private Throwable exception;

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
}
