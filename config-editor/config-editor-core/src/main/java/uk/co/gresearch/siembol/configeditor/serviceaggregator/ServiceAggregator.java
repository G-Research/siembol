package uk.co.gresearch.siembol.configeditor.serviceaggregator;

import org.springframework.boot.actuate.health.Health;
import uk.co.gresearch.siembol.configeditor.common.*;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorService;
import uk.co.gresearch.siembol.configeditor.configstore.ConfigStore;

import java.util.List;
import java.util.stream.Collectors;

public interface ServiceAggregator {
    ConfigStore getConfigStore(UserInfo user, String serviceName) throws AuthorisationException;

    ConfigSchemaService getConfigSchema(UserInfo user, String serviceName) throws AuthorisationException;

    List<ServiceAggregatorService> getAggregatorServices();

    List<ConfigStore> getConfigStoreServices();

    List<ConfigSchemaService> getConfigSchemaServices();

    List<ConfigEditorService> getConfigEditorServices(UserInfo user);

    default List<ConfigEditorService> getConfigEditorAdminServices(UserInfo user) {
        return getConfigEditorServices(user).stream()
                .filter(x -> x.getUserRoles().contains(ServiceUserRole.SERVICE_ADMIN))
                .collect(Collectors.toList());
    }

    Health checkConfigStoreServicesHealth();

    Health checkConfigSchemaServicesHealth();

    ConfigEditorResult shutDown();

    ConfigEditorResult awaitShutDown();
}
