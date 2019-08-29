package uk.co.gresearch.nortem.nikita.common;

import org.apache.commons.lang3.exception.ExceptionUtils;

import java.util.Map;

public class NikitaResult {
    public enum StatusCode {
        OK,
        ERROR,
    }

    private final StatusCode statusCode;
    private final NikitaAttributes attributes;

    public NikitaResult(StatusCode statusCode, NikitaAttributes attributes) {
        this.statusCode = statusCode;
        this.attributes = attributes;
    }

    public StatusCode getStatusCode() {
        return statusCode;
    }

    public NikitaAttributes getAttributes() {
        return attributes;
    }

    public static NikitaResult fromEvaluationResult(EvaluationResult result, Map<String, Object> event) {
        NikitaAttributes attributes = new NikitaAttributes();
        attributes.setEvaluationResult(result);
        attributes.setEvent(event);

        return new NikitaResult(StatusCode.OK, attributes);
    }

    public static NikitaResult fromException(Exception e) {
        NikitaAttributes attributes = new NikitaAttributes();
        attributes.setException(ExceptionUtils.getStackTrace(e));
        return new NikitaResult(StatusCode.ERROR, attributes);
    }

    public static NikitaResult fromErrorMessage(String msg) {
        NikitaAttributes attributes = new NikitaAttributes();
        attributes.setMessage(msg);
        return new NikitaResult(StatusCode.ERROR, attributes);
    }
}
