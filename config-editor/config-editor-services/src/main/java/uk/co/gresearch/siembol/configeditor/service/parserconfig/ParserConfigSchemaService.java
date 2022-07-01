package uk.co.gresearch.siembol.configeditor.service.parserconfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.gresearch.siembol.common.jsonschema.SiembolJsonSchemaValidator;
import uk.co.gresearch.siembol.common.model.testing.ParserConfingTestSpecificationDto;
import uk.co.gresearch.siembol.configeditor.common.ConfigEditorUtils;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorAttributes;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult;
import uk.co.gresearch.siembol.configeditor.common.ConfigSchemaService;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorUiLayout;
import uk.co.gresearch.siembol.configeditor.service.common.ConfigSchemaServiceAbstract;
import uk.co.gresearch.siembol.configeditor.service.common.ConfigSchemaServiceContext;
import uk.co.gresearch.siembol.parsers.factory.ParserFactory;
import uk.co.gresearch.siembol.parsers.factory.ParserFactoryImpl;
import uk.co.gresearch.siembol.parsers.factory.ParserFactoryResult;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Optional;

import static uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult.StatusCode.*;

public class ParserConfigSchemaService extends ConfigSchemaServiceAbstract {
    private static final Logger LOG = LoggerFactory
            .getLogger(MethodHandles.lookup().lookupClass());
    private final ParserFactory parserFactory;

    ParserConfigSchemaService(ParserFactory parserFactory,
                              ConfigSchemaServiceContext context) throws Exception {
        super(context);
        this.parserFactory = parserFactory;
    }

    @Override
    public ConfigEditorResult validateConfiguration(String config) {
        ParserFactoryResult parserResult = parserFactory.validateConfiguration(config);
        return fromParserFactoryValidateResult(parserResult);
    }

    @Override
    public ConfigEditorResult validateConfigurations(String configs) {
        ParserFactoryResult parserResult = parserFactory.validateConfigurations(configs);
        return fromParserFactoryValidateResult(parserResult);
    }

    public static ConfigSchemaService createParserConfigSchemaService(ConfigEditorUiLayout uiLayout) throws Exception {
        LOG.info("Initialising parser config schema service");
        ConfigSchemaServiceContext context = new ConfigSchemaServiceContext();
        ParserFactory parserFactory = ParserFactoryImpl.createParserFactory();
        ParserFactoryResult schemaResult = parserFactory.getSchema();

        if (schemaResult.getStatusCode() != ParserFactoryResult.StatusCode.OK
                || schemaResult.getAttributes().getJsonSchema() == null
                || uiLayout == null) {
            LOG.error(SCHEMA_INIT_ERROR);
            throw new IllegalStateException(SCHEMA_INIT_ERROR);
        }

        Optional<String> computedSchema = ConfigEditorUtils
                .patchJsonSchema(schemaResult.getAttributes().getJsonSchema(), uiLayout.getConfigLayout());

        var testValidator = new SiembolJsonSchemaValidator(ParserConfingTestSpecificationDto.class);
        String testValidationSchema = testValidator.getJsonSchema().getAttributes().getJsonSchema();
        Optional<String> testSchema = ConfigEditorUtils.patchJsonSchema(testValidationSchema, uiLayout.getTestLayout());

        if (!computedSchema.isPresent() || !testSchema.isPresent()) {
            LOG.error(SCHEMA_INIT_ERROR);
            throw new IllegalStateException(SCHEMA_INIT_ERROR);
        }

        context.setConfigSchema(computedSchema.get());
        var defaultConfigTester = new ParserConfigTester(testValidator, testSchema.get(), parserFactory);
        context.setConfigTesters(List.of(defaultConfigTester.withErrorMessage()));

        LOG.info("Initialising parser config schema service completed");
        return new ParserConfigSchemaService(parserFactory, context);
    }

    private ConfigEditorResult fromParserFactoryValidateResult(ParserFactoryResult parserResult) {
        ConfigEditorAttributes attr = new ConfigEditorAttributes();
        ConfigEditorResult.StatusCode statusCode = parserResult.getStatusCode() == ParserFactoryResult.StatusCode.OK
                ? OK
                : ConfigEditorResult.StatusCode.BAD_REQUEST;

        attr.setMessage(parserResult.getAttributes().getMessage());
        return new ConfigEditorResult(statusCode, attr);
    }
}
