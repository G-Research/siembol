package uk.co.gresearch.siembol.configeditor.rest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorAttributes;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult;

import java.security.Principal;
import java.util.Optional;

public class ConfigEditorHelper {
    private static final String UNSUPPORTED_USER_PRINCIPAL_FORMAT =
            "Unsupported principal format in the authentication structure";

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

    public static String getUserNameFromAuthentication(Authentication authentication) {
        Object userPrincipal = authentication.getPrincipal();
        String userName;
        if (userPrincipal instanceof UserDetails) {
            userName = ((UserDetails) userPrincipal).getUsername();
        } else if (userPrincipal instanceof Principal) {
            userName = ((Principal)userPrincipal).getName();
        } else {
            throw new IllegalArgumentException(UNSUPPORTED_USER_PRINCIPAL_FORMAT);
        }
        return userName.contains("\\") ? userName.substring(userName.indexOf("\\") + 1) : userName;
    }

    public static Optional<String> getFileContent(ConfigEditorAttributes attributes) {
        return attributes == null || attributes.getFiles() == null || attributes.getFiles().isEmpty()
                ? Optional.empty()
                : Optional.ofNullable(attributes.getFiles().get(0).getContentValue());
    }

}

