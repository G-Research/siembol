package uk.co.gresearch.siembol.configeditor.serviceaggregator;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import uk.co.gresearch.siembol.configeditor.common.*;
import uk.co.gresearch.siembol.configeditor.configstore.ConfigStore;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class ServiceAggregatorImpl implements ServiceAggregator {
    private static final String UNSUPPORTED_SERVICE_MSG = "Unsupported service %s";
    private static final String AUTHORISATION_MSG = "User %s is unauthorised to access the service %s";
    private final AuthorisationProvider authProvider;
    private final Map<String, ConfigStore> configStores;
    private final Map<String, ConfigSchemaService> configSchemaServices;

    ServiceAggregatorImpl(Builder builder) {
        this.authProvider = builder.authProvider;
        this.configStores = builder.configStores;
        this.configSchemaServices = builder.configSchemaServices;
    }

    @Override
    public ConfigStore getConfigStore(String user, String serviceName) {
        return getService(user, serviceName,  configStores);
    }

    @Override
    public ConfigSchemaService getConfigSchema(String user, String serviceName) {
        return getService(user, serviceName,  configSchemaServices);
    }

    @Override
    public List<ConfigStore> getConfigStoreServices() {
        return new ArrayList<>(configStores.values());
    }

    @Override
    public List<ConfigSchemaService> getConfigSchemaServices() {
        return new ArrayList<>(configSchemaServices.values());
    }

    @Override
    public List<ConfigEditorService> getConfigEditorServices(String user) {
        List<ConfigEditorService> ret = new ArrayList<>();
        for (String serviceName : configSchemaServices.keySet()) {
            try {
                ConfigSchemaService current = getConfigSchema(user, serviceName);
                ConfigEditorService configEditorService = new ConfigEditorService();
                configEditorService.setName(serviceName);
                ret.add(configEditorService);
            } catch (AuthorisationException e) {
                continue;
            }
        }
        return ret;
    }

    @Override
    public Health checkConfigStoreServicesHealth() {
        return checkServiceHealth(configStores);
    }

    @Override
    public Health checkConfigSchemaServicesHealth() {
        return checkServiceHealth(configSchemaServices);
    }

    private <T> T getService(String user, String serviceName,  Map<String, T> serviceMap)  {
        if (!serviceMap.containsKey(serviceName)) {
            throw new UnsupportedOperationException(String.format(UNSUPPORTED_SERVICE_MSG, serviceName));
        }

        AuthorisationProvider.AuthorisationResult authResult = authProvider.getUserAuthorisation(user, serviceName);
        if (authResult == AuthorisationProvider.AuthorisationResult.FORBIDDEN) {
            throw new AuthorisationException(String.format(AUTHORISATION_MSG, user, serviceName));
        }
        return serviceMap.get(serviceName);
    }

    private <T extends HealthCheckable> Health checkServiceHealth(Map<String, T> serviceMap)  {
        for (String name : serviceMap.keySet()) {
            Health current = serviceMap.get(name).checkHealth();
            try {
                if (current.getStatus() != Status.UP) {
                    return current;
                }
            } catch (Exception e) {
                new Health.Builder().down(e).build();
            }
        }
        return new Health.Builder().up().build();
    }

    public static class Builder {
        private static final String SERVICE_ALREADY_REGISTERED = "Service is already registered";
        private static final String NO_SERVICE_REGISTERED = "No services registered in aggregator";
        private final AuthorisationProvider authProvider;
        private final Map<String, ConfigStore> configStores = new HashMap<>();
        private final Map<String, ConfigSchemaService> configSchemaServices = new HashMap<>();

        public Builder(AuthorisationProvider authProvider) {
            this.authProvider = authProvider;
        }

        public Builder addService(String name, ConfigStore store, ConfigSchemaService schemaService) {
            if (configStores.containsKey(name)) {
                throw new IllegalArgumentException(SERVICE_ALREADY_REGISTERED);
            }
            configStores.put(name, store);
            configSchemaServices.put(name, schemaService);
            return this;
        }

        public ServiceAggregator build() {
            if (configStores.isEmpty()) {
                throw new IllegalArgumentException(NO_SERVICE_REGISTERED);
            }
            return new ServiceAggregatorImpl(this);
        }
    }
}
