package uk.co.gresearch.siembol.configeditor.common;

import org.springframework.boot.actuate.health.Health;
import uk.co.gresearch.siembol.configeditor.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult.StatusCode.OK;
/**
 * An object for configuration schema service
 *
 * <p>This interface is for providing functionality for configuration schema service.
 * It validates configurations, and provides a json schema.
 * it provides config importers and testers registered for the service.
 *
 * @author  Marian Novotny
 * @see ConfigTester
 * @see ConfigImporter
 *
 */
public interface ConfigSchemaService extends HealthCheckable {
    String NOT_IMPLEMENTED_MSG = "Not implemented";
    String SCHEMA_INIT_ERROR = "Error during computing json schema";

    /**
     * Gets a json schema for configurations
     * @return a config editor result with a json schema for configurations
     */
    ConfigEditorResult getSchema();

    /**
     * Validates a configuration
     * @param configuration a json string with configuration
     * @return a config editor result with OK status code if the configuration is valid, otherwise
     *         the result with ERROR status.
     */
    ConfigEditorResult validateConfiguration(String configuration);

    /**
     * Validates configurations
     * @param configurations a json string with configurations
     * @return a config editor result with OK status code if the configurations are valid, otherwise
     *         the result with ERROR status.
     */
    ConfigEditorResult validateConfigurations(String configurations);

    /**
     * Gets config importers
     * @return the map of an importer name string to a config importer object
     */
    Map<String, ConfigImporter> getConfigImporters();

    /**
     * Gets config testers
     * @return a config editor result with config testers registered for the service
     */
    default ConfigEditorResult getConfigTesters() {
        ConfigEditorAttributes attributes = new ConfigEditorAttributes();
        attributes.setConfigTesters(new ArrayList<>());
        return new ConfigEditorResult(OK, attributes);
    }

    /**
     * Gets a config tester by name
     * @return a config tester
     */
    ConfigTester getConfigTester(String name);

    /**
     * Checks a health of teh service
     * @return a health object with the status
     * @see Health
     */
    default Health checkHealth() { return Health.up().build(); }

    /**
     * Get config importers
     * @return config editor result with config importers
     */
    default ConfigEditorResult getImporters() {
        List<ConfigImporterDto> importers = getConfigImporters().entrySet().stream().map(x -> {
            ConfigImporterDto importer = new ConfigImporterDto();
            importer.setImporterName(x.getKey());
            importer.setImporterAttributesSchema(
                    x.getValue().getImporterAttributesSchema().getAttributes().getConfigImporterAttributesSchema());
            return importer;
        }).collect(Collectors.toList());
        ConfigEditorAttributes attributes = new ConfigEditorAttributes();
        attributes.setConfigImporters(importers);
        return new ConfigEditorResult(OK, attributes);
    }

    /**
     * Imports a config into a service syntax
     * @param user a user info object
     * @param importerName the name of teh importer
     * @param importerAttributes a json string with importer attributes
     * @param configToImport a string with configuration to be imported
     * @return config editor result with OK status code and the imported config if the import was successful, otherwise
     *         the result with ERROR status.
     */
    default ConfigEditorResult importConfig(UserInfo user,
                                            String importerName,
                                            String importerAttributes,
                                            String configToImport) {
        if (!getConfigImporters().containsKey(importerName)) {
            return  ConfigEditorResult.fromMessage(ConfigEditorResult.StatusCode.BAD_REQUEST,
                    ErrorMessages.UNKNOWN_CONFIG_IMPORTER.getMessage(importerName));
        }

        ConfigEditorResult importResult =  getConfigImporters().get(importerName)
                .importConfig(user, importerAttributes, configToImport);
        if (importResult.getStatusCode() != OK) {
            return importResult;
        }

        ConfigEditorResult validationResult = validateConfiguration(
                importResult.getAttributes().getImportedConfiguration());
        if (validationResult.getStatusCode() != OK) {
            return validationResult;
        }

        return importResult;
    }

    /**
     * Gets a json schema for an admin configuration
     * @return a config editor result with a json schema for an admin configuration
     */
    default ConfigEditorResult getAdminConfigurationSchema() {
        return ConfigEditorResult.fromMessage(ConfigEditorResult.StatusCode.ERROR, NOT_IMPLEMENTED_MSG);
    }

    /**
     * Validates an admin configuration
     * @param configuration a json string with an admin configuration
     * @return a config editor result with OK status code if the admin configuration is valid, otherwise
     *         the result with ERROR status.
     */
    default ConfigEditorResult validateAdminConfiguration(String configuration) {
        return ConfigEditorResult.fromMessage(ConfigEditorResult.StatusCode.ERROR, NOT_IMPLEMENTED_MSG);
    }

    /**
     * Gets a topology name from an admin configuration
     * @param configuration a json string with an admin configuration
     * @return a config editor result with the topology name on success, otherwise
     *         the result with ERROR status.
     */
    default ConfigEditorResult getAdminConfigTopologyName(String configuration) {
        return ConfigEditorResult.fromMessage(ConfigEditorResult.StatusCode.ERROR, NOT_IMPLEMENTED_MSG);
    }

    /**
     * Get a config schema service with enhanced error messages
     * @return a config schema service with enhanced error messages
     */
    default ConfigSchemaService withErrorMessage() {
        return new ConfigSchemaServiceWithErrorMessage(this);
    }
}
