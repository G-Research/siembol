package uk.co.gresearch.siembol.configeditor.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.co.gresearch.siembol.common.result.SiembolResult;

import static uk.co.gresearch.siembol.common.result.SiembolResult.StatusCode.OK;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ConfigEditorResult {
    public enum StatusCode {
        OK(HttpStatus.OK),
        BAD_REQUEST(HttpStatus.BAD_REQUEST),
        UNAUTHORISED(HttpStatus.UNAUTHORIZED),
        ERROR(HttpStatus.INTERNAL_SERVER_ERROR);

        private final HttpStatus httpStatus;

        StatusCode(HttpStatus httpStatus) {
            this.httpStatus = httpStatus;
        }

        public HttpStatus getHttpStatus() {
            return httpStatus;
        }
    }

    private final StatusCode statusCode;
    private final ConfigEditorAttributes attributes;

    public ConfigEditorResult(StatusCode statusCode) {
        this(statusCode, null);
    }

    public ConfigEditorResult(StatusCode statusCode, ConfigEditorAttributes attributes) {
        this.statusCode = statusCode;
        this.attributes = attributes;
    }

    public StatusCode getStatusCode() {
        return statusCode;
    }

    public ConfigEditorAttributes getAttributes() {
        return attributes;
    }

    public static ConfigEditorResult fromMessage(StatusCode statusCode,
                                                 String message) {
        ConfigEditorAttributes attr = new ConfigEditorAttributes();
        attr.setMessage(message);
        return new ConfigEditorResult(statusCode, attr);
    }

    public static ConfigEditorResult fromException(Exception e) {
        return fromException(StatusCode.ERROR, e);
    }

    public static ConfigEditorResult fromException(StatusCode code, Exception e) {
        ConfigEditorAttributes attr = new ConfigEditorAttributes();
        attr.setException(ExceptionUtils.getStackTrace(e));
        return new ConfigEditorResult(code, attr);
    }

    public static ConfigEditorResult fromSchema(String schema) {
        ConfigEditorAttributes attributes = new ConfigEditorAttributes();
        attributes.setRulesSchema(schema);
        return new ConfigEditorResult(ConfigEditorResult.StatusCode.OK,
                attributes);
    }

    public static ConfigEditorResult fromTestSchema(String schema) {
        ConfigEditorAttributes attributes = new ConfigEditorAttributes();
        attributes.setTestSchema(schema);
        return new ConfigEditorResult(ConfigEditorResult.StatusCode.OK,
                attributes);
    }

    public static ConfigEditorResult fromAdminConfigSchema(String schema) {
        ConfigEditorAttributes attributes = new ConfigEditorAttributes();
        attributes.setAdminConfigSchema(schema);
        return new ConfigEditorResult(ConfigEditorResult.StatusCode.OK,
                attributes);
    }

    public static ConfigEditorResult fromServiceContext(ConfigEditorServiceContext context) {
        ConfigEditorAttributes attributes = new ConfigEditorAttributes();
        attributes.setServiceContext(context);
        return new ConfigEditorResult(ConfigEditorResult.StatusCode.OK,
                attributes);
    }

    public static ConfigEditorResult fromValidationResult(SiembolResult siembolResult) {
        ConfigEditorAttributes attributes = new ConfigEditorAttributes();
        attributes.setMessage(siembolResult.getAttributes().getMessage());

        return new ConfigEditorResult(siembolResult.getStatusCode() == OK
                ? ConfigEditorResult.StatusCode.OK : StatusCode.BAD_REQUEST, attributes);
    }

    public ResponseEntity<ConfigEditorAttributes> toResponseEntity() {
        return new ResponseEntity<>(this.attributes, this.statusCode.getHttpStatus());
    }
}
