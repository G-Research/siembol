package uk.co.gresearch.siembol.configeditor.service.parsingapp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorAttributes;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult;
import uk.co.gresearch.siembol.configeditor.common.ConfigEditorUtils;
import uk.co.gresearch.siembol.configeditor.common.ConfigSchemaService;
import uk.co.gresearch.siembol.parsers.application.factory.ParsingApplicationFactory;
import uk.co.gresearch.siembol.parsers.application.factory.ParsingApplicationFactoryImpl;
import uk.co.gresearch.siembol.parsers.application.factory.ParsingApplicationFactoryResult;

import java.lang.invoke.MethodHandles;
import java.util.Optional;
import java.util.function.Function;

import static uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult.StatusCode.ERROR;
import static uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult.StatusCode.OK;

public class ParsingAppConfigSchemaServiceImpl implements ConfigSchemaService {
    private static final Logger LOG = LoggerFactory
            .getLogger(MethodHandles.lookup().lookupClass());
    private final ParsingApplicationFactory factory;
    private final String schema;

    ParsingAppConfigSchemaServiceImpl(ParsingApplicationFactory factory, String schema) {
        this.factory = factory;
        this.schema = schema;
    }

    @Override
    public ConfigEditorResult getSchema() {
        return ConfigEditorResult.fromSchema(schema);
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

    public static ParsingAppConfigSchemaServiceImpl createParserConfigSchemaServiceImpl(
            Optional<String> uiConfig) throws Exception {
        LOG.info("Initialising parsing app config schema service");

        ParsingApplicationFactory factory = new ParsingApplicationFactoryImpl();
        ParsingApplicationFactoryResult schemaResult = factory.getSchema();

        if (schemaResult.getStatusCode() != ParsingApplicationFactoryResult.StatusCode.OK
                || schemaResult.getAttributes().getJsonSchema() == null
                || !uiConfig.isPresent()) {
            LOG.error(SCHEMA_INIT_ERROR);
            throw new IllegalStateException(SCHEMA_INIT_ERROR);
        }

        Optional<String> computedSchema = ConfigEditorUtils
                .computeRulesSchema(schemaResult.getAttributes().getJsonSchema(), uiConfig.get());

        if (!computedSchema.isPresent()) {
            LOG.error(SCHEMA_INIT_ERROR);
            throw new IllegalStateException(SCHEMA_INIT_ERROR);
        }

        LOG.info("Initialising parsing app schema service completed");
        return new ParsingAppConfigSchemaServiceImpl(factory, computedSchema.get());
    }
}