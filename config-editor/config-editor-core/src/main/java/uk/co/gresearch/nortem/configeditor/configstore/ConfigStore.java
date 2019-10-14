package uk.co.gresearch.nortem.configeditor.configstore;

import org.springframework.boot.actuate.health.Health;
import uk.co.gresearch.nortem.configeditor.model.ConfigEditorResult;
import uk.co.gresearch.nortem.configeditor.common.HealthCheckable;

public interface ConfigStore extends HealthCheckable {

    ConfigEditorResult addConfig(String user, String newConfig);

    ConfigEditorResult updateConfig(String user, String configToUpdate);

    ConfigEditorResult getConfigs();

    ConfigEditorResult getConfigsRelease();

    ConfigEditorResult getConfigsReleaseStatus();

    ConfigEditorResult submitConfigsRelease(String user, String rulesRelease);

    ConfigEditorResult getRepositories();

    ConfigEditorResult shutDown();

    ConfigEditorResult awaitShutDown();

    Health checkHealth();
}
