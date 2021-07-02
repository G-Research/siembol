package uk.co.gresearch.siembol.configeditor.rest.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.gresearch.siembol.common.zookeeper.ZooKeeperConnector;
import uk.co.gresearch.siembol.common.zookeeper.ZooKeeperConnectorFactory;
import uk.co.gresearch.siembol.configeditor.common.ConfigInfoProvider;
import uk.co.gresearch.siembol.configeditor.common.ConfigInfoType;
import uk.co.gresearch.siembol.configeditor.common.ServiceType;
import uk.co.gresearch.siembol.configeditor.configinfo.AdminConfigInfoProvider;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult;
import uk.co.gresearch.siembol.configeditor.rest.common.ConfigEditorConfigurationProperties;
import uk.co.gresearch.siembol.configeditor.rest.common.ServiceConfigurationProperties;
import uk.co.gresearch.siembol.configeditor.service.common.ConfigEditorServiceFactory;
import uk.co.gresearch.siembol.configeditor.serviceaggregator.ServiceAggregatorService;
import uk.co.gresearch.siembol.configeditor.sync.common.ConfigServiceHelper;

import java.lang.invoke.MethodHandles;
import java.util.Optional;

public class ConfigServiceHelperImpl implements ConfigServiceHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final String MISSING_ZOOKEEPER_ATTRIBUTES = "Missing release zookeeper attributes in service %s";
    private static final String MISSING_TOPOLOGY_ATTRIBUTES = "Missing topology-image in service %s properties";
    private static final String ZOOKEEPER_CONNECTOR_ERROR = "Problem during initialising zookeeper connector for {}";
    private static final String INIT_RELEASE_ITEM = "{}";
    private static final String RELEASE_ERROR_MSG =
            "Error during getting release item for the service: {} message: {}, exception: {}";
    private static final String VALIDATION_ERROR_MSG =
            "Error during validation item for service: {}, message: {}, exception: {}";


    private final ServiceAggregatorService aggregatorService;
    private final ServiceConfigurationProperties serviceConfigurationProperties;
    private final AdminConfigInfoProvider adminConfigInfoProvider = new AdminConfigInfoProvider();
    private final ConfigInfoProvider configInfoProvider;
    private final Optional<ZooKeeperConnector> zooKeeperConnector;
    private final boolean shouldSyncRelease;
    private final boolean shouldSyncAdminConfig;

    public ConfigServiceHelperImpl(ServiceAggregatorService aggregatorService,
                                   ConfigEditorConfigurationProperties properties,
                                   ZooKeeperConnectorFactory zooKeeperConnectorFactory) {
        this.aggregatorService = aggregatorService;
        this.serviceConfigurationProperties = properties.getServices().get(aggregatorService.getName());
        this.shouldSyncRelease = properties.getSynchronisation().isReleaseEnabled()
                && serviceConfigurationProperties.getSynchronisation() != null
                && serviceConfigurationProperties.getSynchronisation().isReleaseEnabled();

        this.shouldSyncAdminConfig = properties.getSynchronisation().isAdminConfigEnabled()
                && aggregatorService.supportsAdminConfiguration()
                && serviceConfigurationProperties.getSynchronisation() != null
                && serviceConfigurationProperties.getSynchronisation().isAdminConfigEnabled();

        configInfoProvider = ConfigEditorServiceFactory.fromServiceType(aggregatorService.getType())
                .getConfigInfoProvider();

        boolean shouldZookeeperRelease = shouldSyncRelease
                && !aggregatorService.getType().equals(ServiceType.PARSING_APP);

        if (shouldZookeeperRelease && serviceConfigurationProperties.getReleaseZookeeper() == null) {
            throw new IllegalArgumentException(String.format(MISSING_ZOOKEEPER_ATTRIBUTES, aggregatorService.getName()));
        }

        if (shouldSyncAdminConfig && serviceConfigurationProperties.getTopologyImage() == null) {
            throw new IllegalArgumentException(String.format(MISSING_TOPOLOGY_ATTRIBUTES, aggregatorService.getName()));
        }

        try {
            zooKeeperConnector = shouldZookeeperRelease
                    ? Optional.of(zooKeeperConnectorFactory
                    .createZookeeperConnector(serviceConfigurationProperties.getReleaseZookeeper()))
                    : Optional.empty();
        } catch (Exception e) {
            LOGGER.error(ZOOKEEPER_CONNECTOR_ERROR, getName());
            throw new IllegalStateException(e);
        }
    }

    @Override
    public String getName() {
        return aggregatorService.getName();
    }

    @Override
    public ServiceType getType() {
        return aggregatorService.getType();
    }

    private Optional<String> fromReleaseResult(ConfigEditorResult result, ConfigInfoType type) {
        if (result.getStatusCode() != ConfigEditorResult.StatusCode.OK) {
            LOGGER.error(RELEASE_ERROR_MSG,
                    getName(),
                    result.getAttributes().getMessage(),
                    result.getAttributes().getException());
            return Optional.empty();
        }

        int version = result.getAttributes().getReleaseVersion(type);
        if (configInfoProvider.isInitReleaseVersion(version)) {
            return Optional.of(INIT_RELEASE_ITEM);
        }

        return Optional.of(result.getAttributes().getFiles().get(0).getContent());
    }

    @Override
    public Optional<String> getConfigsRelease() {
        return fromReleaseResult(aggregatorService.getConfigStore().getConfigsRelease(),
                configInfoProvider.getConfigInfoType());
    }

    @Override
    public int getReleaseVersion(String release) {
        return configInfoProvider.getReleaseVersion(release);
    }

    private boolean fromValidationResult(ConfigEditorResult result) {
        if (result.getStatusCode() != ConfigEditorResult.StatusCode.OK) {
            LOGGER.error(VALIDATION_ERROR_MSG,
                    getName(),
                    result.getAttributes().getMessage(),
                    result.getAttributes().getException());
            return false;
        }
        return true;
    }

    @Override
    public boolean validateConfigurations(String release) {
        return isInitRelease(release) || fromValidationResult(
                aggregatorService.getConfigSchemaService().validateConfigurations(release));
    }

    @Override
    public Optional<String> getAdminConfig() {
        return fromReleaseResult(aggregatorService.getConfigStore().getAdminConfig(), ConfigInfoType.ADMIN_CONFIG);
    }

    @Override
    public boolean validateAdminConfiguration(String adminConfiguration) {
        return isInitAdminConfig(adminConfiguration) || fromValidationResult(
                aggregatorService.getConfigSchemaService().validateAdminConfiguration(adminConfiguration));
    }

    @Override
    public Optional<ZooKeeperConnector> getZookeeperReleaseConnector() {
        return zooKeeperConnector;
    }

    @Override
    public Optional<String> getStormTopologyImage() {
        return Optional.ofNullable(serviceConfigurationProperties.getTopologyImage());
    }

    @Override
    public Optional<String> getStormTopologyName(String adminConfig) {
        ConfigEditorResult topologyNameResult = aggregatorService
                .getConfigSchemaService()
                .getAdminConfigTopologyName(adminConfig);
        return Optional.ofNullable(topologyNameResult.getAttributes().getTopologyName());
    }

    @Override
    public boolean shouldSyncAdminConfig() {
        return shouldSyncAdminConfig;
    }

    @Override
    public boolean shouldSyncRelease() {
        return shouldSyncRelease;
    }

    @Override
    public boolean isAdminConfigSupported() {
        return aggregatorService.supportsAdminConfiguration();
    }

    @Override
    public boolean isInitAdminConfig(String adminConfig) {
        return INIT_RELEASE_ITEM.equals(adminConfig);
    }

    @Override
    public boolean isInitRelease(String release) {
        return INIT_RELEASE_ITEM.equals(release);
    }

    @Override
    public int getAdminConfigVersion(String adminConfig) {
        return adminConfigInfoProvider.getReleaseVersion(adminConfig);
    }
}
