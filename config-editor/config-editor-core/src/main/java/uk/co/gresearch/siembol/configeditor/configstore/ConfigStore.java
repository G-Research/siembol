package uk.co.gresearch.siembol.configeditor.configstore;

import org.springframework.boot.actuate.health.Health;
import uk.co.gresearch.siembol.configeditor.common.UserInfo;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult;
import uk.co.gresearch.siembol.configeditor.common.HealthCheckable;
/**
 * An object for storing and manipulating Siembol configurations
 *
 * <p>This interface is for storing and manipulating Siembol configurations.
 * It stores configurations, test cases, admin config and the release.
 * It checks health of the service.
 *
 * @author  Marian Novotny
 *
 */
public interface ConfigStore extends HealthCheckable {
    /**
     * Adds a test case into the store
     * @param user the metadata about the user
     * @param testCase a json string test case
     * @return config editor result with the status and the files after adding the test cases
     */
    ConfigEditorResult addTestCase(UserInfo user, String testCase);

    /**
     * Updates an existing test case in the store
     * @param user the metadata about the user
     * @param testCase a json string test case
     * @return config editor result with the status and the files after updating the test cases
     */
    ConfigEditorResult updateTestCase(UserInfo user, String testCase);

    /**
     * Deletes existing test case in the store
     * @param user the metadata about the user
     * @param configName the name of configuration of the test case
     * @param testCaseName the name of teh test case to be deleted
     * @return config editor result with the status and the files after updating the test cases
     */
    ConfigEditorResult deleteTestCase(UserInfo user, String configName, String testCaseName);

    /**
     * Gets test cases from the store
     * @return config editor result with test cases files
     */
    ConfigEditorResult getTestCases();

    /**
     * Adds a configuration into the store
     * @param user the metadata about the user
     * @param newConfig a json string configuration
     * @return config editor result with the status and the files after adding the configuration
     */
    ConfigEditorResult addConfig(UserInfo user, String newConfig);

    /**
     * Updates a configuration in the store
     * @param user the metadata about the user
     * @param configToUpdate a json string configuration
     * @return config editor result with the status and the files after updating the configuration
     */
    ConfigEditorResult updateConfig(UserInfo user, String configToUpdate);

    /**
     * Deletes existing test case in the store
     * @param user the metadata about the user
     * @param configName the name of configuration to be deleted
     * @return config editor result with the status and the files after deleting
     */
    ConfigEditorResult deleteConfig(UserInfo user, String configName);

    /**
     * Gets configurations from the store
     * @return config editor result with configuration files
     */
    ConfigEditorResult getConfigs();

    /**
     * Gets the release from cache
     * @return config editor result with the release file
     */
    ConfigEditorResult getConfigsReleaseFromCache();

    /**
     * Gets the release from the store
     * @return config editor result with the release file
     */
    ConfigEditorResult getConfigsRelease();

    /**
     * Gets the release status related to pending pull requests
     * @return config editor result with the release status
     */
    ConfigEditorResult getConfigsReleaseStatus();

    /**
     * Submits the release into the store
     * @param user the metadata about the user
     * @param rulesRelease a json string with the release
     * @return config editor result with the release status
     */
    ConfigEditorResult submitConfigsRelease(UserInfo user, String rulesRelease);

    /**
     * Gets the admin configuration from the cache
     * @return config editor result with the admin configuration file
     */
    ConfigEditorResult getAdminConfigFromCache();

    /**
     * Gets the admin configuration from the store
     * @return config editor result with the admin configuration file
     */
    ConfigEditorResult getAdminConfig();

    /**
     * Gets the admin configuration status related to pending pull requests
     * @return config editor result with the admin configuration status
     */
    ConfigEditorResult getAdminConfigStatus();

    /**
     * Submits the admin configuration into the store
     * @param user the metadata about the user
     * @param adminConfig a json string with the admin configuration
     * @return config editor result with the admin configuration status
     */
    ConfigEditorResult submitAdminConfig(UserInfo user, String adminConfig);

    /**
     * Gets urls to all store repositories
     * @return the config editor result with urls to all store repositories
     */
    ConfigEditorResult getRepositories();

    /**
     * Checks the health of the store
     * @return a health object with the status information
     * @see Health
     */
    Health checkHealth();

    /**
     * Gets an store instance with enhanced error message
     * @return
     */
    default ConfigStore withErrorMessage() {
        return new ConfigStoreWithErrorMessage(this);
    }
}
