package uk.co.gresearch.siembol.configeditor.service.alerts;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.gresearch.siembol.common.model.AlertingStormAttributesDto;
import uk.co.gresearch.siembol.common.jsonschema.SiembolJsonSchemaValidator;
import uk.co.gresearch.siembol.common.model.testing.AlertingSparkTestingSpecificationDto;
import uk.co.gresearch.siembol.common.model.testing.AlertingTestSpecificationDto;
import uk.co.gresearch.siembol.common.utils.HttpProvider;
import uk.co.gresearch.siembol.configeditor.common.ConfigImporter;
import uk.co.gresearch.siembol.configeditor.common.ConfigTester;
import uk.co.gresearch.siembol.configeditor.model.*;
import uk.co.gresearch.siembol.configeditor.common.ConfigEditorUtils;
import uk.co.gresearch.siembol.configeditor.common.ConfigSchemaService;
import uk.co.gresearch.siembol.alerts.common.AlertingAttributes;
import uk.co.gresearch.siembol.alerts.common.AlertingResult;
import uk.co.gresearch.siembol.alerts.compiler.AlertingCompiler;
import uk.co.gresearch.siembol.alerts.compiler.AlertingCorrelationRulesCompiler;
import uk.co.gresearch.siembol.alerts.compiler.AlertingRulesCompiler;
import uk.co.gresearch.siembol.configeditor.service.alerts.sigma.SigmaRuleImporter;
import uk.co.gresearch.siembol.configeditor.service.alerts.spark.AlertingSparkConfigTester;
import uk.co.gresearch.siembol.configeditor.service.alerts.spark.AlertingSparkTestingProvider;
import uk.co.gresearch.siembol.configeditor.service.common.ConfigSchemaServiceAbstract;
import uk.co.gresearch.siembol.configeditor.service.common.ConfigSchemaServiceContext;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.*;

import static uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult.StatusCode.OK;

public class AlertingRuleSchemaService extends ConfigSchemaServiceAbstract {
    private static final Logger LOG = LoggerFactory
            .getLogger(MethodHandles.lookup().lookupClass());
    private static final ObjectReader ADMIN_CONFIG_READER = new ObjectMapper()
            .readerFor(AlertingStormAttributesDto.class);

    private static final String SCHEMA_INIT_ERROR = "Error during computing rules schema";
    private static final String SIGMA_IMPORTER_NAME = "sigma";
    private static final String MISSING_SPARK_HDFS_TESTER_PROPS = "Missing spark hdfs additional tester properties";
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

    public static ConfigSchemaService createAlertingRuleSchemaService(
            ConfigEditorUiLayout uiLayout,
            Optional<AdditionalConfigTesters> additionalConfigTesters) throws Exception {
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

        Map<String, ConfigImporter> importerMap = new HashMap<>();
        importerMap.put(SIGMA_IMPORTER_NAME, new SigmaRuleImporter.Builder().configEditorUiLayout(uiLayout).build());
        context.setConfigImporters(importerMap);

        var configTestersList = new ArrayList<ConfigTester>();
        configTestersList.add(new AlertingConfigTester(testValidator, testSchemaUi.get(), compiler).withErrorMessage());
        if (additionalConfigTesters.isPresent() && additionalConfigTesters.get().getSparkHdfs() != null) {
            configTestersList.add(getSparkHdfsConfigTester(additionalConfigTesters.get().getSparkHdfs())
                    .withErrorMessage());
        }
        context.setConfigTesters(configTestersList);

        LOG.info("Initialising alerts rule schema service completed");
        return new AlertingRuleSchemaService(compiler, context);
    }

    private static ConfigTester getSparkHdfsConfigTester(
            SparkHdfsTesterProperties sparkHdfsProperties) throws Exception {
        if (sparkHdfsProperties.getUrl() == null
                || sparkHdfsProperties.getFileExtension() == null
                || sparkHdfsProperties.getFolderPath() == null
                || sparkHdfsProperties.getAttributes() == null) {
            throw new IllegalArgumentException(MISSING_SPARK_HDFS_TESTER_PROPS);
        }
        HttpProvider httpProvider = new HttpProvider(
                sparkHdfsProperties.getUrl(),
                HttpProvider::getHttpClient);
        AlertingSparkTestingProvider sparkTestingProvider = new AlertingSparkTestingProvider(
                httpProvider,
                sparkHdfsProperties.getAttributes());

        SiembolJsonSchemaValidator testValidator = new SiembolJsonSchemaValidator(
                AlertingSparkTestingSpecificationDto.class);
        String testSchema = testValidator.getJsonSchema().getAttributes().getJsonSchema();

        return new AlertingSparkConfigTester(testValidator, testSchema, sparkTestingProvider, sparkHdfsProperties);
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
    public ConfigEditorResult getAdminConfigTopologyName(String configuration) {
        try {
            AlertingStormAttributesDto adminConfig = ADMIN_CONFIG_READER.readValue(configuration);
            ConfigEditorAttributes attributes = new ConfigEditorAttributes();
            attributes.setTopologyName(adminConfig.getTopologyName());
            return new ConfigEditorResult(OK, attributes);
        } catch (IOException e) {
            return ConfigEditorResult.fromException(e);
        }
    }

    private ConfigEditorResult fromAlertingValidateResult(AlertingResult alertingResult) {
        ConfigEditorAttributes attr = new ConfigEditorAttributes();
        ConfigEditorResult.StatusCode statusCode = alertingResult.getStatusCode() == AlertingResult.StatusCode.OK
                ? OK
                : ConfigEditorResult.StatusCode.BAD_REQUEST;

        attr.setMessage(alertingResult.getAttributes().getMessage());
        attr.setException(alertingResult.getAttributes().getException());

        return new ConfigEditorResult(statusCode, attr);
    }
}
