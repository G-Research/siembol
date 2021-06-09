package uk.co.gresearch.siembol.configeditor.service.enrichments;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.internal.verification.VerificationModeFactory;
import uk.co.gresearch.siembol.common.jsonschema.SiembolJsonSchemaValidator;
import uk.co.gresearch.siembol.common.result.SiembolAttributes;
import uk.co.gresearch.siembol.common.result.SiembolResult;
import uk.co.gresearch.siembol.configeditor.common.ConfigSchemaService;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorUiLayout;
import uk.co.gresearch.siembol.configeditor.service.common.ConfigSchemaServiceContext;
import uk.co.gresearch.siembol.enrichments.common.EnrichmentAttributes;
import uk.co.gresearch.siembol.enrichments.common.EnrichmentResult;
import uk.co.gresearch.siembol.enrichments.compiler.EnrichmentCompiler;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.co.gresearch.siembol.enrichments.common.EnrichmentResult.StatusCode.ERROR;
import static uk.co.gresearch.siembol.enrichments.common.EnrichmentResult.StatusCode.OK;

public class EnrichmentSchemaServiceTest {
    private EnrichmentSchemaService enrichmentsSchemaService;
    private final String schema = "dummmy schema";
    private final String adminSchema = "dummmy admin schema";
    private final String testConfig = "dummmy enrichments config";
    private final String testSpecification = "dummmy test specification";
    private final String testConfigs = "dummmy enrichments configs";
    private final String testResult = "dummy test result";
    private final String testRawResult = "dummy test raw result";

    private EnrichmentCompiler compiler;

    private EnrichmentAttributes enrichmentAttributes;
    private EnrichmentResult enrichmentResult;
    private ConfigSchemaServiceContext context;
    private SiembolJsonSchemaValidator adminConfigValidator;
    private SiembolResult validationResult;

    @Before
    public void setup() {
        compiler = Mockito.mock(EnrichmentCompiler.class);
        adminConfigValidator = Mockito.mock(SiembolJsonSchemaValidator.class);
        validationResult = new SiembolResult(SiembolResult.StatusCode.OK, new SiembolAttributes());
        when(adminConfigValidator.validate(eq(testConfig))).thenReturn(validationResult);

        context = new ConfigSchemaServiceContext();
        context.setConfigSchema(schema);
        context.setTestSchema(schema);
        context.setAdminConfigSchema(adminSchema);
        context.setAdminConfigValidator(adminConfigValidator);

        this.enrichmentsSchemaService = new EnrichmentSchemaService(compiler, context);
        enrichmentAttributes = new EnrichmentAttributes();
        enrichmentResult = new EnrichmentResult(OK, enrichmentAttributes);
        Mockito.when(compiler.compile(anyString())).thenReturn(enrichmentResult);

        Mockito.when(compiler.validateConfiguration(anyString())).thenReturn(enrichmentResult);
        Mockito.when(compiler.validateConfigurations(anyString())).thenReturn(enrichmentResult);
        Mockito.when(compiler.testConfiguration(anyString(), anyString())).thenReturn(enrichmentResult);
        Mockito.when(compiler.testConfigurations(anyString(), anyString())).thenReturn(enrichmentResult);
    }

    @Test
    public void getSchemaOK() {
        ConfigEditorResult ret = enrichmentsSchemaService.getSchema();
        Assert.assertEquals(ConfigEditorResult.StatusCode.OK, ret.getStatusCode());
        Assert.assertEquals(schema, ret.getAttributes().getRulesSchema());
    }

    @Test
    public void getTestSchemaOK() {
        ConfigEditorResult ret = enrichmentsSchemaService.getTestSchema();
        Assert.assertEquals(ConfigEditorResult.StatusCode.OK, ret.getStatusCode());
        Assert.assertNotNull(ret.getAttributes().getTestSchema());
    }

    @Test
    public void validateConfigurationsOK() {
        ConfigEditorResult ret = enrichmentsSchemaService.validateConfigurations(testConfigs);
        Mockito.verify(compiler, times(1)).validateConfigurations(testConfigs);
        Assert.assertEquals(ConfigEditorResult.StatusCode.OK, ret.getStatusCode());
    }

    @Test
    public void validateConfigurationOK() {
        ConfigEditorResult ret = enrichmentsSchemaService.validateConfiguration(testConfig);
        Mockito.verify(compiler, times(1)).validateConfiguration(testConfig);
        Assert.assertEquals(ConfigEditorResult.StatusCode.OK, ret.getStatusCode());
    }

    @Test
    public void validateConfigurationsError() {
        enrichmentAttributes.setMessage("error");
        enrichmentResult = new EnrichmentResult(ERROR, enrichmentAttributes);
        Mockito.when(compiler.validateConfigurations(anyString())).thenReturn(enrichmentResult);
        ConfigEditorResult ret = enrichmentsSchemaService.validateConfigurations(testConfigs);
        Mockito.verify(compiler, times(1)).validateConfigurations(testConfigs);
        Assert.assertEquals(ConfigEditorResult.StatusCode.ERROR, ret.getStatusCode());
        Assert.assertEquals("error", ret.getAttributes().getMessage());
    }

    @Test
    public void validateConfigurationError() {
        enrichmentAttributes.setMessage("error");
        enrichmentResult = new EnrichmentResult(ERROR, enrichmentAttributes);
        Mockito.when(compiler.validateConfiguration(anyString())).thenReturn(enrichmentResult);
        ConfigEditorResult ret = enrichmentsSchemaService.validateConfiguration(testConfigs);
        Mockito.verify(compiler, times(1)).validateConfiguration(testConfigs);
        Assert.assertEquals(ConfigEditorResult.StatusCode.ERROR, ret.getStatusCode());
        Assert.assertEquals("error", ret.getAttributes().getMessage());
    }

