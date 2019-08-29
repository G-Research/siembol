package uk.co.gresearch.nortem.common.result;

public class NortemResult {
    public enum StatusCode {
        OK,
        ERROR
    };

    private final StatusCode statusCode;
    private final NortemAttributes attributes;

    public NortemResult(StatusCode statusCode, NortemAttributes attributes) {
        this.statusCode = statusCode;
        this.attributes = attributes;
    }

    public StatusCode getStatusCode() {
        return statusCode;
    }

    public NortemAttributes getAttributes() {
        return attributes;
    }

    static public NortemResult fromErrorMessage(String message) {
        NortemAttributes attributes = new NortemAttributes();
        attributes.setMessage(message);
        return new NortemResult(StatusCode.ERROR, attributes);
    }
}
