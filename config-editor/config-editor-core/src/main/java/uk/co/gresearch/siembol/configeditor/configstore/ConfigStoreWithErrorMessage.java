package uk.co.gresearch.siembol.configeditor.configstore;

import org.springframework.boot.actuate.health.Health;
import uk.co.gresearch.siembol.configeditor.common.UserInfo;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult;

public class ConfigStoreWithErrorMessage implements ConfigStore {
    private final ConfigStore configStore;

    public ConfigStoreWithErrorMessage(ConfigStore configStore) {
        this.configStore = configStore;
    }

    @Override
    public ConfigEditorResult addTestCase(UserInfo user, String testCase) {
        var ret = configStore.addTestCase(user, testCase);
        if (ret.getStatusCode() == ConfigEditorResult.StatusCode.BAD_REQUEST) {
            
        }
        return ret;
    }

    @Override
    public ConfigEditorResult updateTestCase(UserInfo user, String testCase) {
        var ret = configStore.updateTestCase(user, testCase);
        return ret;
    }

    @Override
    public ConfigEditorResult deleteTestCase(UserInfo user, String configName, String testCaseName) {
        var ret = configStore.deleteTestCase(user, configName, testCaseName);
        return ret;
    }

    @Override
    public ConfigEditorResult getTestCases() {
        return configStore.getTestCases();
    }

    @Override
    public ConfigEditorResult addConfig(UserInfo user, String newConfig) {
        var ret = configStore.addConfig(user, newConfig);
        return ret;
    }

    @Override
    public ConfigEditorResult updateConfig(UserInfo user, String configToUpdate) {
        var ret = configStore.updateConfig(user, configToUpdate);
        return ret;
    }

    @Override
    public ConfigEditorResult deleteConfig(UserInfo user, String configName) {
        var ret = configStore.deleteConfig(user, configName);
        return ret;
    }

    @Override
    public ConfigEditorResult getConfigs() {
        return configStore.getConfigs();
    }

    @Override
    public ConfigEditorResult getConfigsReleaseFromCache() {
        return configStore.getConfigsReleaseFromCache();
    }

    @Override
    public ConfigEditorResult getConfigsRelease() {
        return configStore.getConfigsRelease();
    }

    @Override
    public ConfigEditorResult getConfigsReleaseStatus() {
        return configStore.getConfigsReleaseStatus();
    }

    @Override
    public ConfigEditorResult submitConfigsRelease(UserInfo user, String rulesRelease) {
        var ret = configStore.submitConfigsRelease(user, rulesRelease);
        return ret;
    }

    @Override
    public ConfigEditorResult getAdminConfigFromCache() {
        return null;
    }

    @Override
    public ConfigEditorResult getAdminConfig() {
        return null;
    }

    @Override
    public ConfigEditorResult getAdminConfigStatus() {
        return null;
    }

    @Override
    public ConfigEditorResult submitAdminConfig(UserInfo user, String adminConfig) {
        return null;
    }

    @Override
    public ConfigEditorResult getRepositories() {
        return null;
    }

    @Override
    public Health checkHealth() {
        return null;
    }
}
