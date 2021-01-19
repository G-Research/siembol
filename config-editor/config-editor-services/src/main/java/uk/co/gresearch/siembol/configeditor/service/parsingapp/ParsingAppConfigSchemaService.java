package uk.co.gresearch.siembol.configeditor.service.parsingapp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.gresearch.siembol.common.jsonschema.SiembolJsonSchemaValidator;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorAttributes;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult;
import uk.co.gresearch.siembol.configeditor.common.ConfigEditorUtils;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorUiLayout;
import uk.co.gresearch.siembol.configeditor.service.common.ConfigSchemaServiceAbstract;
import uk.co.gresearch.siembol.configeditor.service.common.ConfigSchemaServiceContext;
import uk.co.gresearch.siembol.parsers.application.factory.ParsingApplicationFactory;
import uk.co.gresearch.siembol.parsers.application.factory.ParsingApplicationFactoryImpl;
import uk.co.gresearch.siembol.parsers.application.factory.ParsingApplicationFactoryResult;
import uk.co.gresearch.siembol.parsers.storm.StormParsingApplicationAttributesDto;

import java.lang.invoke.MethodHandles;
import java.util.Optional;
import java.util.function.Function;

import static uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult.StatusCode.ERROR;
import static uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult.StatusCode.OK;

public class ParsingAppConfigSchemaService extends ConfigSchemaServiceAbstract {
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final ParsingApplicationFactory factory;

    ParsingAppConfigSchemaService(ParsingApplicationFactory factory, ConfigSchemaServiceContext context) {
        super(context);
        this.factory = factory;
    }

    @Override
    public ConfigEditorResult validateConfiguration(String configs) {
        return validate(configs, x -> factory.validateConfiguration(x));
    }

    @Override
    public ConfigEditorResult validateConfigurations(String configs) {
        return validate(configs, x -> factory.validateConfigurations(x));
    }

    private ConfigEditorResult validate(String config, Function<String, ParsingApplicationFactoryResult> fun) {
        ParsingApplicationFactoryResult factoryResult = fun.apply(config);

        ConfigEditorAttributes attr = new ConfigEditorAttributes();
        if (factoryResult.getStatusCode() == ParsingApplicationFactoryResult.StatusCode.ERROR) {
            attr.setMessage(factoryResult.getAttributes().getMessage());
        }

        ConfigEditorResult.StatusCode statusCode =
                factoryResult.getStatusCode() == ParsingApplicationFactoryResult.StatusCode.OK
                        ? OK
                        : ERROR;

        return new ConfigEditorResult(statusCode, attr);
    }

    public static ParsingAppConfigSchemaService createParsingAppConfigSchemaService(
            ConfigEditorUiLayout uiLayout) throws Exception {
        LOG.info("Initialising parsing app config schema service");

        ConfigSchemaServiceContext context = new ConfigSchemaServiceContext();
        ParsingApplicationFactory factory = new ParsingApplicationFactoryImpl();
        ParsingApplicationFactoryResult schemaResult = factory.getSchema();

        if (schemaResult.getStatusCode() != ParsingApplicationFactoryResult.StatusCode.OK
                || schemaResult.getAttributes().getJsonSchema() == null
                || uiLayout == null) {
            LOG.error(SCHEMA_INIT_ERROR);
            throw new IllegalStateException(SCHEMA_INIT_ERROR);
        }

        Optional<String> computedSchema = ConfigEditorUtils
                .patchJsonSchema(schemaResult.getAttributes().getJsonSchema(), uiLayout.getConfigLayout());
        SiembolJsonSchemaValidator adminConfigValidator = new SiembolJsonSchemaValidator(
                StormParsingApplicationAttributesDto.class);
        Optional<String> adminConfigSchemaUi = ConfigEditorUtils.patchJsonSchema(
                adminConfigValidator.getJsonSchema().getAttributes().getJsonSchema(),
                uiLayout.getAdminConfigLayout());

        if (!computedSchema.isPresent()
                || !adminConfigSchemaUi.isPresent()) {
            LOG.error(SCHEMA_INIT_ERROR);
            throw new IllegalStateException(SCHEMA_INIT_ERROR);
        }

        context.setConfigSchema(computedSchema.get());
        context.setAdminConfigSchema(adminConfigSchemaUi.get());
        context.setAdminConfigValidator(adminConfigValidator);

        LOG.info("Initialising parsing app schema service completed");
        return new ParsingAppConfigSchemaService(factory, context);
    }
}