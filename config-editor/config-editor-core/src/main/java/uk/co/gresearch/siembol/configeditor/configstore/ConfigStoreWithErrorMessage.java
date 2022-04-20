package uk.co.gresearch.siembol.configeditor.configstore;

import org.springframework.boot.actuate.health.Health;
import uk.co.gresearch.siembol.configeditor.common.UserInfo;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult;
import uk.co.gresearch.siembol.configeditor.model.ErrorTitles;

import java.util.function.Supplier;

public class ConfigStoreWithErrorMessage implements ConfigStore {
    private final ConfigStore configStore;

    public ConfigStoreWithErrorMessage(ConfigStore configStore) {
        this.configStore = configStore;
    }

    @Override
    public ConfigEditorResult addTestCase(UserInfo user, String testCase) {
        Supplier<ConfigEditorResult> fun = () -> configStore.addTestCase(user, testCase);
        return executeInternally(fun, ErrorTitles.ADD_TEST_CASE.getTitle(),
                null, null);
    }

    @Override
    public ConfigEditorResult updateTestCase(UserInfo user, String testCase) {
        Supplier<ConfigEditorResult> fun = () -> configStore.updateTestCase(user, testCase);
        return executeInternally(fun, ErrorTitles.UPDATE_TEST_CASE.getTitle(), null, null);
    }

    @Override
    public ConfigEditorResult deleteTestCase(UserInfo user, String configName, String testCaseName) {
        Supplier<ConfigEditorResult> fun = () -> configStore.deleteTestCase(user, configName, testCaseName);
        return executeInternally(fun, null, null, null);
    }

    @Override
    public ConfigEditorResult getTestCases() {
        return configStore.getTestCases();
    }

    @Override
    public ConfigEditorResult addConfig(UserInfo user, String newConfig) {
        Supplier<ConfigEditorResult> fun = () -> configStore.addConfig(user, newConfig);
        return executeInternally(fun, null, null, null);
    }

    @Override
    public ConfigEditorResult updateConfig(UserInfo user, String configToUpdate) {
        Supplier<ConfigEditorResult> fun = () -> configStore.addConfig(user, configToUpdate);
        return executeInternally(fun, null, null, null);
    }

    @Override
    public ConfigEditorResult deleteConfig(UserInfo user, String configName) {
        Supplier<ConfigEditorResult> fun = () -> configStore.addConfig(user, configName);
        return executeInternally(fun, null, null, null);
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
        Supplier<ConfigEditorResult> fun = () -> configStore.submitConfigsRelease(user, rulesRelease);
        return executeInternally(fun, null, null, null);
    }

    @Override
    public ConfigEditorResult getAdminConfigFromCache() {
        return configStore.getAdminConfigFromCache();
    }

    @Override
    public ConfigEditorResult getAdminConfig() {
        return configStore.getAdminConfig();
    }

    @Override
    public ConfigEditorResult getAdminConfigStatus() {
        return configStore.getAdminConfigStatus();
    }

    @Override
    public ConfigEditorResult submitAdminConfig(UserInfo user, String adminConfig) {
        return null;
    }

    @Override
    public ConfigEditorResult getRepositories() {
        return configStore.getRepositories();
    }

    @Override
    public Health checkHealth() {
        return configStore.checkHealth();
    }

    private ConfigEditorResult executeInternally(Supplier<ConfigEditorResult> supplier,
                                                 String title,
                                                 String message,
                                                 String resolution) {
        var ret = supplier.get();
        if (ret.getStatusCode() == ConfigEditorResult.StatusCode.BAD_REQUEST) {
            var attributes = ret.getAttributes();
            attributes.setErrorTitleIfNotPresent(title);
            attributes.setMessageIfNotPresent(message);
            attributes.setErrorResolutionIfNotPresent(message);
        }
        return ret;
    }
}
