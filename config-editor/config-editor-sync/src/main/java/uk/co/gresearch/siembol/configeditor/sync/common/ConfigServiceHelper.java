package uk.co.gresearch.siembol.configeditor.sync.common;

import uk.co.gresearch.siembol.common.zookeeper.ZooKeeperConnector;
import uk.co.gresearch.siembol.configeditor.common.ServiceType;

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
    boolean isInitAdminConfig(String adminConfig);
    boolean isInitRelease(String release);
    Optional<ZooKeeperConnector> getZookeeperReleaseConnector();
    Optional<String> getStormTopologyImage();
    Optional<String> getStormTopologyName(String adminConfig);
}
