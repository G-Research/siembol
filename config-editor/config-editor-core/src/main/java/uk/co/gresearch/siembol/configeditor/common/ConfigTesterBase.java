package uk.co.gresearch.siembol.configeditor.common;

import uk.co.gresearch.siembol.common.jsonschema.SiembolJsonSchemaValidator;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult;

public abstract class ConfigTesterBase<T> implements ConfigTester {
    private final SiembolJsonSchemaValidator testValidator;

    private final String testSchema;

    protected final T testProvider;

    protected ConfigTesterBase(SiembolJsonSchemaValidator testValidator, String testSchema, T testProvider) {
        this.testValidator = testValidator;
        this.testSchema = testSchema;
        this.testProvider = testProvider;
    }

    @Override
    public ConfigEditorResult getTestSpecificationSchema() {
        return ConfigEditorResult.fromTestSchema(testSchema);
    }

    @Override
    public ConfigEditorResult validateTestSpecification(String attributes) {
        var validateResult = testValidator.validate(attributes);
        return ConfigEditorResult.fromValidationResult(validateResult);
    }
}
