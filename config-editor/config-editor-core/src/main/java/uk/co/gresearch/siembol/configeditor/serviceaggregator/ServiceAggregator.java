package uk.co.gresearch.siembol.configeditor.serviceaggregator;

import org.springframework.boot.actuate.health.Health;
import uk.co.gresearch.siembol.configeditor.common.*;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorService;
import uk.co.gresearch.siembol.configeditor.configstore.ConfigStore;

import java.util.List;
import java.util.stream.Collectors;
/**
 * An object for composition of a config store service and a config schema service.
 *
 *
 * <p>This interface is for composing a config store service and a config schema service.
 * It checks an authorisation for a service and a user.
 * It checks health of all services.
 *
 * @author  Marian Novotny
 *
 */
public interface ServiceAggregator {
    /**
     * Gets a config store service for a user
     * @param user a user who is trying to access the service
     * @param serviceName the name of the service
     * @return config store service for the service
     * @throws AuthorisationException if the user is not authorised to the service
     */
    ConfigStore getConfigStore(UserInfo user, String serviceName) throws AuthorisationException;

    /**
     * Gets a config schema service for a user
     * @param user a user who is trying to access the service
     * @param serviceName the name of the service
     * @return config schema service for the service
     * @throws AuthorisationException if the user is not authorised to the service
     */
    ConfigSchemaService getConfigSchema(UserInfo user, String serviceName) throws AuthorisationException;

    /**
     * Gets a list of aggregated services
     * @return the list of aggregated services
     */
    List<ServiceAggregatorService> getAggregatorServices();

    /**
     * Gets a list of config store services
     * @return a list of config store services
     */
    List<ConfigStore> getConfigStoreServices();

    /**
     * Gets a list of config schema services
     * @return a list of config schema services
     */
    List<ConfigSchemaService> getConfigSchemaServices();

    /**
     * Gets a list of services for which the user is authorised
     * @param user a user info object
     * @return a list of services for which the user is authorised
     */
    List<ConfigEditorService> getConfigEditorServices(UserInfo user);

    /**
     * Gets a list of admin services for which the user is authorised
     * @param user a user info object
     * @return a list of services for which the user is authorised
     */
    default List<ConfigEditorService> getConfigEditorAdminServices(UserInfo user) {
        return getConfigEditorServices(user).stream()
                .filter(x -> x.getUserRoles().contains(ServiceUserRole.SERVICE_ADMIN))
                .collect(Collectors.toList());
    }

    /**
     * Checks health of all config store services
     * @return a health object with status
     * @see Health
     */
    Health checkConfigStoreServicesHealth();

    /**
     * Checks health of all config schema services
     * @return a health object with status
     * @see Health
     */
    Health checkConfigSchemaServicesHealth();

    /**
     * Initiates shutting down of all services
     * @return the result  with status
     */
    ConfigEditorResult shutDown();

    /**
     * Waits for shutting down of all services
     * @return the result  with status
     */
    ConfigEditorResult awaitShutDown();
}
