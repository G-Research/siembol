package uk.co.gresearch.siembol.parsers.application.factory;
/**
 * An object for representing a parsing application factory result
 *
 * <p>This class represents parsing application factory result used by a parsing application factory.
 * It includes a status code along with attributes.
 *
 * @author Marian Novotny
 * @see ParsingApplicationFactoryAttributes
 *
 */
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
