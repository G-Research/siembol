package uk.co.gresearch.siembol.common.storm;

import java.io.Serializable;

public class KafkaBatchWriterMessage implements Serializable {
    private final String topic;
    private final String message;

    public KafkaBatchWriterMessage(String topic, String message) {
        this.topic = topic;
        this.message = message;
    }

    public String getTopic() {
        return topic;
    }

    public String getMessage() {
        return message;
    }
}
