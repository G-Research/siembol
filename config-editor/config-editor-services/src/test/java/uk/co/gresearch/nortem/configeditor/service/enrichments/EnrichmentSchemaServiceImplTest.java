package uk.co.gresearch.nortem.configeditor.service.enrichments;

import com.fasterxml.jackson.core.JsonParseException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import uk.co.gresearch.nortem.configeditor.common.ConfigSchemaService;
import uk.co.gresearch.nortem.configeditor.model.ConfigEditorResult;
import uk.co.gresearch.nortem.enrichments.common.EnrichmentAttributes;
import uk.co.gresearch.nortem.enrichments.common.EnrichmentResult;
import uk.co.gresearch.nortem.enrichments.compiler.EnrichmentCompiler;

import java.util.Optional;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static uk.co.gresearch.nortem.enrichments.common.EnrichmentResult.StatusCode.ERROR;
import static uk.co.gresearch.nortem.enrichments.common.EnrichmentResult.StatusCode.OK;

public class EnrichmentSchemaServiceImplTest {
    private EnrichmentSchemaServiceImpl enrichmentsSchemaService;
    private final String schema = "dummmy schema";
    private final String testSchema = "dummmy test schema";
    private final String testConfig = "dummmy enrichments config";
    private final String testSpecification = "dummmy test specification";
    private final String testConfigs = "dummmy enrichments configs";
    private final String testLog = "dummy log";
    private final String testResult = "dummy test result";
    private final String testRawResult = "dummy test raw result";

    private EnrichmentCompiler compiler;

    private EnrichmentAttributes enrichmentAttributes;
    private EnrichmentResult enrichmentResult;

    @Before
    public void setup() throws Exception {
        compiler = Mockito.mock(EnrichmentCompiler.class);
        this.enrichmentsSchemaService = new EnrichmentSchemaServiceImpl(compiler, schema, testSchema);
        enrichmentAttributes = new EnrichmentAttributes();
        enrichmentResult = new EnrichmentResult(OK, enrichmentAttributes);
        Mockito.when(compiler.compile(any())).thenReturn(enrichmentResult);

        Mockito.when(compiler.validateConfiguration(any())).thenReturn(enrichmentResult);
        Mockito.when(compiler.validateConfigurations(any())).thenReturn(enrichmentResult);
        Mockito.when(compiler.testConfiguration(any(), any())).thenReturn(enrichmentResult);
        Mockito.when(compiler.testConfigurations(any(), any())).thenReturn(enrichmentResult);
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
        Mockito.when(compiler.validateConfigurations(any())).thenReturn(enrichmentResult);
        ConfigEditorResult ret = enrichmentsSchemaService.validateConfigurations(testConfigs);
        Mockito.verify(compiler, times(1)).validateConfigurations(testConfigs);
        Assert.assertEquals(ConfigEditorResult.StatusCode.ERROR, ret.getStatusCode());
        Assert.assertEquals("error", ret.getAttributes().getMessage());
    }

    @Test
    public void validateConfigurationError() {
        enrichmentAttributes.setMessage("error");
        enrichmentResult = new EnrichmentResult(ERROR, enrichmentAttributes);
        Mockito.when(compiler.validateConfiguration(any())).thenReturn(enrichmentResult);
        ConfigEditorResult ret = enrichmentsSchemaService.validateConfiguration(testConfigs);
        Mockito.verify(compiler, times(1)).validateConfiguration(testConfigs);
        Assert.assertEquals(ConfigEditorResult.StatusCode.ERROR, ret.getStatusCode());
        Assert.assertEquals("error", ret.getAttributes().getMessage());
    }

    @Test
    public void testConfigurationOK() {
        enrichmentAttributes.setTestResult(testResult);
        enrichmentAttributes.setTestRawResult(testRawResult);
        Mockito.when(compiler.testConfiguration(any(), any())).thenReturn(enrichmentResult);
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
        Mockito.when(compiler.testConfiguration(any(), any())).thenReturn(enrichmentResult);
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
        Mockito.when(compiler.testConfiguration(any(), any())).thenReturn(enrichmentResult);
        ConfigEditorResult ret = enrichmentsSchemaService.testConfiguration(testConfig, testSpecification);
        Mockito.verify(compiler, times(1)).testConfiguration(testConfig, testSpecification);
        Assert.assertEquals(ConfigEditorResult.StatusCode.ERROR, ret.getStatusCode());
        Assert.assertEquals("error", ret.getAttributes().getMessage());
    }

    @Test
    public void testConfigurationsError() {
        enrichmentAttributes.setMessage("error");
        enrichmentResult = new EnrichmentResult(ERROR, enrichmentAttributes);
        Mockito.when(compiler.testConfigurations(any(), any())).thenReturn(enrichmentResult);
        ConfigEditorResult ret = enrichmentsSchemaService.testConfigurations(testConfigs, testSpecification);
        Mockito.verify(compiler, times(1)).testConfigurations(testConfigs, testSpecification);
        Assert.assertEquals(ConfigEditorResult.StatusCode.ERROR, ret.getStatusCode());
        Assert.assertEquals("error", ret.getAttributes().getMessage());
    }

    @Test
    public void createEnrichmentSchemaServiceEmptyUiConfig() throws Exception {
        ConfigSchemaService service = EnrichmentSchemaServiceImpl
                .createEnrichmentsSchemaService(Optional.empty(), Optional.empty());
        Assert.assertNotNull(service);
    }

    @Test(expected = JsonParseException.class)
    public void createEnrichmentSchemaServiceWrongUiConfig() throws Exception {
        ConfigSchemaService service = EnrichmentSchemaServiceImpl
                .createEnrichmentsSchemaService(Optional.of("invalid"), Optional.empty());
    }
}
