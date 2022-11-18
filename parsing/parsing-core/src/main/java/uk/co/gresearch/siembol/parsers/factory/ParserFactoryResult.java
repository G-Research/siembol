package uk.co.gresearch.siembol.parsers.factory;
/**
 * An object for representing a parser factory result
 *
 * <p>This class represents parser factory result used by a parser factory.
 * It includes a status code of the result along with a parser factory attributes.
 *
 * @author Marian Novotny
 * @see StatusCode
 * @see ParserFactoryAttributes
 *
 */
public class ParserFactoryResult {
    public enum StatusCode {
        OK,
        ERROR
    }

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
