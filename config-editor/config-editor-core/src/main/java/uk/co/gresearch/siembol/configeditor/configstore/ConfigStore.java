package uk.co.gresearch.siembol.configeditor.configstore;

import org.springframework.boot.actuate.health.Health;
import uk.co.gresearch.siembol.configeditor.common.UserInfo;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult;
import uk.co.gresearch.siembol.configeditor.common.HealthCheckable;

public interface ConfigStore extends HealthCheckable {
    ConfigEditorResult addTestCase(UserInfo user, String testCase);

    ConfigEditorResult updateTestCase(UserInfo user, String testCase);

    ConfigEditorResult deleteTestCase(UserInfo user, String configName, String testCaseName);

    ConfigEditorResult getTestCases();

    ConfigEditorResult addConfig(UserInfo user, String newConfig);

    ConfigEditorResult updateConfig(UserInfo user, String configToUpdate);

    ConfigEditorResult deleteConfig(UserInfo user, String configName);

    ConfigEditorResult getConfigs();

    ConfigEditorResult getConfigsReleaseFromCache();

    ConfigEditorResult getConfigsRelease();

    ConfigEditorResult getConfigsReleaseStatus();

    ConfigEditorResult submitConfigsRelease(UserInfo user, String rulesRelease);

    ConfigEditorResult getAdminConfigFromCache();

    ConfigEditorResult getAdminConfig();

    ConfigEditorResult getAdminConfigStatus();

    ConfigEditorResult submitAdminConfig(UserInfo user, String adminConfig);

    ConfigEditorResult getRepositories();

    Health checkHealth();

    default ConfigStore withErrorMessage() {
        return new ConfigStoreWithErrorMessage(this);
    }
}
