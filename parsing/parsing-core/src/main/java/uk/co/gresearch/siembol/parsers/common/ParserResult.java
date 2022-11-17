package uk.co.gresearch.siembol.parsers.common;

import java.util.List;
import java.util.Map;
/**
 * An object for representing a parser result
 *
 * <p>This class contains a list of parsed messages and
 *  exception if a parser throws an error.
 *  It provides metadata about the parsed messages such as the source type.
 *
 * @author Marian Novotny
 *
 */
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
