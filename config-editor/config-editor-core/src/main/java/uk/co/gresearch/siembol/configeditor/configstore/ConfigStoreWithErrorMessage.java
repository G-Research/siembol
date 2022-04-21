package uk.co.gresearch.siembol.configeditor.configstore;

import org.springframework.boot.actuate.health.Health;
import uk.co.gresearch.siembol.configeditor.common.ServiceWithErrorMessage;
import uk.co.gresearch.siembol.configeditor.common.UserInfo;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult;
import uk.co.gresearch.siembol.configeditor.model.ErrorMessages;
import uk.co.gresearch.siembol.configeditor.model.ErrorResolutions;
import uk.co.gresearch.siembol.configeditor.model.ErrorTitles;

import java.util.function.Supplier;

public class ConfigStoreWithErrorMessage extends ServiceWithErrorMessage<ConfigStore> implements ConfigStore {
    public ConfigStoreWithErrorMessage(ConfigStore service) {
        super(service);
    }

    @Override
    public ConfigEditorResult addTestCase(UserInfo user, String testCase) {
        Supplier<ConfigEditorResult> fun = () -> service.addTestCase(user, testCase);
        return executeInternally(fun, ErrorTitles.ADD_TEST_CASE.getTitle(),
                ErrorMessages.GENERIC_WRONG_REQUEST.getMessage(),
                ErrorResolutions.GENERIC_BAD_REQUEST.getResolution());
    }

    @Override
    public ConfigEditorResult updateTestCase(UserInfo user, String testCase) {
        Supplier<ConfigEditorResult> fun = () -> service.updateTestCase(user, testCase);
        return executeInternally(fun, ErrorTitles.UPDATE_TEST_CASE.getTitle(),
                ErrorMessages.GENERIC_WRONG_REQUEST.getMessage(),
                ErrorResolutions.GENERIC_BAD_REQUEST.getResolution());
    }

    @Override
    public ConfigEditorResult deleteTestCase(UserInfo user, String configName, String testCaseName) {
        Supplier<ConfigEditorResult> fun = () -> service.deleteTestCase(user, configName, testCaseName);
        return executeInternally(fun, ErrorTitles.DELETE_TEST_CASE.getTitle(testCaseName),
                ErrorMessages.GENERIC_WRONG_REQUEST.getMessage(),
                ErrorResolutions.GENERIC_BAD_REQUEST.getResolution());
    }

    @Override
    public ConfigEditorResult getTestCases() {
        return service.getTestCases();
    }

    @Override
    public ConfigEditorResult addConfig(UserInfo user, String newConfig) {
        Supplier<ConfigEditorResult> fun = () -> service.addConfig(user, newConfig);
        return executeInternally(fun, ErrorTitles.ADD_CONFIG.getTitle(),
                ErrorMessages.GENERIC_WRONG_REQUEST.getMessage(),
                ErrorResolutions.GENERIC_BAD_REQUEST.getResolution());
    }

    @Override
    public ConfigEditorResult updateConfig(UserInfo user, String configToUpdate) {
        Supplier<ConfigEditorResult> fun = () -> service.addConfig(user, configToUpdate);
        return executeInternally(fun, ErrorTitles.UPDATE_CONFIG.getTitle(),
                ErrorMessages.GENERIC_WRONG_REQUEST.getMessage(),
                ErrorResolutions.GENERIC_BAD_REQUEST.getResolution());
    }

    @Override
    public ConfigEditorResult deleteConfig(UserInfo user, String configName) {
        Supplier<ConfigEditorResult> fun = () -> service.addConfig(user, configName);
        return executeInternally(fun, ErrorTitles.DELETE_CONFIG.getTitle(configName),
                ErrorMessages.GENERIC_WRONG_REQUEST.getMessage(),
                ErrorResolutions.GENERIC_BAD_REQUEST.getResolution());
    }

    @Override
    public ConfigEditorResult getConfigs() {
        return service.getConfigs();
    }

    @Override
    public ConfigEditorResult getConfigsReleaseFromCache() {
        return service.getConfigsReleaseFromCache();
    }

    @Override
    public ConfigEditorResult getConfigsRelease() {
        return service.getConfigsRelease();
    }

    @Override
    public ConfigEditorResult getConfigsReleaseStatus() {
        return service.getConfigsReleaseStatus();
    }

    @Override
    public ConfigEditorResult submitConfigsRelease(UserInfo user, String rulesRelease) {
        Supplier<ConfigEditorResult> fun = () -> service.submitConfigsRelease(user, rulesRelease);
        return executeInternally(fun, ErrorTitles.CREATE_RELEASE_PR.getTitle(),
                ErrorMessages.GENERIC_WRONG_REQUEST.getMessage(),
                ErrorResolutions.GENERIC_BAD_REQUEST.getResolution());
    }

    @Override
    public ConfigEditorResult getAdminConfigFromCache() {
        return service.getAdminConfigFromCache();
    }

    @Override
    public ConfigEditorResult getAdminConfig() {
        return service.getAdminConfig();
    }

    @Override
    public ConfigEditorResult getAdminConfigStatus() {
        return service.getAdminConfigStatus();
    }

    @Override
    public ConfigEditorResult submitAdminConfig(UserInfo user, String adminConfig) {
        Supplier<ConfigEditorResult> fun = () -> service.submitAdminConfig(user, adminConfig);
        return executeInternally(fun, ErrorTitles.CREATE_ADMIN_CONFIG_PR.getTitle(),
                ErrorMessages.GENERIC_WRONG_REQUEST.getMessage(),
                ErrorResolutions.GENERIC_BAD_REQUEST.getResolution());
    }

    @Override
    public ConfigEditorResult getRepositories() {
        return service.getRepositories();
    }

    @Override
    public Health checkHealth() {
        return service.checkHealth();
    }
}
