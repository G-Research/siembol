package uk.co.gresearch.siembol.configeditor.service.enrichments;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.gresearch.siembol.common.jsonschema.SiembolJsonSchemaValidator;
import uk.co.gresearch.siembol.configeditor.common.ConfigEditorUtils;
import uk.co.gresearch.siembol.configeditor.common.ConfigSchemaService;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorAttributes;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorUiLayout;
import uk.co.gresearch.siembol.configeditor.service.common.ConfigSchemaServiceAbstract;
import uk.co.gresearch.siembol.configeditor.service.common.ConfigSchemaServiceContext;
import uk.co.gresearch.siembol.enrichments.common.EnrichmentResult;
import uk.co.gresearch.siembol.enrichments.compiler.EnrichmentCompiler;
import uk.co.gresearch.siembol.enrichments.compiler.EnrichmentCompilerImpl;
import uk.co.gresearch.siembol.common.model.StormEnrichmentAttributesDto;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.Optional;

import static uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult.StatusCode.OK;

public class EnrichmentSchemaService extends ConfigSchemaServiceAbstract {
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final String INIT_ERROR = "Error during initialisation of enrichment rules and testing schema";
    private static final ObjectReader ADMIN_CONFIG_READER = new ObjectMapper()
            .readerFor(StormEnrichmentAttributesDto.class);

    private final EnrichmentCompiler compiler;

    EnrichmentSchemaService(EnrichmentCompiler compiler, ConfigSchemaServiceContext context) {
        super(context);
        this.compiler = compiler;
    }

    @Override
    public ConfigEditorResult validateConfiguration(String configuration) {
        return fromEnrichmentResult(compiler.validateConfiguration(configuration));
    }

    @Override
    public ConfigEditorResult validateConfigurations(String configurations) {
        return fromEnrichmentResult(compiler.validateConfigurations(configurations));
    }

    @Override
    public ConfigEditorResult testConfiguration(String configuration, String testSpecification) {
        ConfigEditorResult ret = fromEnrichmentResult(compiler.testConfiguration(configuration, testSpecification));
        if (ret.getStatusCode() == ConfigEditorResult.StatusCode.ERROR) {
            return ret;
        }

        ret.getAttributes().setTestResultComplete(true);
        return ret;
    }

    @Override
    public ConfigEditorResult testConfigurations(String configurations, String testSpecification) {
        ConfigEditorResult ret = fromEnrichmentResult(compiler.testConfigurations(configurations, testSpecification));
        if (ret.getStatusCode() == ConfigEditorResult.StatusCode.ERROR) {
            return ret;
        }

        ret.getAttributes().setTestResultComplete(true);
        return ret;
    }

    @Override
    public ConfigEditorResult getAdminConfigTopologyName(String configuration) {
        try {
            StormEnrichmentAttributesDto adminConfig = ADMIN_CONFIG_READER.readValue(configuration);
            ConfigEditorAttributes attributes = new ConfigEditorAttributes();
            attributes.setTopologyName(adminConfig.getTopologyName());
            return new ConfigEditorResult(OK, attributes);
        } catch (IOException e) {
            return ConfigEditorResult.fromException(e);
        }
    }

    private ConfigEditorResult fromEnrichmentResult(EnrichmentResult enrichmentResultresult) {
        ConfigEditorAttributes attr = new ConfigEditorAttributes();
        attr.setMessage(enrichmentResultresult.getAttributes().getMessage());
        attr.setTestResultRawOutput(enrichmentResultresult.getAttributes().getTestRawResult());
        attr.setTestResultOutput(enrichmentResultresult.getAttributes().getTestResult());

        ConfigEditorResult.StatusCode statusCode =
                enrichmentResultresult.getStatusCode() == EnrichmentResult.StatusCode.OK
                        ? ConfigEditorResult.StatusCode.OK
                        : ConfigEditorResult.StatusCode.ERROR;
        return new ConfigEditorResult(statusCode, attr);
    }

    public static ConfigSchemaService createEnrichmentsSchemaService(ConfigEditorUiLayout uiLayout) throws Exception {
        LOG.info("Initialising enrichment config schema service");
        ConfigSchemaServiceContext context = new ConfigSchemaServiceContext();
        EnrichmentCompiler compilerResult = EnrichmentCompilerImpl.createEnrichmentsCompiler();
        String rulesSchema = compilerResult.getSchema().getAttributes().getRulesSchema();
        String testSchema = compilerResult.getTestSpecificationSchema().getAttributes().getTestSchema();

        Optional<String> rulesSchemaUi = ConfigEditorUtils.patchJsonSchema(rulesSchema, uiLayout.getConfigLayout());
        Optional<String> testSchemaUi = ConfigEditorUtils.patchJsonSchema(testSchema, uiLayout.getTestLayout());
        SiembolJsonSchemaValidator adminConfigValidator = new SiembolJsonSchemaValidator(
                StormEnrichmentAttributesDto.class);
        Optional<String> adminConfigSchemaUi = ConfigEditorUtils.patchJsonSchema(
                adminConfigValidator.getJsonSchema().getAttributes().getJsonSchema(),
                uiLayout.getAdminConfigLayout());

        if (!rulesSchemaUi.isPresent()
                || !testSchemaUi.isPresent()
                || !adminConfigSchemaUi.isPresent()) {
            LOG.error(INIT_ERROR);
            throw new IllegalArgumentException(INIT_ERROR);
        }

        context.setConfigSchema(rulesSchemaUi.get());
        context.setTestSchema(testSchemaUi.get());
        context.setAdminConfigSchema(adminConfigSchemaUi.get());
        context.setAdminConfigValidator(adminConfigValidator);
        LOG.info("Initialising enrichment config schema service completed");
        return new EnrichmentSchemaService(compilerResult, context);
    }
}