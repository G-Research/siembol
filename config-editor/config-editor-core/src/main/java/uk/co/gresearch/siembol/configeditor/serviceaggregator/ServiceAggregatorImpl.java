package uk.co.gresearch.siembol.configeditor.serviceaggregator;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import uk.co.gresearch.siembol.configeditor.common.*;
import uk.co.gresearch.siembol.configeditor.configstore.ConfigStore;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorService;

import java.util.*;
import java.util.stream.Collectors;

public class ServiceAggregatorImpl implements ServiceAggregator {
    private static final String UNSUPPORTED_SERVICE_MSG = "Unsupported service %s";
    private static final String AUTHORISATION_MSG = "User %s is unauthorised to access the service %s";
    private final AuthorisationProvider authProvider;
    private final Map<String, ServiceAggregatorService> serviceMap;

    ServiceAggregatorImpl(Builder builder) {
        this.authProvider = builder.authProvider;
        this.serviceMap = builder.serviceMap;
    }

    @Override
    public ConfigStore getConfigStore(String user, String serviceName) throws AuthorisationException{
        return getService(user, serviceName).getConfigStore();
    }

    @Override
    public ConfigSchemaService getConfigSchema(String user, String serviceName) throws AuthorisationException {
        return getService(user, serviceName).getConfigSchemaService();
    }

    @Override
    public List<ConfigStore> getConfigStoreServices() {
        return serviceMap.keySet().stream()
                .map(x -> serviceMap.get(x).getConfigStore())
                .collect(Collectors.toList());
    }

    @Override
    public List<ConfigSchemaService> getConfigSchemaServices() {
        return serviceMap.keySet().stream()
                .map(x -> serviceMap.get(x).getConfigSchemaService())
                .collect(Collectors.toList());
    }

    @Override
    public List<ConfigEditorService> getConfigEditorServices(String user) {
        List<ConfigEditorService> ret = new ArrayList<>();
        for (String serviceName : serviceMap.keySet()) {
            try {
                ServiceAggregatorService current = getService(user, serviceName);
                ConfigEditorService configEditorService = new ConfigEditorService();
                configEditorService.setName(serviceName);
                configEditorService.setType(current.getType());
                ret.add(configEditorService);
            } catch (AuthorisationException e) {
                continue;
            }
        }
        ret.sort(Comparator.comparing(ConfigEditorService::getName));
        return ret;
    }

    @Override
    public Health checkConfigStoreServicesHealth() {
        return checkServiceHealth(getConfigStoreServices());
    }

    @Override
    public Health checkConfigSchemaServicesHealth() {
        return checkServiceHealth(getConfigSchemaServices());
    }

    private ServiceAggregatorService getService(String user, String serviceName) throws AuthorisationException  {
        if (!serviceMap.containsKey(serviceName)) {
            throw new UnsupportedOperationException(String.format(UNSUPPORTED_SERVICE_MSG, serviceName));
        }

        AuthorisationProvider.AuthorisationResult authResult = authProvider.getUserAuthorisation(user, serviceName);
        if (authResult == AuthorisationProvider.AuthorisationResult.FORBIDDEN) {
            throw new AuthorisationException(String.format(AUTHORISATION_MSG, user, serviceName));
        }
        return serviceMap.get(serviceName);
    }

    private <T extends HealthCheckable> Health checkServiceHealth(List<T> checkList) {
        for (T service : checkList) {
            Health current = service.checkHealth();
            if (current.getStatus() != Status.UP) {
                return current;
            }
        }
        return new Health.Builder().up().build();
    }

    public static class Builder {
        private static final String SERVICE_ALREADY_REGISTERED = "Service is already registered";
        private static final String NO_SERVICE_REGISTERED = "No services registered in aggregator";
        private final AuthorisationProvider authProvider;
        private Map<String, ServiceAggregatorService> serviceMap = new HashMap<>();

        public Builder(AuthorisationProvider authProvider) {
            this.authProvider = authProvider;
        }

        public Builder addService(String name, String type, ConfigStore store, ConfigSchemaService schemaService) {
            if (serviceMap.containsKey(name)) {
                throw new IllegalArgumentException(SERVICE_ALREADY_REGISTERED);
            }

            ServiceAggregatorService current = new ServiceAggregatorService(type, store, schemaService);
            serviceMap.put(name, current);
            return this;
        }

        public ServiceAggregator build() {
            if (serviceMap.isEmpty()) {
                throw new IllegalArgumentException(NO_SERVICE_REGISTERED);
            }
            return new ServiceAggregatorImpl(this);
        }
    }
}
