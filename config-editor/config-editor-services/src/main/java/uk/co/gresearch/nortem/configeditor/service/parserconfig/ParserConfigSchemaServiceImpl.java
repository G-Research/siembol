package uk.co.gresearch.nortem.configeditor.service.parserconfig;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.gresearch.nortem.common.jsonschema.NortemJsonSchemaValidator;
import uk.co.gresearch.nortem.common.result.NortemResult;
import uk.co.gresearch.nortem.configeditor.common.ConfigEditorUtils;
import uk.co.gresearch.nortem.configeditor.model.ConfigEditorAttributes;
import uk.co.gresearch.nortem.configeditor.model.ConfigEditorResult;
import uk.co.gresearch.nortem.configeditor.common.ConfigSchemaService;
import uk.co.gresearch.nortem.parsers.common.ParserResult;
import uk.co.gresearch.nortem.parsers.factory.ParserFactory;
import uk.co.gresearch.nortem.parsers.factory.ParserFactoryImpl;
import uk.co.gresearch.nortem.parsers.factory.ParserFactoryResult;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.co.gresearch.nortem.configeditor.model.ConfigEditorResult.StatusCode.ERROR;
import static uk.co.gresearch.nortem.configeditor.model.ConfigEditorResult.StatusCode.OK;

public class ParserConfigSchemaServiceImpl implements ConfigSchemaService {
    private static final Logger LOG = LoggerFactory
            .getLogger(MethodHandles.lookup().lookupClass());
    private static final ObjectWriter JSON_WRITER_MESSAGES = new ObjectMapper()
            .writerFor(new TypeReference<List<Map<String, Object>>>() { })
            .with(SerializationFeature.INDENT_OUTPUT);
    private static final ObjectWriter JSON_WRITER_RESULT = new ObjectMapper()
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .writerFor(new TypeReference<ParserResult>() { })
            .with(SerializationFeature.INDENT_OUTPUT);

    private static final ObjectReader TEST_LOG_READER = new ObjectMapper()
            .readerFor(ParserConfingTestSpecificationDto.class);

    private final ParserFactory parserFactory;
    private final String schema;
    private final NortemJsonSchemaValidator testSchemaValidator;

    ParserConfigSchemaServiceImpl(ParserFactory parserFactory,
                                  String schema) throws Exception {
        this.parserFactory = parserFactory;
        this.schema = schema;
        this.testSchemaValidator = new NortemJsonSchemaValidator(ParserConfingTestSpecificationDto.class);
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

    public static ConfigSchemaService createParserConfigSchemaServiceImpl(Optional<String> uiConfig ) throws Exception {
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
    public ConfigEditorResult testConfiguration(String config, String testSpecification) {
        NortemResult validationResult  = testSchemaValidator.validate(testSpecification);
        if (validationResult.getStatusCode() != NortemResult.StatusCode.OK) {
            return ConfigEditorResult.fromMessage(ERROR, validationResult.getAttributes().getMessage());
        }

        try {
            ParserConfingTestSpecificationDto test = TEST_LOG_READER.readValue(testSpecification);
            ParserFactoryResult parserResult = parserFactory.test(config,
                    test.getMetadata(),
                    test.getEncoding().decode(test.getLog()));
            return fromParserFactoryTestResult(parserResult);
        } catch (Exception e) {
            return ConfigEditorResult.fromException(e);
        }
    }

    private ConfigEditorResult fromParserFactoryValidateResult(ParserFactoryResult parserResult) {
        ConfigEditorAttributes attr = new ConfigEditorAttributes();
        ConfigEditorResult.StatusCode statusCode = parserResult.getStatusCode() == ParserFactoryResult.StatusCode.OK
                ? OK
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
            String testOutput = JSON_WRITER_MESSAGES.writeValueAsString(result.getParsedMessages());
            attr.setTestResultOutput(testOutput);
            String rawTestResult = JSON_WRITER_RESULT.writeValueAsString(result);
            attr.setTestResultRawOutput(rawTestResult);
        } catch (JsonProcessingException e) {
            return ConfigEditorResult.fromException(e);
        }

        return new ConfigEditorResult(OK, attr);
    }

    @Override
    public ConfigEditorResult getTestSchema() {
        ConfigEditorAttributes attr = new ConfigEditorAttributes();
        attr.setTestSchema(testSchemaValidator.getJsonSchema().getAttributes().getJsonSchema());
        return new ConfigEditorResult(OK, attr);
    }
}