    @Test
    public void testConfigurationOK() {
        enrichmentAttributes.setTestResult(testResult);
        enrichmentAttributes.setTestRawResult(testRawResult);
        Mockito.when(compiler.testConfiguration(anyString(), anyString())).thenReturn(enrichmentResult);
        ConfigEditorResult ret = enrichmentsSchemaService.testConfiguration(testConfig, testSpecification);
        Mockito.verify(compiler, times(1)).testConfiguration(testConfig, testSpecification);
        Assert.assertEquals(ConfigEditorResult.StatusCode.OK, ret.getStatusCode());
        Assert.assertTrue(ret.getAttributes().getTestResultComplete());
        Assert.assertEquals(ConfigEditorResult.StatusCode.OK, ret.getStatusCode());
        Assert.assertEquals(testResult, ret.getAttributes().getTestResultOutput());
        Assert.assertEquals(testRawResult, ret.getAttributes().getTestResultRawOutput());
    }

    @Test
    public void testConfigurationsOK() {
        enrichmentAttributes.setTestResult(testResult);
        enrichmentAttributes.setTestRawResult(testRawResult);
        Mockito.when(compiler.testConfiguration(anyString(), anyString())).thenReturn(enrichmentResult);
        ConfigEditorResult ret = enrichmentsSchemaService.testConfigurations(testConfigs, testSpecification);
        Mockito.verify(compiler, times(1)).testConfigurations(testConfigs, testSpecification);
        Assert.assertEquals(ConfigEditorResult.StatusCode.OK, ret.getStatusCode());
        Assert.assertTrue(ret.getAttributes().getTestResultComplete());
        Assert.assertEquals(ConfigEditorResult.StatusCode.OK, ret.getStatusCode());
        Assert.assertEquals(testResult, ret.getAttributes().getTestResultOutput());
        Assert.assertEquals(testRawResult, ret.getAttributes().getTestResultRawOutput());
    }

    @Test
    public void testConfigurationError() {
        enrichmentAttributes.setMessage("error");
        enrichmentResult = new EnrichmentResult(ERROR, enrichmentAttributes);
        Mockito.when(compiler.testConfiguration(anyString(), anyString())).thenReturn(enrichmentResult);
        ConfigEditorResult ret = enrichmentsSchemaService.testConfiguration(testConfig, testSpecification);
        Mockito.verify(compiler, times(1)).testConfiguration(testConfig, testSpecification);
        Assert.assertEquals(ConfigEditorResult.StatusCode.ERROR, ret.getStatusCode());
        Assert.assertEquals("error", ret.getAttributes().getMessage());
    }

    @Test
    public void testConfigurationsError() {
        enrichmentAttributes.setMessage("error");
        enrichmentResult = new EnrichmentResult(ERROR, enrichmentAttributes);
        Mockito.when(compiler.testConfigurations(anyString(), anyString())).thenReturn(enrichmentResult);
        ConfigEditorResult ret = enrichmentsSchemaService.testConfigurations(testConfigs, testSpecification);
        Mockito.verify(compiler, times(1)).testConfigurations(testConfigs, testSpecification);
        Assert.assertEquals(ConfigEditorResult.StatusCode.ERROR, ret.getStatusCode());
        Assert.assertEquals("error", ret.getAttributes().getMessage());
    }

    @Test
    public void createEnrichmentSchemaServiceEmptyUiConfig() throws Exception {
        ConfigSchemaService service = EnrichmentSchemaService
                .createEnrichmentsSchemaService(new ConfigEditorUiLayout());
        Assert.assertNotNull(service);
    }

    @Test
    public void getAdminConfigSchemaOK() {
        ConfigEditorResult ret = enrichmentsSchemaService.getAdminConfigurationSchema();
        Assert.assertEquals(ConfigEditorResult.StatusCode.OK, ret.getStatusCode());
        Assert.assertEquals(adminSchema, ret.getAttributes().getAdminConfigSchema());
    }

    @Test
    public void validateAdminConfigOK() {
        ConfigEditorResult ret = enrichmentsSchemaService.validateAdminConfiguration(testConfig);
        Assert.assertEquals(ConfigEditorResult.StatusCode.OK, ret.getStatusCode());
        verify(adminConfigValidator, VerificationModeFactory.times(1)).validate(testConfig);
    }

    @Test
    public void validateAdminConfigInvalid() {
        when(adminConfigValidator.validate(eq(testConfig)))
                .thenReturn(new SiembolResult(SiembolResult.StatusCode.ERROR, new SiembolAttributes()));
        ConfigEditorResult ret = enrichmentsSchemaService.validateAdminConfiguration(testConfig);
        Assert.assertEquals(ConfigEditorResult.StatusCode.BAD_REQUEST, ret.getStatusCode());
        verify(adminConfigValidator, VerificationModeFactory.times(1)).validate(testConfig);
    }

    @Test
    public void getImportersEmpty() {
        ConfigEditorResult ret = enrichmentsSchemaService.getImporters();
        Assert.assertEquals(ConfigEditorResult.StatusCode.OK, ret.getStatusCode());
        Assert.assertNotNull(ret.getAttributes().getConfigImporters());
        Assert.assertTrue(ret.getAttributes().getConfigImporters().isEmpty());
    }
}
