package uk.co.gresearch.nortem.configeditor.service.nikita;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.gresearch.nortem.common.jsonschema.NortemJsonSchemaValidator;
import uk.co.gresearch.nortem.configeditor.model.ConfigEditorAttributes;
import uk.co.gresearch.nortem.configeditor.model.ConfigEditorResult;
import uk.co.gresearch.nortem.configeditor.common.ConfigEditorUtils;
import uk.co.gresearch.nortem.configeditor.common.ConfigSchemaService;
import uk.co.gresearch.nortem.nikita.common.NikitaAttributes;
import uk.co.gresearch.nortem.nikita.common.NikitaResult;
import uk.co.gresearch.nortem.nikita.compiler.NikitaCompiler;
import uk.co.gresearch.nortem.nikita.compiler.NikitaCorrelationRulesCompiler;
import uk.co.gresearch.nortem.nikita.compiler.NikitaRulesCompiler;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.Optional;

import static uk.co.gresearch.nortem.configeditor.model.ConfigEditorResult.StatusCode.OK;

public class NikitaRuleSchemaServiceImpl implements ConfigSchemaService {
    private static final Logger LOG = LoggerFactory
            .getLogger(MethodHandles.lookup().lookupClass());

    private static final ObjectReader TEST_SPECIFICATION_READER = new ObjectMapper()
            .readerFor(NikitaTestSpecificationDto.class);

    private static final ObjectWriter NIKITA_ATTRIBUTES_WRITER = new ObjectMapper()
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .writerFor(NikitaAttributes.class)
            .with(SerializationFeature.INDENT_OUTPUT);

    private static final String SCHEMA_INIT_ERROR = "Error during computing rules schema";
    private static final String TESTING_ERROR = "Unexpected rule testing service result";
    private final NikitaCompiler nikitaCompiler;
    private final Optional<NortemJsonSchemaValidator> testSchemaValidator;
    private final Optional<String> testSchema;
    private final String schema;

    NikitaRuleSchemaServiceImpl(NikitaCompiler nikitaCompiler,
                                Optional<NortemJsonSchemaValidator> testSchemaValidator,
                                Optional<String> testSchema,
                                String schema) throws Exception {
        this.nikitaCompiler = nikitaCompiler;
        this.testSchemaValidator = testSchemaValidator;
        this.testSchema = testSchema;
        this.schema = schema;
    }

    @Override
    public ConfigEditorResult getSchema() {
        return ConfigEditorResult.fromSchema(schema);
    }

    @Override
    public ConfigEditorResult validateConfiguration(String rule) {
        NikitaResult nikitaResult = nikitaCompiler.validateRule(rule);
        return fromNikitaValidateResult(nikitaResult);
    }

    @Override
    public ConfigEditorResult validateConfigurations(String rules) {
        NikitaResult nikitaResult = nikitaCompiler.validateRules(rules);
        return fromNikitaValidateResult(nikitaResult);
    }

    public static ConfigSchemaService createNikitaRuleSchema(Optional<String> uiConfig,
                                                             Optional<String> testUiConfig) throws Exception {
        LOG.info("Initialising nikita rule schema service");
        NikitaCompiler compiler = NikitaRulesCompiler.createNikitaRulesCompiler();
        NikitaResult schemaResult = compiler.getSchema();

        if (schemaResult.getStatusCode() != NikitaResult.StatusCode.OK
                || schemaResult.getAttributes().getRulesSchema() == null
                || !uiConfig.isPresent()) {
            LOG.error(SCHEMA_INIT_ERROR);
            throw new IllegalStateException(SCHEMA_INIT_ERROR);
        }

        Optional<String> computedSchema = ConfigEditorUtils
                .computeRulesSchema(schemaResult.getAttributes().getRulesSchema(), uiConfig.get());

        if (!computedSchema.isPresent()) {
            LOG.error(SCHEMA_INIT_ERROR);
            throw new IllegalStateException(SCHEMA_INIT_ERROR);
        }
        NortemJsonSchemaValidator testValidator = new NortemJsonSchemaValidator(NikitaTestSpecificationDto.class);
        String testSchema = testValidator.getJsonSchema().getAttributes().getJsonSchema();

        Optional<String> testSchemaUi = testUiConfig.isPresent()
                ? ConfigEditorUtils.computeRulesSchema(testSchema, testUiConfig.get())
                : Optional.of(testSchema);
        LOG.info("Initialising nikita rule schema service completed");
        return new NikitaRuleSchemaServiceImpl(compiler, Optional.of(testValidator), testSchemaUi, computedSchema.get());
    }

