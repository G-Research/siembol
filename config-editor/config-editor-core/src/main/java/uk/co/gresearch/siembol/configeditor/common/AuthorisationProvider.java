package uk.co.gresearch.siembol.configeditor.common;
/**
 * An object for providing authorisation for Siembol services
 *
 * <p>This interface is for providing authorisation for a Siembol service.
 * It decides whether the user is allowed to access a service under its role.
 *
 * @author  Marian Novotny
 *
 */
public interface AuthorisationProvider {
    enum AuthorisationResult {
        UNDEFINED,
        ALLOWED,
        FORBIDDEN,
    }

    /**
     * Gets authorisation decision for a user and a service
     * @param user a user info object
     * @param serviceName the name of teh service
     * @return the authorisation result
     */
    AuthorisationResult getUserAuthorisation(UserInfo user, String serviceName);
}
