package uk.co.gresearch.siembol.parsers.application.parsing;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.EnumSet;
/**
 * An object for representing a parsing application result
 *
 * <p>This class represents parsing application result used by a parser applications.
 * It includes a result flags along with parsing attributes such as source type and the list of parsed messages.
 *
 * @author Marian Novotny
 *
 */
public class ParsingApplicationResult implements Serializable {
    public enum ResultFlag implements Serializable {
        PARSED,
        ERROR,
        FILTERED,
        TRUNCATED_FIELDS,
        TRUNCATED_ORIGINAL_STRING,
        REMOVED_FIELDS,
        ORIGINAL_MESSAGE,
    }
    private static final long serialVersionUID = 1L;
    private final String sourceType;
    private String topic;
    private ArrayList<String> messages;
    private EnumSet<ResultFlag> resultFlags = EnumSet.noneOf(ResultFlag.class);

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

    public EnumSet<ResultFlag> getResultFlags() {
        return resultFlags;
    }

    public void setResultFlags(EnumSet<ResultFlag> resultFlags) {
        this.resultFlags = resultFlags;
    }
}
