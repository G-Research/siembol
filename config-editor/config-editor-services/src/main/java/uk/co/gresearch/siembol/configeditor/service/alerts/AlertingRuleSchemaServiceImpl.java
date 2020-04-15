package uk.co.gresearch.siembol.configeditor.service.alerts;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.gresearch.siembol.common.jsonschema.SiembolJsonSchemaValidator;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorAttributes;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult;
import uk.co.gresearch.siembol.configeditor.common.ConfigEditorUtils;
import uk.co.gresearch.siembol.configeditor.common.ConfigSchemaService;
import uk.co.gresearch.siembol.alerts.common.AlertingAttributes;
import uk.co.gresearch.siembol.alerts.common.AlertingResult;
import uk.co.gresearch.siembol.alerts.compiler.AlertingCompiler;
import uk.co.gresearch.siembol.alerts.compiler.AlertingCorrelationRulesCompiler;
import uk.co.gresearch.siembol.alerts.compiler.AlertingRulesCompiler;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.Optional;

import static uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult.StatusCode.OK;

public class AlertingRuleSchemaServiceImpl implements ConfigSchemaService {
    private static final Logger LOG = LoggerFactory
            .getLogger(MethodHandles.lookup().lookupClass());

    private static final ObjectReader TEST_SPECIFICATION_READER = new ObjectMapper()
            .readerFor(AlertingTestSpecificationDto.class);

    private static final ObjectWriter ALERTING_ATTRIBUTES_WRITER = new ObjectMapper()
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .writerFor(AlertingAttributes.class)
            .with(SerializationFeature.INDENT_OUTPUT);

    private static final String SCHEMA_INIT_ERROR = "Error during computing rules schema";
    private static final String TESTING_ERROR = "Unexpected rule testing service result";
    private final AlertingCompiler alertingCompiler;
    private final Optional<SiembolJsonSchemaValidator> testSchemaValidator;
    private final Optional<String> testSchema;
    private final String schema;

    AlertingRuleSchemaServiceImpl(AlertingCompiler alertingCompiler,
                                  Optional<SiembolJsonSchemaValidator> testSchemaValidator,
                                  Optional<String> testSchema,
                                  String schema) throws Exception {
        this.alertingCompiler = alertingCompiler;
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
        AlertingResult alertingResult = alertingCompiler.validateRule(rule);
        return fromAlertingValidateResult(alertingResult);
    }

    @Override
    public ConfigEditorResult validateConfigurations(String rules) {
        AlertingResult alertingResult = alertingCompiler.validateRules(rules);
        return fromAlertingValidateResult(alertingResult);
    }

    public static ConfigSchemaService createAlertingRuleSchema(Optional<String> uiConfig,
                                                               Optional<String> testUiConfig) throws Exception {
        LOG.info("Initialising alerts rule schema service");
        AlertingCompiler compiler = AlertingRulesCompiler.createAlertingRulesCompiler();
        AlertingResult schemaResult = compiler.getSchema();

        if (schemaResult.getStatusCode() != AlertingResult.StatusCode.OK
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
        SiembolJsonSchemaValidator testValidator = new SiembolJsonSchemaValidator(AlertingTestSpecificationDto.class);
        String testSchema = testValidator.getJsonSchema().getAttributes().getJsonSchema();

        Optional<String> testSchemaUi = testUiConfig.isPresent()
                ? ConfigEditorUtils.computeRulesSchema(testSchema, testUiConfig.get())
                : Optional.of(testSchema);
        LOG.info("Initialising alerts rule schema service completed");
        return new AlertingRuleSchemaServiceImpl(compiler, Optional.of(testValidator), testSchemaUi, computedSchema.get());
    }

    public static ConfigSchemaService createAlertingCorrelationRuleSchema(
            Optional<String> uiConfig) throws Exception {
        LOG.info("Initialising alerts correlation rule schema service");
        AlertingCompiler compiler = AlertingCorrelationRulesCompiler.createAlertingCorrelationRulesCompiler();
        AlertingResult schemaResult = compiler.getSchema();

        if (schemaResult.getStatusCode() != AlertingResult.StatusCode.OK
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

        LOG.info("Initialising alerts correlation rule schema service completed");
        return new AlertingRuleSchemaServiceImpl(compiler, Optional.empty(), Optional.empty(), computedSchema.get());
    }

    @Override
    public ConfigEditorResult testConfiguration(String rule, String testSpecification) {
        AlertingTestSpecificationDto specificationDto;
        try {
            specificationDto = TEST_SPECIFICATION_READER.readValue(testSpecification);
        } catch (IOException e) {
            return ConfigEditorResult.fromException(e);
        }
        return fromAlertingTestResult(alertingCompiler.testRule(rule, specificationDto.getEventContent()));
    }

    @Override
    public ConfigEditorResult testConfigurations(String rule, String testSpecification) {
        AlertingTestSpecificationDto specificationDto;
        try {
            specificationDto = TEST_SPECIFICATION_READER.readValue(testSpecification);
        } catch (IOException e) {
            return ConfigEditorResult.fromException(e);
        }
        return fromAlertingTestResult(alertingCompiler.testRules(rule, specificationDto.getEventContent()));
    }

    private ConfigEditorResult fromAlertingValidateResult(AlertingResult alertingResult) {
        ConfigEditorAttributes attr = new ConfigEditorAttributes();
        ConfigEditorResult.StatusCode statusCode = alertingResult.getStatusCode() == AlertingResult.StatusCode.OK
                ? ConfigEditorResult.StatusCode.OK
                : ConfigEditorResult.StatusCode.ERROR;

        attr.setMessage(alertingResult.getAttributes().getMessage());
        attr.setException(alertingResult.getAttributes().getException());

        return new ConfigEditorResult(statusCode, attr);
    }

    private ConfigEditorResult fromAlertingTestResult(AlertingResult alertingResult) {
        ConfigEditorAttributes attr = new ConfigEditorAttributes();
        if (alertingResult.getStatusCode() != AlertingResult.StatusCode.OK) {
            attr.setMessage(alertingResult.getAttributes().getMessage());
            attr.setException(alertingResult.getAttributes().getException());
            return new ConfigEditorResult(ConfigEditorResult.StatusCode.ERROR, attr);
        }

        if (alertingResult.getAttributes().getMessage() == null) {
            return ConfigEditorResult.fromMessage(ConfigEditorResult.StatusCode.ERROR,
                    TESTING_ERROR);
        }

        attr.setTestResultComplete(true);
        attr.setTestResultOutput(alertingResult.getAttributes().getMessage());
        AlertingAttributes alertingAttributes = new AlertingAttributes();
        alertingAttributes.setOutputEvents(alertingResult.getAttributes().getOutputEvents());
        alertingAttributes.setExceptionEvents(alertingResult.getAttributes().getExceptionEvents());
        try {
            String rawTestOutput = ALERTING_ATTRIBUTES_WRITER.writeValueAsString(alertingAttributes);
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
