package uk.co.gresearch.siembol.common.result;

public class SiembolResult {
    public enum StatusCode {
        OK,
        ERROR
    };

    private final StatusCode statusCode;
    private final SiembolAttributes attributes;

    public SiembolResult(StatusCode statusCode, SiembolAttributes attributes) {
        this.statusCode = statusCode;
        this.attributes = attributes;
    }

    public StatusCode getStatusCode() {
        return statusCode;
    }

    public SiembolAttributes getAttributes() {
        return attributes;
    }

    static public SiembolResult fromErrorMessage(String message) {
        SiembolAttributes attributes = new SiembolAttributes();
        attributes.setMessage(message);
        return new SiembolResult(StatusCode.ERROR, attributes);
    }
}
