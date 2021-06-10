package uk.co.gresearch.siembol.configeditor.common;

import org.springframework.boot.actuate.health.Health;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorAttributes;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult;
import uk.co.gresearch.siembol.configeditor.model.ConfigImporterDto;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public interface ConfigSchemaService extends HealthCheckable {
    String UNKNOWN_CONFIG_IMPORTER_MSG = "Unknown config importer: %s";
    String NOT_IMPLEMENTED_MSG = "Not implemented";
    String SCHEMA_INIT_ERROR = "Error during computing json schema";

    ConfigEditorResult getSchema();

    ConfigEditorResult validateConfiguration(String configuration);

    ConfigEditorResult validateConfigurations(String configurations);

    Map<String, ConfigImporter> getConfigImporters();

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
        return new ConfigEditorResult(ConfigEditorResult.StatusCode.OK, attributes);
    }

    default ConfigEditorResult importConfig(UserInfo user, String importerName, String importerAttributes, String configToImport) {
        if (!getConfigImporters().containsKey(importerName)) {
            return  ConfigEditorResult.fromMessage(ConfigEditorResult.StatusCode.BAD_REQUEST,
                    String.format(UNKNOWN_CONFIG_IMPORTER_MSG, importerName));
        }

        return getConfigImporters().get(importerName).importConfig(user, importerAttributes, configToImport);
    }

    default ConfigEditorResult getTestSchema() {
        return ConfigEditorResult.fromMessage(ConfigEditorResult.StatusCode.ERROR, NOT_IMPLEMENTED_MSG);
    }

    default ConfigEditorResult testConfiguration(String configuration, String testSpecification ) {
        return ConfigEditorResult.fromMessage(ConfigEditorResult.StatusCode.ERROR, NOT_IMPLEMENTED_MSG);
    }

    default ConfigEditorResult testConfigurations(String configurations, String event) {
        return ConfigEditorResult.fromMessage(ConfigEditorResult.StatusCode.ERROR, NOT_IMPLEMENTED_MSG);
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
}
