package uk.co.gresearch.siembol.configeditor.configstore;

import org.springframework.boot.actuate.health.Health;
import uk.co.gresearch.siembol.configeditor.common.UserInfo;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult;
import uk.co.gresearch.siembol.configeditor.common.HealthCheckable;

public interface ConfigStore extends HealthCheckable {

    ConfigEditorResult addTestCase(UserInfo user, String testCase);

    ConfigEditorResult updateTestCase(UserInfo user, String testCase);

    ConfigEditorResult getTestCases();

    ConfigEditorResult addConfig(UserInfo user, String newConfig);

    ConfigEditorResult updateConfig(UserInfo user, String configToUpdate);

    ConfigEditorResult getConfigs();

    ConfigEditorResult getConfigsRelease();

    ConfigEditorResult getConfigsReleaseStatus();

    ConfigEditorResult submitConfigsRelease(UserInfo user, String rulesRelease);

    ConfigEditorResult getRepositories();

    Health checkHealth();
}
