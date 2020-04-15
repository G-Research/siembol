package uk.co.gresearch.siembol.configeditor.common;

public interface AuthorisationProvider {
    enum AuthorisationResult {
        UNDEFINED,
        ALLOWED,
        FORBIDDEN,
    }

    AuthorisationResult getUserAuthorisation(String user, String serviceName);
}
