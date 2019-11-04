package uk.co.gresearch.nortem.configeditor.common;

import org.springframework.boot.actuate.health.Health;
import uk.co.gresearch.nortem.configeditor.model.ConfigEditorResult;

public interface ConfigSchemaService extends HealthCheckable {
    String NOT_IMPLEMENTED_MSG = "Not implemented";
    String SCHEMA_INIT_ERROR = "Error during computing json schema";

    ConfigEditorResult getSchema();

    ConfigEditorResult validateConfiguration(String configuration);

    ConfigEditorResult validateConfigurations(String configurations);

    default Health checkHealth() { return Health.up().build(); };

    default ConfigEditorResult getTestSchema() {
        return ConfigEditorResult.fromMessage(ConfigEditorResult.StatusCode.ERROR, NOT_IMPLEMENTED_MSG);
    }

    default ConfigEditorResult getFields() {
        return ConfigEditorResult.fromMessage(ConfigEditorResult.StatusCode.ERROR, NOT_IMPLEMENTED_MSG);
    }

    default ConfigEditorResult testConfiguration(String configuration, String testSpecification ) {
        return ConfigEditorResult.fromMessage(ConfigEditorResult.StatusCode.ERROR, NOT_IMPLEMENTED_MSG);
    }

    default ConfigEditorResult testConfigurations(String configurations, String event) {
        return ConfigEditorResult.fromMessage(ConfigEditorResult.StatusCode.ERROR, NOT_IMPLEMENTED_MSG);
    }

}
