package uk.co.gresearch.siembol.configeditor.sync.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import uk.co.gresearch.siembol.common.model.StormTopologyDto;
import uk.co.gresearch.siembol.configeditor.common.ServiceType;
import uk.co.gresearch.siembol.configeditor.sync.actions.*;
import uk.co.gresearch.siembol.configeditor.sync.common.ConfigServiceHelper;
import uk.co.gresearch.siembol.configeditor.model.*;
import uk.co.gresearch.siembol.configeditor.sync.common.SynchronisationType;

import java.lang.invoke.MethodHandles;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult.StatusCode.OK;

public class SynchronisationServiceImpl implements SynchronisationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final String DUPLICATE_SERVICE_MSG = "The service %s has been already added";
    private static final String SYNC_COMPLETED_MSG = "Synchronisation completed";
    private static final String ERROR_SYNC_MSG = "Error during synchronising the service %s";
    private static final String ERROR_UPDATE_TOPOLOGIES_MSG = "Error synchronising storm topologies for the services %s";
    private static final String INIT_START_MSG = "Starting initialising synchronisation service";
    private static final String INIT_COMPLETED_MSG = "Initialisation of synchronisation service completed";
    private static final String EMPTY_ACTIONS_MSG = "No actions registered in synchronisation service";
    private static final String SERVICES_SYNC_REQUEST_MSG = "Requested synchronisation {} of services: {}";
    private static final String SERVICE_SYNC_RELEASE_DISABLED =
            "Release synchronisation with zookeeper is disabled for the service: {}";
    private static final String RELEASING_TOPOLOGIES_DISABLED =
            "Synchronisation releasing topologies is disabled for the service: {}";

    private final List<String> allServiceNames;
    private final StormApplicationProvider stormProvider;
    private final Map<SynchronisationType, Map<String, SynchronisationAction>> syncTypeToActionsMap;
    private final AtomicReference<Exception> exception = new AtomicReference<>();

    SynchronisationServiceImpl(Builder builder) {
        allServiceNames = builder.allServiceNames;
        stormProvider = builder.stormProvider;
        syncTypeToActionsMap = builder.syncTypeToActionsMap;
    }

    private ConfigEditorResult executeActions(List<String> serviceNames,
                                              Map<String, SynchronisationAction> serviceActionsMaps) {
        List<ConfigEditorServiceContext> serviceContexts = new ArrayList<>();

        for (String serviceName : serviceNames) {
            if (!serviceActionsMaps.containsKey(serviceName)) {
                continue;
            }

            ConfigEditorServiceContext currentContext = new ConfigEditorServiceContext();
            currentContext.setServiceName(serviceName);
            ConfigEditorResult currentResult = serviceActionsMaps.get(serviceName).execute(currentContext);
            if (currentResult.getStatusCode() != OK) {
                String msg = String.format(ERROR_SYNC_MSG, serviceName);
                LOGGER.error(msg);
                exception.set(new IllegalStateException(msg));
                return currentResult;
            }
            serviceContexts.add(currentResult.getAttributes().getServiceContext());
        }

        Set<String> servicesWithChangedTopologies = new HashSet<>();
        List<StormTopologyDto> topologies = new ArrayList<>();
        serviceContexts.forEach(x -> {
            if (x.getStormTopologies().isPresent()) {
                topologies.addAll(x.getStormTopologies().get());
                servicesWithChangedTopologies.add(x.getServiceName());
            }
        });

        if (!servicesWithChangedTopologies.isEmpty()) {
            ConfigEditorResult topologiesUpdateResult = stormProvider
                    .updateStormTopologies(topologies, servicesWithChangedTopologies);
            if (topologiesUpdateResult.getStatusCode() != OK) {
                String msg = String.format(ERROR_UPDATE_TOPOLOGIES_MSG, servicesWithChangedTopologies.toString());
                LOGGER.error(msg);
                exception.set(new IllegalStateException(msg));
                return topologiesUpdateResult;
            }
        }

        return ConfigEditorResult.fromMessage(OK, SYNC_COMPLETED_MSG);
    }

    @Override
    public ConfigEditorResult synchroniseServices(List<String> serviceNames, SynchronisationType syncType) {
        LOGGER.info(SERVICES_SYNC_REQUEST_MSG, syncType.toString(), serviceNames.toString());
        Map<String, SynchronisationAction> actionsMap = syncTypeToActionsMap.get(syncType);
        return executeActions(serviceNames, actionsMap);
    }

    @Override
    public ConfigEditorResult synchroniseAllServices(SynchronisationType syncType) {
        return synchroniseServices(allServiceNames, syncType);
    }

    @Override
    public Health checkHealth() {
        return exception.get() == null
                ? Health.up().build()
                : Health.down().withException(exception.get()).build();
    }

    public static class Builder {
        List<String> allServiceNames = new ArrayList<>();
        StormApplicationProvider stormProvider;
        Map<SynchronisationType, Map<String, SynchronisationAction>> syncTypeToActionsMap = new HashMap<>();

        private Map<String, SynchronisationAction> releaseSyncActions = new HashMap<>();
        private Map<String, SynchronisationAction> adminConfigSyncActions = new HashMap<>();
        private Map<String, SynchronisationAction> allSyncActions = new HashMap<>();

        private void addService(ConfigServiceHelper serviceHelper) {
            CompositeSyncAction.Builder releaseBuilder = new CompositeSyncAction.Builder();
            CompositeSyncAction.Builder adminConfigBuilder = new CompositeSyncAction.Builder();
            CompositeSyncAction.Builder allSyncBuilder = new CompositeSyncAction.Builder();

            if (serviceHelper.shouldSyncRelease()) {
                SynchronisationAction getRelease = new GetReleaseAction(serviceHelper);
                SynchronisationAction updateRelease = new UpdateReleaseInZookeeperAction(serviceHelper);

                releaseBuilder.addAction(getRelease).addAction(updateRelease);
                allSyncBuilder.addAction(getRelease).addAction(updateRelease);
            } else {
                LOGGER.warn(SERVICE_SYNC_RELEASE_DISABLED, serviceHelper.getName());
            }

            if (serviceHelper.shouldSyncAdminConfig()) {
                SynchronisationAction getAdminConfig = new GetAdminConfigAction(serviceHelper);
                SynchronisationAction getStormTopology = new GetStormTopologyAction(serviceHelper);

                adminConfigBuilder.addAction(getAdminConfig).addAction(getStormTopology);
                allSyncBuilder.addAction(getAdminConfig).addAction(getStormTopology);
            } else if (serviceHelper.isAdminConfigSupported()) {
                LOGGER.warn(RELEASING_TOPOLOGIES_DISABLED, serviceHelper.getName());
            }

            if (!releaseBuilder.isEmpty()) {
                releaseSyncActions.put(serviceHelper.getName(), releaseBuilder.build());
            }

            if (!adminConfigBuilder.isEmpty()) {
                adminConfigSyncActions.put(serviceHelper.getName(), adminConfigBuilder.build());
            }

            if (!allSyncBuilder.isEmpty()) {
                allSyncActions.put(serviceHelper.getName(), allSyncBuilder.build());
            }
        }

        private void addParsingAppService(ConfigServiceHelper serviceHelper) {
            if (!serviceHelper.shouldSyncRelease() || !serviceHelper.shouldSyncAdminConfig()) {
                LOGGER.warn(RELEASING_TOPOLOGIES_DISABLED, serviceHelper.getName());
                return;
            }

            CompositeSyncAction serviceAction = new CompositeSyncAction.Builder()
                    .addAction(new GetReleaseAction(serviceHelper))
                    .addAction(new GetAdminConfigAction(serviceHelper))
                    .addAction(new GetParsingAppStormTopologyAction(serviceHelper))
                    .build();

            releaseSyncActions.put(serviceHelper.getName(), serviceAction);
            adminConfigSyncActions.put(serviceHelper.getName(), serviceAction);
            allSyncActions.put(serviceHelper.getName(), serviceAction);
        }

        public Builder(StormApplicationProvider stormProvider) {
            this.stormProvider = stormProvider;
        }

        public Builder addConfigServiceHelpers(List<ConfigServiceHelper> services) {
            for (ConfigServiceHelper service : services) {
                if (allServiceNames.contains(service.getName())) {
                    throw new IllegalArgumentException(String.format(DUPLICATE_SERVICE_MSG, service.getName()));
                }

                if (service.getType().equals(ServiceType.PARSING_APP)) {
                    addParsingAppService(service);
                } else {
                    addService(service);
                }
                allServiceNames.add(service.getName());
            }
            return this;
        }

        public SynchronisationService build() {
            LOGGER.info(INIT_START_MSG);
            if (allSyncActions.isEmpty()) {
                throw new IllegalArgumentException(EMPTY_ACTIONS_MSG);
            }

            syncTypeToActionsMap.put(SynchronisationType.RELEASE, releaseSyncActions);
            syncTypeToActionsMap.put(SynchronisationType.ADMIN_CONFIG, adminConfigSyncActions);
            syncTypeToActionsMap.put(SynchronisationType.ALL, allSyncActions);

            LOGGER.info(INIT_COMPLETED_MSG);
            return new SynchronisationServiceImpl(this);
        }
    }
}

