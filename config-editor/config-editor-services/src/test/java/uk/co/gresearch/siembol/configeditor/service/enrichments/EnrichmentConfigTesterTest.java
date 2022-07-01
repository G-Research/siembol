package uk.co.gresearch.siembol.configeditor.service.enrichments;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import uk.co.gresearch.siembol.common.jsonschema.SiembolJsonSchemaValidator;
import uk.co.gresearch.siembol.configeditor.common.ConfigTesterFlag;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult;
import uk.co.gresearch.siembol.enrichments.common.EnrichmentAttributes;
import uk.co.gresearch.siembol.enrichments.common.EnrichmentResult;
import uk.co.gresearch.siembol.enrichments.compiler.EnrichmentCompiler;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.internal.verification.VerificationModeFactory.times;
import static uk.co.gresearch.siembol.enrichments.common.EnrichmentResult.StatusCode.ERROR;
import static uk.co.gresearch.siembol.enrichments.common.EnrichmentResult.StatusCode.OK;

public class EnrichmentConfigTesterTest {
    private final String schema = "dummy schema";
   
    private final String testRule = "dummy enrichments config";
    private final String testSpecification = "dummy test specification";
    private final String testRules = "dummy enrichments configs";
    private final String testResult = "dummy test result";
    private final String testRawResult = "dummy test raw result";

    private EnrichmentCompiler compiler;
    private EnrichmentConfigTester configTester;

    private EnrichmentAttributes enrichmentAttributes;
    private EnrichmentResult enrichmentResult;
    private SiembolJsonSchemaValidator testValidator;

    @Before
    public void setUp() {
        compiler = Mockito.mock(EnrichmentCompiler.class);
        testValidator = Mockito.mock(SiembolJsonSchemaValidator.class);

        enrichmentAttributes = new EnrichmentAttributes();
        enrichmentResult = new EnrichmentResult(OK, enrichmentAttributes);
        Mockito.when(compiler.compile(anyString())).thenReturn(enrichmentResult);

        Mockito.when(compiler.testConfiguration(anyString(), anyString())).thenReturn(enrichmentResult);
        Mockito.when(compiler.testConfigurations(anyString(), anyString())).thenReturn(enrichmentResult);
        configTester = new EnrichmentConfigTester(testValidator, schema, compiler);
    }

    @Test
    public void testRuleOK() {
        enrichmentAttributes.setTestResult(testResult);
        enrichmentAttributes.setTestRawResult(testRawResult);
        ConfigEditorResult ret = configTester.testConfiguration(testRule, testSpecification);
        verify(compiler, times(1)).testConfiguration(testRule, testSpecification);
        Assert.assertEquals(ConfigEditorResult.StatusCode.OK, ret.getStatusCode());
        Assert.assertEquals(testResult, ret.getAttributes().getTestResultOutput());
        Assert.assertEquals(testResult, ret.getAttributes().getTestResultOutput());
        Assert.assertEquals(testRawResult, ret.getAttributes().getTestResultRawOutput());
    }


    @Test
    public void testRulesError() {
        enrichmentAttributes.setMessage("error");
        enrichmentResult = new EnrichmentResult(ERROR, enrichmentAttributes);

        Mockito.when(compiler.testConfigurations(anyString(), anyString())).thenReturn(enrichmentResult);
        ConfigEditorResult ret = configTester.testConfigurations(testRules, testSpecification);
        verify(compiler, times(1)).testConfigurations(testRules, testSpecification);
        Assert.assertEquals(ConfigEditorResult.StatusCode.BAD_REQUEST, ret.getStatusCode());
        Assert.assertEquals("error", ret.getAttributes().getMessage());
    }

    @Test
    public void testRuleError() {
        enrichmentAttributes.setMessage("error");
        enrichmentResult = new EnrichmentResult(ERROR, enrichmentAttributes);

        Mockito.when(compiler.testConfiguration(anyString(), anyString())).thenReturn(enrichmentResult);
        ConfigEditorResult ret = configTester.testConfiguration(testRules, testSpecification);
        verify(compiler, times(1)).testConfiguration(testRules, testSpecification);
        Assert.assertEquals(ConfigEditorResult.StatusCode.BAD_REQUEST, ret.getStatusCode());
        Assert.assertEquals("error", ret.getAttributes().getMessage());
    }

    @Test
    public void getFlags() {
        var flags = configTester.getFlags();
        Assert.assertTrue(flags.contains(ConfigTesterFlag.CONFIG_TESTING));
        Assert.assertTrue(flags.contains(ConfigTesterFlag.TEST_CASE_TESTING));
        Assert.assertFalse(flags.contains(ConfigTesterFlag.RELEASE_TESTING));
    }

    @Test
    public void getTestSpecificationSchemaOK() {
        ConfigEditorResult ret = configTester.getTestSpecificationSchema();
        Assert.assertEquals(ConfigEditorResult.StatusCode.OK, ret.getStatusCode());
        Assert.assertNotNull(ret.getAttributes().getTestSchema());
        Assert.assertEquals(schema, ret.getAttributes().getTestSchema());
    }
}
