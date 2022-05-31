package uk.co.gresearch.siembol.deployment.monitoring.heartbeat;

import org.apache.commons.lang3.exception.ExceptionUtils;

public class HeartbeatProcessingResult {
    private StatusCode statusCode;

    private String message;

    public enum StatusCode {
        OK,
        ERROR;
    }

    public HeartbeatProcessingResult(StatusCode statusCode, String message) {
        this.statusCode = statusCode;
        this.message = message;
    }

    public StatusCode getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(StatusCode statusCode) {
        this.statusCode = statusCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public static HeartbeatProcessingResult fromException(Throwable exception) {
        return new HeartbeatProcessingResult(StatusCode.ERROR, ExceptionUtils.getStackTrace(exception));
    }

    public static HeartbeatProcessingResult fromSuccess() {
        return new HeartbeatProcessingResult(StatusCode.OK, "");
    }

}
