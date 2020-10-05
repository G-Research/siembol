package uk.co.gresearch.siembol.configeditor.rest.common;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorAttributes;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult;

import java.util.Optional;

public class ConfigEditorHelper {
    public static final String SWAGGER_AUTH_SCHEMA = "security_auth";
    public static ResponseEntity<ConfigEditorResult> fromConfigEditorResult(ConfigEditorResult result) {
        switch (result.getStatusCode()) {
            case OK:
                return new ResponseEntity<>(result, HttpStatus.CREATED);
            case BAD_REQUEST:
                return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
            case UNAUTHORISED:
                return new ResponseEntity<>(result, HttpStatus.UNAUTHORIZED);
        }

        return new ResponseEntity<>(result, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public static Optional<String> getFileContent(ConfigEditorAttributes attributes) {
        return attributes == null || attributes.getFiles() == null || attributes.getFiles().isEmpty()
                ? Optional.empty()
                : Optional.ofNullable(attributes.getFiles().get(0).getContentValue());
    }
}

