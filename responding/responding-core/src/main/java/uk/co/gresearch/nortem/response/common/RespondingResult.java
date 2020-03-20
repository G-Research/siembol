package uk.co.gresearch.nortem.response.common;

import org.apache.commons.lang3.exception.ExceptionUtils;
import uk.co.gresearch.nortem.common.result.NortemResult;

import static uk.co.gresearch.nortem.common.result.NortemResult.StatusCode.OK;

public class RespondingResult {
    public enum StatusCode {
        OK,
        ERROR
    }
    private StatusCode statusCode;
    private RespondingResultAttributes attributes;

    public RespondingResult(StatusCode statusCode, RespondingResultAttributes attributes) {
        this.statusCode = statusCode;
        this.attributes = attributes;
    }

    public StatusCode getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(StatusCode statusCode) {
        this.statusCode = statusCode;
    }

    public RespondingResultAttributes getAttributes() {
        return attributes;
    }

    public void setAttributes(RespondingResultAttributes attributes) {
        this.attributes = attributes;
    }

    public static RespondingResult fromEvaluationResult(ResponseEvaluationResult result, ResponseAlert alert) {
        RespondingResultAttributes attributes = new RespondingResultAttributes();
        attributes.setResult(result);
        attributes.setAlert(alert);
        return new RespondingResult(StatusCode.OK, attributes);
    }

    public static RespondingResult fromException(Throwable exception) {
        RespondingResultAttributes attributes = new RespondingResultAttributes();
        attributes.setMessage(ExceptionUtils.getStackTrace(exception));
        return new RespondingResult(StatusCode.ERROR, attributes);
    }

    public static RespondingResult fromNortemResult(NortemResult result) {
        RespondingResultAttributes attributes = new RespondingResultAttributes();
        attributes.setMessage(result.getAttributes().getMessage());
        return new RespondingResult(result.getStatusCode() == OK ? StatusCode.OK : StatusCode.ERROR, attributes);
    }

    public static RespondingResult fromEvaluatorType(String evaluatorType) {
        RespondingResultAttributes attributes = new RespondingResultAttributes();
        attributes.setEvaluatorType(evaluatorType);
        return new RespondingResult(StatusCode.OK, attributes);
    }

    public static RespondingResult fromAttributesSchema(String attributesSchema) {
        RespondingResultAttributes attributes = new RespondingResultAttributes();
        attributes.setAttributesSchema(attributesSchema);
        return new RespondingResult(StatusCode.OK, attributes);
    }

    public static RespondingResult fromEvaluator(Evaluable evaluator) {
        RespondingResultAttributes attributes = new RespondingResultAttributes();
        attributes.setRespondingEvaluator(evaluator);
        return new RespondingResult(StatusCode.OK, attributes);
    }
}
