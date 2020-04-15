package uk.co.gresearch.siembol.parsers.factory;

public class ParserFactoryResult {
    public enum StatusCode {
        OK,
        ERROR
    };

    private final StatusCode statusCode;
    private final ParserFactoryAttributes attributes;

    public ParserFactoryResult(StatusCode statusCode, ParserFactoryAttributes attributes) {
        this.statusCode = statusCode;
        this.attributes = attributes;
    }

    public StatusCode getStatusCode() {
        return statusCode;
    }

    public ParserFactoryAttributes getAttributes() {
        return attributes;
    }

    public static ParserFactoryResult fromErrorMessage(String message) {
        ParserFactoryAttributes attributes = new ParserFactoryAttributes();
        attributes.setMessage(message);
        return new ParserFactoryResult(ParserFactoryResult.StatusCode.ERROR, attributes);
    }

}
