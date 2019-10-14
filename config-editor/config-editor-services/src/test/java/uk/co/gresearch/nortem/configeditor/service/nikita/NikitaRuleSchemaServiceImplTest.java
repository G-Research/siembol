package uk.co.gresearch.nortem.configeditor.service.nikita;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import uk.co.gresearch.nortem.configeditor.model.ConfigEditorResult;
import uk.co.gresearch.nortem.nikita.common.NikitaAttributes;
import uk.co.gresearch.nortem.nikita.common.NikitaResult;
import uk.co.gresearch.nortem.nikita.compiler.NikitaCompiler;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.internal.verification.VerificationModeFactory.times;


public class NikitaRuleSchemaServiceImplTest {
    private NikitaRuleSchemaServiceImpl nikitaRuleSchemaService;
    private final String ruleSchema = "dummmy schema";
    private final String testRule = "dummmy rule";
    private final String testRules = "dummmy rules";
    private final String testEvent = "dummy event";
    private final String testResultOutput = "test output";
    private NikitaCompiler nikitaCompiler;
    private NikitaResult nikitaResult;
    private NikitaAttributes nikitaAttributes;

    @Before
    public void Setup() {
        nikitaCompiler = Mockito.mock(NikitaCompiler.class);
        this.nikitaRuleSchemaService = new NikitaRuleSchemaServiceImpl(nikitaCompiler, ruleSchema);
        nikitaAttributes = new NikitaAttributes();
        nikitaResult = new NikitaResult(NikitaResult.StatusCode.OK, nikitaAttributes);
        Mockito.when(nikitaCompiler.validateRules(any())).thenReturn(nikitaResult);
        Mockito.when(nikitaCompiler.validateRule(any())).thenReturn(nikitaResult);
        Mockito.when(nikitaCompiler.testRule(testRule, testEvent)).thenReturn(nikitaResult);
        Mockito.when(nikitaCompiler.testRule(testRules, testEvent)).thenReturn(nikitaResult);
    }

    @Test
    public void getRulesSchemaOK() {
        ConfigEditorResult ret = nikitaRuleSchemaService.getSchema();
        Assert.assertEquals(ConfigEditorResult.StatusCode.OK, ret.getStatusCode());
        Assert.assertEquals(ret.getAttributes().getRulesSchema(), ruleSchema);
    }

    @Test
    public void validateRulesOK() {
        ConfigEditorResult ret = nikitaRuleSchemaService.validateConfigurations(testRules);
        verify(nikitaCompiler, times(1)).validateRules(testRules);
        Assert.assertEquals(ConfigEditorResult.StatusCode.OK, ret.getStatusCode());
    }

    @Test
    public void validateRulesError() {

        ConfigEditorResult ret = nikitaRuleSchemaService.validateConfigurations(testRules);
        verify(nikitaCompiler, times(1)).validateRules(testRules);
        Assert.assertEquals(ConfigEditorResult.StatusCode.OK, ret.getStatusCode());
    }

    @Test
    public void validateRuleOK() {
        ConfigEditorResult ret = nikitaRuleSchemaService.validateConfiguration(testRule);
        verify(nikitaCompiler, times(1)).validateRule(testRule);
        Assert.assertEquals(ConfigEditorResult.StatusCode.OK, ret.getStatusCode());
    }

    @Test
    public void ValidateRulesError()  {
        nikitaAttributes.setMessage("error");
        nikitaAttributes.setException("exception");
        nikitaResult = new NikitaResult(NikitaResult.StatusCode.ERROR, nikitaAttributes);
        Mockito.when(nikitaCompiler.validateRules(any())).thenReturn(nikitaResult);
        ConfigEditorResult ret = nikitaRuleSchemaService.validateConfigurations(testRules);
        verify(nikitaCompiler, times(1)).validateRules(testRules);
        Assert.assertEquals(ConfigEditorResult.StatusCode.ERROR, ret.getStatusCode());
        Assert.assertEquals("error", ret.getAttributes().getMessage());
        Assert.assertEquals("exception", ret.getAttributes().getException());
    }

    @Test
    public void ValidateRuleError() {
        nikitaAttributes.setMessage("error");
        nikitaAttributes.setException("exception");
        nikitaResult = new NikitaResult(NikitaResult.StatusCode.ERROR, nikitaAttributes);
        Mockito.when(nikitaCompiler.validateRule(any())).thenReturn(nikitaResult);
        ConfigEditorResult ret = nikitaRuleSchemaService.validateConfiguration(testRule);
        verify(nikitaCompiler, times(1)).validateRule(testRule);
        Assert.assertEquals(ConfigEditorResult.StatusCode.ERROR, ret.getStatusCode());
        Assert.assertEquals("error", ret.getAttributes().getMessage());
        Assert.assertEquals("exception", ret.getAttributes().getException());
    }

    @Test
    public void TestRuleOK() {
        nikitaAttributes.setMessage(testResultOutput);
        ConfigEditorResult ret = nikitaRuleSchemaService.testConfiguration(testRule, testEvent);
        verify(nikitaCompiler, times(1)).testRule(testRule, testEvent);
        Assert.assertEquals(ConfigEditorResult.StatusCode.OK, ret.getStatusCode());
        Assert.assertTrue(ret.getAttributes().getTestResultComplete());
        Assert.assertEquals(testResultOutput, ret.getAttributes().getTestResultOutput());
    }

    @Test
    public void TestRulesError() {
        nikitaAttributes.setMessage("error");
        nikitaAttributes.setException("exception");
        nikitaResult = new NikitaResult(NikitaResult.StatusCode.ERROR, nikitaAttributes);
        Mockito.when(nikitaCompiler.testRules(testRules, testEvent)).thenReturn(nikitaResult);
        ConfigEditorResult ret = nikitaRuleSchemaService.testConfigurations(testRules, testEvent);
        verify(nikitaCompiler, times(1)).testRules(testRules, testEvent);
        Assert.assertEquals(ConfigEditorResult.StatusCode.ERROR, ret.getStatusCode());
        Assert.assertEquals("error", ret.getAttributes().getMessage());
        Assert.assertEquals("exception", ret.getAttributes().getException());
    }

    @Test
    public void TestRuleError() {
        nikitaAttributes.setMessage("error");
        nikitaAttributes.setException("exception");
        nikitaResult = new NikitaResult(NikitaResult.StatusCode.ERROR, nikitaAttributes);
        Mockito.when(nikitaCompiler.testRule(testRule, testEvent)).thenReturn(nikitaResult);
        ConfigEditorResult ret = nikitaRuleSchemaService.testConfiguration(testRule, testEvent);
        verify(nikitaCompiler, times(1)).testRule(testRule, testEvent);
        Assert.assertEquals(ConfigEditorResult.StatusCode.ERROR, ret.getStatusCode());
        Assert.assertEquals("error", ret.getAttributes().getMessage());
        Assert.assertEquals("exception", ret.getAttributes().getException());
    }

    @Test
    public void TestRuleInternalError() {
        nikitaResult = new NikitaResult(NikitaResult.StatusCode.OK, nikitaAttributes);
        Mockito.when(nikitaCompiler.testRule(testRule, testEvent)).thenReturn(nikitaResult);
        ConfigEditorResult ret = nikitaRuleSchemaService.testConfiguration(testRule, testEvent);
        verify(nikitaCompiler, times(1)).testRule(testRule, testEvent);
        Assert.assertEquals(ConfigEditorResult.StatusCode.ERROR, ret.getStatusCode());
    }
}
