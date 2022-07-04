package uk.co.gresearch.siembol.configeditor.common;

import org.springframework.boot.actuate.health.Health;
import uk.co.gresearch.siembol.configeditor.model.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult.StatusCode.OK;

public interface ConfigSchemaService extends HealthCheckable {
    String NOT_IMPLEMENTED_MSG = "Not implemented";
    String SCHEMA_INIT_ERROR = "Error during computing json schema";

    ConfigEditorResult getSchema();

    ConfigEditorResult validateConfiguration(String configuration);

    ConfigEditorResult validateConfigurations(String configurations);

    Map<String, ConfigImporter> getConfigImporters();

    default ConfigEditorResult getConfigTesters() {
        ConfigEditorAttributes attributes = new ConfigEditorAttributes();
        attributes.setConfigTesters(new ArrayList<>());
        return new ConfigEditorResult(OK, attributes);
    }

    ConfigTester getConfigTester(String name);

    default Health checkHealth() { return Health.up().build(); }

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

    default ConfigEditorResult importConfig(UserInfo user, String importerName, String importerAttributes, String configToImport) {
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

    default ConfigEditorResult getAdminConfigurationSchema() {
        return ConfigEditorResult.fromMessage(ConfigEditorResult.StatusCode.ERROR, NOT_IMPLEMENTED_MSG);
    }

    default ConfigEditorResult validateAdminConfiguration(String configuration) {
        return ConfigEditorResult.fromMessage(ConfigEditorResult.StatusCode.ERROR, NOT_IMPLEMENTED_MSG);
    }

    default ConfigEditorResult getAdminConfigTopologyName(String configuration) {
        return ConfigEditorResult.fromMessage(ConfigEditorResult.StatusCode.ERROR, NOT_IMPLEMENTED_MSG);
    }

    default ConfigSchemaService withErrorMessage() {
        return new ConfigSchemaServiceWithErrorMessage(this);
    }
}
