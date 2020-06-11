package uk.co.gresearch.siembol.configeditor.service.enrichments;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.gresearch.siembol.configeditor.common.ConfigEditorUtils;
import uk.co.gresearch.siembol.configeditor.common.ConfigSchemaService;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorAttributes;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult;
import uk.co.gresearch.siembol.enrichments.common.EnrichmentResult;
import uk.co.gresearch.siembol.enrichments.compiler.EnrichmentCompiler;
import uk.co.gresearch.siembol.enrichments.compiler.EnrichmentCompilerImpl;

import java.lang.invoke.MethodHandles;
import java.util.Optional;

public class EnrichmentSchemaServiceImpl implements ConfigSchemaService {
    private static final Logger LOG = LoggerFactory
            .getLogger(MethodHandles.lookup().lookupClass());
    private static final String INIT_ERROR = "Error during initialisation of enrichments rules and testing schema";

    private final EnrichmentCompiler compiler;
    private final String rulesSchema;
    private final String testSchema;

    EnrichmentSchemaServiceImpl(EnrichmentCompiler compiler, String rulesSchema, String testSchema) {
        this.compiler = compiler;
        this.rulesSchema = rulesSchema;
        this.testSchema = testSchema;
    }

    @Override
    public ConfigEditorResult getSchema() {
        ConfigEditorAttributes attributes = new ConfigEditorAttributes();
        attributes.setRulesSchema(rulesSchema);
        return new ConfigEditorResult(ConfigEditorResult.StatusCode.OK, attributes);
    }

    @Override
    public ConfigEditorResult getTestSchema() {
        ConfigEditorAttributes attributes = new ConfigEditorAttributes();
        attributes.setTestSchema(testSchema);
        return new ConfigEditorResult(ConfigEditorResult.StatusCode.OK, attributes);
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

    public static ConfigSchemaService createEnrichmentsSchemaService(Optional<String> rulesUiConfig,
                                                                     Optional<String> testUiConfig) throws Exception {
        LOG.info("Initialising enrichment config schema service");

        EnrichmentCompiler compilerResult = EnrichmentCompilerImpl.createEnrichmentsCompiler();
        String rulesSchema = compilerResult.getSchema().getAttributes().getRulesSchema();
        String testSchema = compilerResult.getTestSpecificationSchema().getAttributes().getTestSchema();

        Optional<String> rulesSchemaUi = rulesUiConfig.isPresent()
                ? ConfigEditorUtils.patchJsonSchema(rulesSchema, rulesUiConfig.get())
                : Optional.ofNullable(rulesSchema);

        Optional<String> testSchemaUi = testUiConfig.isPresent()
                ? ConfigEditorUtils.patchJsonSchema(testSchema, testUiConfig.get())
                : Optional.ofNullable(testSchema);

        if (!rulesSchemaUi.isPresent() || !testSchemaUi.isPresent()) {
            LOG.error(INIT_ERROR);
            throw new IllegalArgumentException(INIT_ERROR);
        }

        LOG.info("Initialising enrichment config schema service completed");
        return new EnrichmentSchemaServiceImpl(compilerResult, rulesSchemaUi.get(), testSchemaUi.get());
    }
}
