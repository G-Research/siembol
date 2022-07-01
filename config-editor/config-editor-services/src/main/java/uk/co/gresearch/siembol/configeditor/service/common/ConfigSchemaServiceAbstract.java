package uk.co.gresearch.siembol.configeditor.service.common;

import uk.co.gresearch.siembol.common.jsonschema.SiembolJsonSchemaValidator;
import uk.co.gresearch.siembol.configeditor.common.ConfigImporter;
import uk.co.gresearch.siembol.configeditor.common.ConfigSchemaService;
import uk.co.gresearch.siembol.configeditor.common.ConfigTester;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorAttributes;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult;

import java.util.*;
import java.util.stream.Collectors;

import static uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult.StatusCode.OK;

public abstract class ConfigSchemaServiceAbstract implements ConfigSchemaService {
    private static final String UNKNOWN_CONFIG_TESTER_MSG = "Unknown config tester with name: %s";
    private final String configSchema;
    private final Optional<String> testSchema;
    private final Optional<String> adminConfigSchema;
    private final Optional<SiembolJsonSchemaValidator> adminConfigValidator;
    private final Map<String, ConfigImporter> configImporters;
    private final Map<String, ConfigTester> configTestersMap;

    private final List<ConfigTester> configTestersList;


    public ConfigSchemaServiceAbstract(ConfigSchemaServiceContext context) {
        this.configSchema = context.getConfigSchema();
        this.testSchema = Optional.ofNullable(context.getTestSchema());
        this.adminConfigSchema = Optional.ofNullable(context.getAdminConfigSchema());
        this.adminConfigValidator = Optional.ofNullable(context.getAdminConfigValidator());
        this.configImporters = context.getConfigImporters();
        this.configTestersMap = new HashMap<>();
        context.getConfigTesters().forEach(x -> configTestersMap.put(x.getName(), x));
        this.configTestersList = context.getConfigTesters();
    }

    @Override
    public ConfigEditorResult getSchema() {
        return ConfigEditorResult.fromSchema(configSchema);
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

    @Override
    public ConfigEditorResult getConfigTesters() {
        var attributes = new ConfigEditorAttributes();
        attributes.setConfigTesters(configTestersList.stream()
                .map(x -> x.getConfigTesterInfo())
                .collect(Collectors.toList()));

        return new ConfigEditorResult(OK, attributes);
    }

    @Override
    public ConfigTester getConfigTester(String name) {
        if (!configTestersMap.containsKey(name)) {
            throw new IllegalArgumentException(String.format(UNKNOWN_CONFIG_TESTER_MSG, name));
        }

        return configTestersMap.get(name);
    }
}
