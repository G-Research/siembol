package uk.co.gresearch.siembol.alerts.common;

import org.apache.commons.lang3.exception.ExceptionUtils;

import java.util.Map;
/**
 * An object that combines alerting result status with attributes
 *
 * @author  Marian Novotny
 * @see StatusCode
 * @see AlertingAttributes
 *
 */
public class AlertingResult {
    public enum StatusCode {
        OK,
        ERROR,
    }

    private final StatusCode statusCode;
    private final AlertingAttributes attributes;

    public AlertingResult(StatusCode statusCode, AlertingAttributes attributes) {
        this.statusCode = statusCode;
        this.attributes = attributes;
    }

    public StatusCode getStatusCode() {
        return statusCode;
    }

    public AlertingAttributes getAttributes() {
        return attributes;
    }

    public static AlertingResult fromEvaluationResult(EvaluationResult result, Map<String, Object> event) {
        AlertingAttributes attributes = new AlertingAttributes();
        attributes.setEvaluationResult(result);
        attributes.setEvent(event);

        return new AlertingResult(StatusCode.OK, attributes);
    }

    public static AlertingResult fromException(Exception e) {
        AlertingAttributes attributes = new AlertingAttributes();
        attributes.setException(ExceptionUtils.getStackTrace(e));
        return new AlertingResult(StatusCode.ERROR, attributes);
    }

    public static AlertingResult fromErrorMessage(String msg) {
        AlertingAttributes attributes = new AlertingAttributes();
        attributes.setMessage(msg);
        return new AlertingResult(StatusCode.ERROR, attributes);
    }
}