    public static ConfigSchemaService createNikitaCorrelationRuleSchema(
            Optional<String> uiConfig) throws Exception {
        LOG.info("Initialising nikita correlation rule schema service");
        NikitaCompiler compiler = NikitaCorrelationRulesCompiler.createNikitaCorrelationRulesCompiler();
        NikitaResult schemaResult = compiler.getSchema();

        if (schemaResult.getStatusCode() != NikitaResult.StatusCode.OK
                || schemaResult.getAttributes().getRulesSchema() == null
                || !uiConfig.isPresent()) {
            LOG.error(SCHEMA_INIT_ERROR);
            throw new IllegalStateException(SCHEMA_INIT_ERROR);
        }

        Optional<String> computedSchema = ConfigEditorUtils
                .computeRulesSchema(schemaResult.getAttributes().getRulesSchema(), uiConfig.get());

        if (!computedSchema.isPresent()) {
            LOG.error(SCHEMA_INIT_ERROR);
            throw new IllegalStateException(SCHEMA_INIT_ERROR);
        }

        LOG.info("Initialising nikita correlation rule schema service completed");
        return new NikitaRuleSchemaServiceImpl(compiler, Optional.empty(), Optional.empty(), computedSchema.get());
    }

    @Override
    public ConfigEditorResult testConfiguration(String rule, String testSpecification) {
        NikitaTestSpecificationDto specificationDto;
        try {
            specificationDto = TEST_SPECIFICATION_READER.readValue(testSpecification);
        } catch (IOException e) {
            return ConfigEditorResult.fromException(e);
        }
        return fromNikitaTestResult(nikitaCompiler.testRule(rule, specificationDto.getEventContent()));
    }

    @Override
    public ConfigEditorResult testConfigurations(String rule, String testSpecification) {
        NikitaTestSpecificationDto specificationDto;
        try {
            specificationDto = TEST_SPECIFICATION_READER.readValue(testSpecification);
        } catch (IOException e) {
            return ConfigEditorResult.fromException(e);
        }
        return fromNikitaTestResult(nikitaCompiler.testRules(rule, specificationDto.getEventContent()));
    }

    private ConfigEditorResult fromNikitaValidateResult(NikitaResult nikitaResult) {
        ConfigEditorAttributes attr = new ConfigEditorAttributes();
        ConfigEditorResult.StatusCode statusCode = nikitaResult.getStatusCode() == NikitaResult.StatusCode.OK
                ? ConfigEditorResult.StatusCode.OK
                : ConfigEditorResult.StatusCode.ERROR;

        attr.setMessage(nikitaResult.getAttributes().getMessage());
        attr.setException(nikitaResult.getAttributes().getException());

        return new ConfigEditorResult(statusCode, attr);
    }

    private ConfigEditorResult fromNikitaTestResult(NikitaResult nikitaResult) {
        ConfigEditorAttributes attr = new ConfigEditorAttributes();
        if (nikitaResult.getStatusCode() != NikitaResult.StatusCode.OK) {
            attr.setMessage(nikitaResult.getAttributes().getMessage());
            attr.setException(nikitaResult.getAttributes().getException());
            return new ConfigEditorResult(ConfigEditorResult.StatusCode.ERROR, attr);
        }

        if (nikitaResult.getAttributes().getMessage() == null) {
            return ConfigEditorResult.fromMessage(ConfigEditorResult.StatusCode.ERROR,
                    TESTING_ERROR);
        }

        attr.setTestResultComplete(true);
        attr.setTestResultOutput(nikitaResult.getAttributes().getMessage());
        NikitaAttributes nikitaAttributes = new NikitaAttributes();
        nikitaAttributes.setOutputEvents(nikitaResult.getAttributes().getOutputEvents());
        nikitaAttributes.setExceptionEvents(nikitaResult.getAttributes().getExceptionEvents());
        try {
            String rawTestOutput = NIKITA_ATTRIBUTES_WRITER.writeValueAsString(nikitaAttributes);
            attr.setTestResultRawOutput(rawTestOutput);
        } catch (JsonProcessingException e) {
            return ConfigEditorResult.fromException(e);
        }

        return new ConfigEditorResult(ConfigEditorResult.StatusCode.OK, attr);
    }

    @Override
    public ConfigEditorResult getTestSchema() {
        if (!testSchema.isPresent()) {
            return ConfigEditorResult.fromMessage(ConfigEditorResult.StatusCode.ERROR, NOT_IMPLEMENTED_MSG);
        }
        ConfigEditorAttributes attr = new ConfigEditorAttributes();
        attr.setTestSchema(testSchema.get());
        return new ConfigEditorResult(OK, attr);
    }
}
