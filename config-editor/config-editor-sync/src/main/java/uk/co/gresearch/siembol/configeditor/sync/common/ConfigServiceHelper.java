package uk.co.gresearch.siembol.configeditor.sync.common;

import uk.co.gresearch.siembol.common.zookeper.ZookeeperConnector;
import uk.co.gresearch.siembol.configeditor.common.ServiceType;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult;

import java.util.Optional;

public interface ConfigServiceHelper {
    String getName();
    ServiceType getType();
    Optional<String> getConfigsRelease();
    Optional<String> getAdminConfig();
    boolean validateAdminConfiguration(String adminConfiguration);
    boolean validateConfigurations(String release);
    int getReleaseVersion(String release);
    int getAdminConfigVersion(String release);
    boolean shouldSyncAdminConfig();
    boolean shouldSyncRelease();
    boolean isAdminConfigSupported();
    Optional<ZookeeperConnector> getZookeeperReleaseConnector();
    Optional<String> getStormTopologyImage();
    Optional<String> getStormTopologyName(String adminConfig);
}
