package uk.co.gresearch.siembol.configeditor.service.alerts;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import uk.co.gresearch.siembol.alerts.common.AlertingAttributes;
import uk.co.gresearch.siembol.alerts.common.AlertingResult;
import uk.co.gresearch.siembol.alerts.compiler.AlertingCompiler;
import uk.co.gresearch.siembol.common.jsonschema.SiembolJsonSchemaValidator;
import uk.co.gresearch.siembol.configeditor.common.ConfigTesterFlag;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult;

import static org.mockito.Mockito.verify;
import static org.mockito.internal.verification.VerificationModeFactory.times;

public class AlertingConfigTesterTest {
    private final String testEvent = """
            {"test_event":"true"}""";

    private final String testSpecification = """
            {
              "event" : {"test_event":"true"}
            }
            """;

    private AlertingConfigTester configTester;
    private final String testSchema = "dummy schema";
    private final String testRule = "dummy rule";
    private final String testRules = "dummy rules";
    private final String testResultOutput = "test output";
    private AlertingCompiler alertingCompiler;
    private AlertingResult alertingResult;
    private AlertingAttributes alertingAttributes;
    private SiembolJsonSchemaValidator testValidator;

    @Before
    public void setUp() {
        alertingCompiler = Mockito.mock(AlertingCompiler.class);
        testValidator = Mockito.mock(SiembolJsonSchemaValidator.class);

        alertingAttributes = new AlertingAttributes();
        alertingResult = new AlertingResult(AlertingResult.StatusCode.OK, alertingAttributes);

        Mockito.when(alertingCompiler.testRule(testRule, testEvent.trim())).thenReturn(alertingResult);
        Mockito.when(alertingCompiler.testRule(testRules, testEvent.trim())).thenReturn(alertingResult);

        configTester = new AlertingConfigTester(testValidator, testSchema, alertingCompiler);
    }

    @Test
    public void testRuleOK() {
        alertingAttributes.setMessage(testResultOutput);
        ConfigEditorResult ret = configTester.testConfiguration(testRule, testSpecification);
        verify(alertingCompiler, times(1)).testRule(testRule, testEvent.trim());
        Assert.assertEquals(ConfigEditorResult.StatusCode.OK, ret.getStatusCode());
        Assert.assertEquals(testResultOutput, ret.getAttributes().getTestResultOutput());
    }

    @Test
    public void testRulesError() {
        alertingAttributes.setMessage("error");
        alertingAttributes.setException("exception");
        alertingResult = new AlertingResult(AlertingResult.StatusCode.ERROR, alertingAttributes);
        Mockito.when(alertingCompiler.testRules(testRules, testEvent.trim())).thenReturn(alertingResult);
        ConfigEditorResult ret = configTester.testConfigurations(testRules, testSpecification);
        verify(alertingCompiler, times(1)).testRules(testRules, testEvent.trim());
        Assert.assertEquals(ConfigEditorResult.StatusCode.BAD_REQUEST, ret.getStatusCode());
        Assert.assertEquals("error", ret.getAttributes().getMessage());
        Assert.assertEquals("exception", ret.getAttributes().getException());
    }

    @Test
    public void testRuleError() {
        alertingAttributes.setMessage("error");
        alertingAttributes.setException("exception");
        alertingResult = new AlertingResult(AlertingResult.StatusCode.ERROR, alertingAttributes);
        Mockito.when(alertingCompiler.testRule(testRule, testEvent.trim())).thenReturn(alertingResult);
        ConfigEditorResult ret = configTester.testConfiguration(testRule, testSpecification);
        verify(alertingCompiler, times(1)).testRule(testRule, testEvent.trim());
        Assert.assertEquals(ConfigEditorResult.StatusCode.BAD_REQUEST, ret.getStatusCode());
        Assert.assertEquals("error", ret.getAttributes().getMessage());
        Assert.assertEquals("exception", ret.getAttributes().getException());
    }

    @Test
    public void testRuleInternalError() {
        alertingResult = new AlertingResult(AlertingResult.StatusCode.OK, alertingAttributes);
        Mockito.when(alertingCompiler.testRule(testRule, testEvent.trim())).thenReturn(alertingResult);
        ConfigEditorResult ret = configTester.testConfiguration(testRule, testSpecification);
        verify(alertingCompiler, times(1)).testRule(testRule, testEvent.trim());
        Assert.assertEquals(ConfigEditorResult.StatusCode.ERROR, ret.getStatusCode());
    }

    @Test
    public void testRulesWrongTestSpecification() {
        alertingAttributes.setMessage("error");
        alertingAttributes.setException("exception");
        alertingResult = new AlertingResult(AlertingResult.StatusCode.ERROR, alertingAttributes);
        Mockito.when(alertingCompiler.testRules(testRules, testEvent.trim())).thenReturn(alertingResult);
        ConfigEditorResult ret = configTester.testConfigurations(testRules, "INVALID");
        Assert.assertEquals(ConfigEditorResult.StatusCode.BAD_REQUEST, ret.getStatusCode());
        Assert.assertNotNull(ret.getAttributes().getException());
    }

    @Test
    public void testRuleWrongTestSpecification() {
        alertingAttributes.setMessage("error");
        alertingAttributes.setException("exception");
        alertingResult = new AlertingResult(AlertingResult.StatusCode.ERROR, alertingAttributes);
        Mockito.when(alertingCompiler.testRule(testRule, testEvent.trim())).thenReturn(alertingResult);
        ConfigEditorResult ret = configTester.testConfiguration(testRule, "INVALID");
        Assert.assertEquals(ConfigEditorResult.StatusCode.BAD_REQUEST, ret.getStatusCode());
        Assert.assertNotNull(ret.getAttributes().getException());
    }

    @Test
    public void getFlags() {
        var flags = configTester.getFlags();
        Assert.assertTrue(flags.contains(ConfigTesterFlag.CONFIG_TESTING));
        Assert.assertTrue(flags.contains(ConfigTesterFlag.TEST_CASE_TESTING));
        Assert.assertTrue(flags.contains(ConfigTesterFlag.RELEASE_TESTING));
    }

    @Test
    public void getTestSpecificationSchemaOK() {
        ConfigEditorResult ret = configTester.getTestSpecificationSchema();
        Assert.assertEquals(ConfigEditorResult.StatusCode.OK, ret.getStatusCode());
        Assert.assertNotNull(ret.getAttributes().getTestSchema());
        Assert.assertEquals(testSchema, ret.getAttributes().getTestSchema());
    }
}
