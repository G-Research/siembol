package uk.co.gresearch.siembol.configeditor.service.common;

import uk.co.gresearch.siembol.common.jsonschema.SiembolJsonSchemaValidator;
import uk.co.gresearch.siembol.configeditor.common.ConfigImporter;
import uk.co.gresearch.siembol.configeditor.common.ConfigSchemaService;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult;

import java.util.Map;
import java.util.Optional;

public abstract class ConfigSchemaServiceAbstract implements ConfigSchemaService {
    private final String configSchema;
    private final Optional<String> testSchema;
    private final Optional<String> adminConfigSchema;
    private final Optional<SiembolJsonSchemaValidator> adminConfigValidator;
    private final Map<String, ConfigImporter> configImporters;

    public ConfigSchemaServiceAbstract(ConfigSchemaServiceContext context) {
        this.configSchema = context.getConfigSchema();
        this.testSchema = Optional.ofNullable(context.getTestSchema());
        this.adminConfigSchema = Optional.ofNullable(context.getAdminConfigSchema());
        this.adminConfigValidator = Optional.ofNullable(context.getAdminConfigValidator());
        this.configImporters = context.getConfigImporters();
    }

    @Override
    public ConfigEditorResult getSchema() {
        return ConfigEditorResult.fromSchema(configSchema);
    }

    @Override
    public ConfigEditorResult getTestSchema() {
        if (!testSchema.isPresent()) {
            return ConfigEditorResult.fromMessage(ConfigEditorResult.StatusCode.ERROR, NOT_IMPLEMENTED_MSG);
        }
        return ConfigEditorResult.fromTestSchema(testSchema.get());
    }

    @Override
    public ConfigEditorResult getAdminConfigurationSchema() {
        if (!adminConfigSchema.isPresent()) {
            return ConfigEditorResult.fromMessage(ConfigEditorResult.StatusCode.ERROR, NOT_IMPLEMENTED_MSG);
        }
        return ConfigEditorResult.fromAdminConfigSchema(adminConfigSchema.get());
    }

    @Override
    public ConfigEditorResult validateAdminConfiguration(String configuration) {
        if (!adminConfigValidator.isPresent()) {
            return ConfigEditorResult.fromMessage(ConfigEditorResult.StatusCode.ERROR, NOT_IMPLEMENTED_MSG);
        }
        return ConfigEditorResult.fromValidationResult(adminConfigValidator.get().validate(configuration));
    }

    @Override
    public Map<String, ConfigImporter> getConfigImporters() {
        return configImporters;
    }
}
