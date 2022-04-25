package uk.co.gresearch.siembol.configeditor.common;

import org.springframework.boot.actuate.health.Health;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult;
import uk.co.gresearch.siembol.configeditor.model.ErrorMessages;
import uk.co.gresearch.siembol.configeditor.model.ErrorResolutions;
import uk.co.gresearch.siembol.configeditor.model.ErrorTitles;

import java.util.Map;
import java.util.function.Supplier;

public class ConfigSchemaServiceWithErrorMessage extends ServiceWithErrorMessage<ConfigSchemaService>
        implements ConfigSchemaService {

    public ConfigSchemaServiceWithErrorMessage(ConfigSchemaService service) {
        super(service);
    }

    @Override
    public ConfigEditorResult getSchema() {
        return service.getSchema();
    }

    @Override
    public ConfigEditorResult validateConfiguration(String configuration) {
        Supplier<ConfigEditorResult> fun = () -> service.validateConfiguration(configuration);
        return executeInternally(fun, ErrorTitles.VALIDATION_GENERIC.getTitle(),
                ErrorMessages.VALIDATION_GENERIC.getMessage(),
                ErrorResolutions.VALIDATION.getResolution());
    }

    @Override
    public ConfigEditorResult validateConfigurations(String configurations) {
        Supplier<ConfigEditorResult> fun = () -> service.validateConfigurations(configurations);
        return executeInternally(fun, ErrorTitles.VALIDATION_GENERIC.getTitle(),
                ErrorMessages.VALIDATION_GENERIC.getMessage(),
                ErrorResolutions.VALIDATION.getResolution());
    }

    @Override
    public Map<String, ConfigImporter> getConfigImporters() {
        return service.getConfigImporters();
    }

    @Override
    public Health checkHealth() {
        return service.checkHealth();
    }

    @Override
    public ConfigEditorResult getImporters() {
        return service.getImporters();
    }

    @Override
    public ConfigEditorResult importConfig(UserInfo user,
                                           String importerName,
                                           String importerAttributes,
                                           String configToImport) {
        Supplier<ConfigEditorResult> fun = () -> service.importConfig(user,
                importerName, importerAttributes, configToImport);
        return executeInternally(fun, ErrorTitles.IMPORTING_CONFIG_GENERIC.getTitle(),
                ErrorMessages.GENERIC_CONFIG_IMPORTER.getMessage(importerName),
                ErrorResolutions.GENERIC_BAD_REQUEST.getResolution());
    }

    @Override
    public ConfigEditorResult getTestSchema() {
        return service.getTestSchema();
    }

    @Override
    public ConfigEditorResult testConfiguration(String configuration, String testSpecification) {
        Supplier<ConfigEditorResult> fun = () -> service.testConfiguration(configuration, testSpecification);
        return executeInternally(fun, ErrorTitles.TESTING_GENERIC.getTitle(),
                ErrorMessages.TESTING_GENERIC.getMessage(),
                ErrorResolutions.GENERIC_BAD_REQUEST.getResolution());
    }

    @Override
    public ConfigEditorResult testConfigurations(String configurations, String event) {
        Supplier<ConfigEditorResult> fun = () -> service.testConfigurations(configurations, event);
        return executeInternally(fun, ErrorTitles.TESTING_GENERIC.getTitle(),
                ErrorMessages.VALIDATION_GENERIC.getMessage(),
                ErrorResolutions.GENERIC_BAD_REQUEST.getResolution());
    }

    @Override
    public ConfigEditorResult getAdminConfigurationSchema() {
        return service.getAdminConfigurationSchema();
    }

    @Override
    public ConfigEditorResult validateAdminConfiguration(String configuration) {
        Supplier<ConfigEditorResult> fun = () -> service.validateAdminConfiguration(configuration);
        return executeInternally(fun, ErrorTitles.VALIDATION_GENERIC.getTitle(),
                ErrorMessages.VALIDATION_GENERIC.getMessage(),
                ErrorResolutions.VALIDATION.getResolution());
    }

    @Override
    public ConfigEditorResult getAdminConfigTopologyName(String configuration) {
        return service.getAdminConfigTopologyName(configuration);
    }
}
