package uk.co.gresearch.siembol.configeditor.service.alerts;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.gresearch.siembol.alerts.storm.model.AlertingStormAttributesDto;
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
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorUiLayout;
import uk.co.gresearch.siembol.configeditor.service.common.ConfigSchemaServiceAbstract;
import uk.co.gresearch.siembol.configeditor.service.common.ConfigSchemaServiceContext;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.Optional;

public class AlertingRuleSchemaService extends ConfigSchemaServiceAbstract {
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

    AlertingRuleSchemaService(AlertingCompiler alertingCompiler,
                              ConfigSchemaServiceContext context) {
        super(context);
        this.alertingCompiler = alertingCompiler;
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

    public static ConfigSchemaService createAlertingRuleSchemaService(ConfigEditorUiLayout uiLayout) throws Exception {
        LOG.info("Initialising alerts rule schema service");
        ConfigSchemaServiceContext context = new ConfigSchemaServiceContext();
        AlertingCompiler compiler = AlertingRulesCompiler.createAlertingRulesCompiler();
        AlertingResult schemaResult = compiler.getSchema();

        if (schemaResult.getStatusCode() != AlertingResult.StatusCode.OK
                || schemaResult.getAttributes().getRulesSchema() == null
                || uiLayout == null) {
            LOG.error(SCHEMA_INIT_ERROR);
            throw new IllegalStateException(SCHEMA_INIT_ERROR);
        }

        Optional<String> computedSchema = ConfigEditorUtils
                .patchJsonSchema(schemaResult.getAttributes().getRulesSchema(), uiLayout.getConfigLayout());

        SiembolJsonSchemaValidator testValidator = new SiembolJsonSchemaValidator(AlertingTestSpecificationDto.class);
        String testSchema = testValidator.getJsonSchema().getAttributes().getJsonSchema();

        SiembolJsonSchemaValidator adminConfigValidator = new SiembolJsonSchemaValidator(AlertingStormAttributesDto.class);
        Optional<String> adminConfigSchemaUi = ConfigEditorUtils.patchJsonSchema(
                adminConfigValidator.getJsonSchema().getAttributes().getJsonSchema(),
                uiLayout.getAdminConfigLayout());

        Optional<String> testSchemaUi = ConfigEditorUtils.patchJsonSchema(testSchema, uiLayout.getTestLayout());

        if (!computedSchema.isPresent()
                || !adminConfigSchemaUi.isPresent()
                || !testSchemaUi.isPresent()) {
            LOG.error(SCHEMA_INIT_ERROR);
            throw new IllegalStateException(SCHEMA_INIT_ERROR);
        }
        context.setConfigSchema(computedSchema.get());
        context.setAdminConfigSchema(adminConfigSchemaUi.get());
        context.setAdminConfigValidator(adminConfigValidator);
        context.setTestSchema(testSchema);
        LOG.info("Initialising alerts rule schema service completed");
        return new AlertingRuleSchemaService(compiler, context);
    }

    public static ConfigSchemaService createAlertingCorrelationRuleSchemaService(
            ConfigEditorUiLayout uiLayout) throws Exception {
        LOG.info("Initialising alerts correlation rule schema service");
        ConfigSchemaServiceContext context = new ConfigSchemaServiceContext();
        AlertingCompiler compiler = AlertingCorrelationRulesCompiler.createAlertingCorrelationRulesCompiler();
        AlertingResult schemaResult = compiler.getSchema();

        if (schemaResult.getStatusCode() != AlertingResult.StatusCode.OK
                || schemaResult.getAttributes().getRulesSchema() == null
                || uiLayout == null) {
            LOG.error(SCHEMA_INIT_ERROR);
            throw new IllegalStateException(SCHEMA_INIT_ERROR);
        }

        Optional<String> computedSchema = ConfigEditorUtils
                .patchJsonSchema(schemaResult.getAttributes().getRulesSchema(), uiLayout.getConfigLayout());
        SiembolJsonSchemaValidator adminConfigValidator = new SiembolJsonSchemaValidator(AlertingStormAttributesDto.class);
        Optional<String> adminConfigSchemaUi = ConfigEditorUtils.patchJsonSchema(
                adminConfigValidator.getJsonSchema().getAttributes().getJsonSchema(),
                uiLayout.getAdminConfigLayout());

        if (!computedSchema.isPresent() || !adminConfigSchemaUi.isPresent()) {
            LOG.error(SCHEMA_INIT_ERROR);
            throw new IllegalStateException(SCHEMA_INIT_ERROR);
        }

        context.setConfigSchema(computedSchema.get());
        context.setAdminConfigSchema(adminConfigSchemaUi.get());
        context.setAdminConfigValidator(adminConfigValidator);

        LOG.info("Initialising alerts correlation rule schema service completed");
        return new AlertingRuleSchemaService(compiler, context);
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
}
