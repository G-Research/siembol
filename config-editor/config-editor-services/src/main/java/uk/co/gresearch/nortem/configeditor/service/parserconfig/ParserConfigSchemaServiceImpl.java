package uk.co.gresearch.nortem.configeditor.service.parserconfig;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.gresearch.nortem.configeditor.model.ConfigEditorAttributes;
import uk.co.gresearch.nortem.configeditor.model.ConfigEditorResult;
import uk.co.gresearch.nortem.configeditor.common.ConfigEditorUtils;
import uk.co.gresearch.nortem.configeditor.common.ConfigSchemaService;
import uk.co.gresearch.nortem.parsers.common.ParserResult;
import uk.co.gresearch.nortem.parsers.factory.ParserFactory;
import uk.co.gresearch.nortem.parsers.factory.ParserFactoryImpl;
import uk.co.gresearch.nortem.parsers.factory.ParserFactoryResult;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ParserConfigSchemaServiceImpl implements ConfigSchemaService {
    private static final Logger LOG = LoggerFactory
            .getLogger(MethodHandles.lookup().lookupClass());
    private static final ObjectWriter JSON_WRITER = new ObjectMapper()
            .writerFor(new TypeReference<List<Map<String, Object>>>() { })
            .with(SerializationFeature.INDENT_OUTPUT);
    private static final ObjectReader TEST_LOG_READER = new ObjectMapper()
            .readerFor(TestSpecificationDto.class);

    private final ParserFactory parserFactory;
    private final String schema;

    ParserConfigSchemaServiceImpl(ParserFactory parserFactory, String schema) {
        this.parserFactory = parserFactory;
        this.schema = schema;
    }

    @Override
    public ConfigEditorResult getSchema() {
        return ConfigEditorResult.fromSchema(schema);
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

    public static ConfigSchemaService createParserConfigSchemaServiceImpl(Optional<String> uiConfig) throws Exception {
        LOG.info("Initialising parser config schema service");

        ParserFactory parserFactory = ParserFactoryImpl.createParserFactory();
        ParserFactoryResult schemaResult = parserFactory.getSchema();

        if (schemaResult.getStatusCode() != ParserFactoryResult.StatusCode.OK
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

        LOG.info("Initialising parser config schema service completed");
        return new ParserConfigSchemaServiceImpl(parserFactory, computedSchema.get());
    }

    @Override
    public ConfigEditorResult testConfiguration(String config, String event) {
        TestSpecificationDto test;
        try {
            test = TEST_LOG_READER.readValue(event);
        } catch (IOException e) {
            return ConfigEditorResult.fromException(e);
        }
        ParserFactoryResult parserResult = parserFactory.test(config, test.getLog().getBytes());
        return fromParserFactoryTestResult(parserResult);
    }

    private ConfigEditorResult fromParserFactoryValidateResult(ParserFactoryResult parserResult) {
        ConfigEditorAttributes attr = new ConfigEditorAttributes();
        ConfigEditorResult.StatusCode statusCode = parserResult.getStatusCode() == ParserFactoryResult.StatusCode.OK
                ? ConfigEditorResult.StatusCode.OK
                : ConfigEditorResult.StatusCode.ERROR;

        attr.setMessage(parserResult.getAttributes().getMessage());
        return new ConfigEditorResult(statusCode, attr);
    }

    private ConfigEditorResult fromParserFactoryTestResult(ParserFactoryResult parserFactoryResult) {
        ConfigEditorAttributes attr = new ConfigEditorAttributes();
        if (parserFactoryResult.getStatusCode() != ParserFactoryResult.StatusCode.OK
                || parserFactoryResult.getAttributes().getParserResult() == null) {
            attr.setMessage(parserFactoryResult.getAttributes().getMessage());
            return new ConfigEditorResult(ConfigEditorResult.StatusCode.ERROR, attr);
        }

        ParserResult result = parserFactoryResult.getAttributes().getParserResult();
        if (result.getException() != null) {
            return ConfigEditorResult.fromMessage(ConfigEditorResult.StatusCode.ERROR,
                    ExceptionUtils.getStackTrace(result.getException()));
        }

        attr.setTestResultComplete(true);
        try {
            String output = JSON_WRITER.writeValueAsString(result.getParsedMessages());
            attr.setTestResultOutput(output);
        } catch (JsonProcessingException e) {
            return ConfigEditorResult.fromException(e);
        }

        return new ConfigEditorResult(ConfigEditorResult.StatusCode.OK, attr);
    }
}
