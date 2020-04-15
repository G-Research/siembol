package uk.co.gresearch.siembol.parsers.application.parsing;

import java.io.Serializable;
import java.util.ArrayList;

public class ParsingApplicationResult implements Serializable {
    private final String topic;
    private final ArrayList<String> messages;

    public ParsingApplicationResult(String topic, ArrayList<String> messages) {
        this.topic = topic;
        this.messages = messages;
    }
    public ParsingApplicationResult(String topic, String message) {
        this.topic = topic;
        messages = new ArrayList<>();
        messages.add(message);
    }

    public String getTopic() {
        return topic;
    }

    public ArrayList<String> getMessages() {
        return messages;
    }

}
