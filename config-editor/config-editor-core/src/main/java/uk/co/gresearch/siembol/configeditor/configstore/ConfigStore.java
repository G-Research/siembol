package uk.co.gresearch.siembol.configeditor.configstore;

import org.springframework.boot.actuate.health.Health;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult;
import uk.co.gresearch.siembol.configeditor.common.HealthCheckable;

public interface ConfigStore extends HealthCheckable {

    ConfigEditorResult addTestCase(String user, String testCase);

    ConfigEditorResult updateTestCase(String user, String testCase);

    ConfigEditorResult getTestCases();

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
