package uk.co.gresearch.siembol.parsers.application.factory;

public class ParsingApplicationFactoryResult {
    public enum StatusCode {
        OK,
        ERROR
    }

    private StatusCode statusCode;
    private ParsingApplicationFactoryAttributes attributes;

    public ParsingApplicationFactoryResult(StatusCode statusCode, ParsingApplicationFactoryAttributes attributes) {
        this.statusCode = statusCode;
        this.attributes = attributes;
    }

    public StatusCode getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(StatusCode statusCode) {
        this.statusCode = statusCode;
    }

    public ParsingApplicationFactoryAttributes getAttributes() {
        return attributes;
    }

    public void setAttributes(ParsingApplicationFactoryAttributes attributes) {
        this.attributes = attributes;
    }
}
