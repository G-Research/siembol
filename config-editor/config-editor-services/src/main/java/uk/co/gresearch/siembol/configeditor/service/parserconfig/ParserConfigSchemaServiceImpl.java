package uk.co.gresearch.siembol.configeditor.service.parserconfig;

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

import uk.co.gresearch.siembol.common.jsonschema.SiembolJsonSchemaValidator;
import uk.co.gresearch.siembol.common.result.SiembolResult;
import uk.co.gresearch.siembol.configeditor.common.ConfigEditorUtils;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorAttributes;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult;
import uk.co.gresearch.siembol.configeditor.common.ConfigSchemaService;
import uk.co.gresearch.siembol.parsers.common.ParserResult;
import uk.co.gresearch.siembol.parsers.factory.ParserFactory;
import uk.co.gresearch.siembol.parsers.factory.ParserFactoryImpl;
import uk.co.gresearch.siembol.parsers.factory.ParserFactoryResult;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult.StatusCode.ERROR;
import static uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult.StatusCode.OK;

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
    private final String testSchema;
    private final SiembolJsonSchemaValidator testSchemaValidator;

    ParserConfigSchemaServiceImpl(ParserFactory parserFactory,
                                  String schema,
                                  String testSchema) throws Exception {
        this.parserFactory = parserFactory;
        this.schema = schema;
        this.testSchema = testSchema;
        this.testSchemaValidator = new SiembolJsonSchemaValidator(ParserConfingTestSpecificationDto.class);
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

    public static ConfigSchemaService createParserConfigSchemaService(Optional<String> uiConfig,
                                                                      Optional<String> testUiConfig) throws Exception {
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
                .patchJsonSchema(schemaResult.getAttributes().getJsonSchema(), uiConfig.get());

        if (!computedSchema.isPresent()) {
            LOG.error(SCHEMA_INIT_ERROR);
            throw new IllegalStateException(SCHEMA_INIT_ERROR);
        }

        String testValidationSchema = new SiembolJsonSchemaValidator(ParserConfingTestSpecificationDto.class)
                .getJsonSchema().getAttributes().getJsonSchema();

        Optional<String> testSchema = testUiConfig.isPresent()
                ? ConfigEditorUtils.patchJsonSchema(testValidationSchema, testUiConfig.get())
                : Optional.of(testValidationSchema);

        if (!computedSchema.isPresent() || !testSchema.isPresent()) {
            LOG.error(SCHEMA_INIT_ERROR);
            throw new IllegalStateException(SCHEMA_INIT_ERROR);
        }

        LOG.info("Initialising parser config schema service completed");
        return new ParserConfigSchemaServiceImpl(parserFactory, computedSchema.get(), testSchema.get());
    }

    @Override
    public ConfigEditorResult testConfiguration(String config, String testSpecification) {
        SiembolResult validationResult  = testSchemaValidator.validate(testSpecification);
        if (validationResult.getStatusCode() != SiembolResult.StatusCode.OK) {
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
        attr.setTestResultComplete(true);
        try {
            String testOutput = result.getException() == null
                    ? JSON_WRITER_MESSAGES.writeValueAsString(result.getParsedMessages())
                    : ExceptionUtils.getStackTrace(result.getException());
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
        attr.setTestSchema(testSchema);
        return new ConfigEditorResult(OK, attr);
    }
}
