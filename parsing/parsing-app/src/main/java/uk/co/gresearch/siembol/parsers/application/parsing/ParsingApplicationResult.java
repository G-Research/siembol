package uk.co.gresearch.siembol.parsers.application.parsing;

import java.io.Serializable;
import java.util.ArrayList;

public class ParsingApplicationResult implements Serializable {
    public enum ResultType implements Serializable {
        PARSED,
        ERROR,
        FILTERED
    }
    private static final long serialVersionUID = 1L;
    private final String sourceType;
    private String topic;
    private ArrayList<String> messages;
    private ResultType resultType = ResultType.PARSED;

    public ParsingApplicationResult(String sourceType) {
        this.sourceType = sourceType;
    }

    public String getTopic() {
        return topic;
    }

    public ArrayList<String> getMessages() {
        return messages;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setMessages(ArrayList<String> messages) {
        this.messages = messages;
    }

    public void setMessage(String message) {
        this.messages = new ArrayList<>();
        this.messages.add(message);
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public ResultType getResultType() {
        return resultType;
    }

    public void setResultType(ResultType resultType) {
        this.resultType = resultType;
    }
}
