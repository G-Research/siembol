package uk.co.gresearch.siembol.configeditor.service.alerts;

import org.adrianwalker.multilinestring.Multiline;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import uk.co.gresearch.siembol.common.jsonschema.SiembolJsonSchemaValidator;
import uk.co.gresearch.siembol.common.result.SiembolAttributes;
import uk.co.gresearch.siembol.common.result.SiembolResult;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult;
import uk.co.gresearch.siembol.alerts.common.AlertingAttributes;
import uk.co.gresearch.siembol.alerts.common.AlertingResult;
import uk.co.gresearch.siembol.alerts.compiler.AlertingCompiler;
import uk.co.gresearch.siembol.configeditor.service.common.ConfigSchemaServiceContext;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;
import static uk.co.gresearch.siembol.common.result.SiembolResult.StatusCode.ERROR;
import static uk.co.gresearch.siembol.common.result.SiembolResult.StatusCode.OK;


public class AlertingRuleSchemaServiceTest {
    /**
     * {"test_event":"true"}
     **/
    @Multiline
    public static String testEvent;
    /**
     * {
     *   "event" : {"test_event":"true"}
     * }
     **/
    @Multiline
    public static String testSpecification;

    private AlertingRuleSchemaService alertingRuleSchemaService;
    private final String ruleSchema = "dummmy schema";
    private final String testSchema = "dummmy test schema";
    private final String adminSchema = "dummmy admin config schema";
    private final String testRule = "dummmy rule";
    private final String testRules = "dummmy rules";
    private final String testResultOutput = "test output";
    private final String testConfig = "dummmy config";
    private AlertingCompiler alertingCompiler;
    private AlertingResult alertingResult;
    private AlertingAttributes alertingAttributes;
    private ConfigSchemaServiceContext context;
    private SiembolJsonSchemaValidator adminConfigValidator;
    private SiembolResult validationResult;

    @Before
    public void Setup() throws Exception {
        alertingCompiler = Mockito.mock(AlertingCompiler.class);
        adminConfigValidator = Mockito.mock(SiembolJsonSchemaValidator.class);
        validationResult = new SiembolResult(OK, new SiembolAttributes());
        when(adminConfigValidator.validate(eq(testConfig))).thenReturn(validationResult);

        context = new ConfigSchemaServiceContext();
        context.setTestSchema(testSchema);
        context.setConfigSchema(ruleSchema);
        context.setAdminConfigSchema(adminSchema);
        context.setAdminConfigValidator(adminConfigValidator);
        this.alertingRuleSchemaService = new AlertingRuleSchemaService(alertingCompiler, context);

        alertingAttributes = new AlertingAttributes();
        alertingResult = new AlertingResult(AlertingResult.StatusCode.OK, alertingAttributes);
        Mockito.when(alertingCompiler.validateRules(anyString())).thenReturn(alertingResult);
        Mockito.when(alertingCompiler.validateRule(anyString())).thenReturn(alertingResult);
        Mockito.when(alertingCompiler.testRule(testRule, testEvent.trim())).thenReturn(alertingResult);
        Mockito.when(alertingCompiler.testRule(testRules, testEvent.trim())).thenReturn(alertingResult);
    }

    @Test
    public void getRulesSchemaOK() {
        ConfigEditorResult ret = alertingRuleSchemaService.getSchema();
        Assert.assertEquals(ConfigEditorResult.StatusCode.OK, ret.getStatusCode());
        Assert.assertEquals(ret.getAttributes().getRulesSchema(), ruleSchema);
    }

    @Test
    public void getAdminConfigSchemaOK() {
        ConfigEditorResult ret = alertingRuleSchemaService.getAdminConfigurationSchema();
        Assert.assertEquals(ConfigEditorResult.StatusCode.OK, ret.getStatusCode());
        Assert.assertEquals(adminSchema, ret.getAttributes().getAdminConfigSchema());
    }

    @Test
    public void validateAdminConfigOK() {
        ConfigEditorResult ret = alertingRuleSchemaService.validateAdminConfiguration(testConfig);
        Assert.assertEquals(ConfigEditorResult.StatusCode.OK, ret.getStatusCode());
        verify(adminConfigValidator, times(1)).validate(testConfig);
    }

    @Test
    public void validateAdminConfigInvalid() {
        when(adminConfigValidator.validate(eq(testConfig)))
                .thenReturn(new SiembolResult(ERROR, new SiembolAttributes()));
        ConfigEditorResult ret = alertingRuleSchemaService.validateAdminConfiguration(testConfig);
        Assert.assertEquals(ConfigEditorResult.StatusCode.BAD_REQUEST, ret.getStatusCode());
        verify(adminConfigValidator, times(1)).validate(testConfig);
    }

    @Test
    public void getTestSchemaOK() {
        ConfigEditorResult ret = alertingRuleSchemaService.getTestSchema();
        Assert.assertEquals(ConfigEditorResult.StatusCode.OK, ret.getStatusCode());
        Assert.assertNotNull(ret.getAttributes().getTestSchema());
    }

    @Test
    public void validateRulesOK() {
        ConfigEditorResult ret = alertingRuleSchemaService.validateConfigurations(testRules);
        verify(alertingCompiler, times(1)).validateRules(testRules);
        Assert.assertEquals(ConfigEditorResult.StatusCode.OK, ret.getStatusCode());
    }

    @Test
    public void validateRulesError() {
        ConfigEditorResult ret = alertingRuleSchemaService.validateConfigurations(testRules);
        verify(alertingCompiler, times(1)).validateRules(testRules);
        Assert.assertEquals(ConfigEditorResult.StatusCode.OK, ret.getStatusCode());
    }

    @Test
    public void validateRuleOK() {
        ConfigEditorResult ret = alertingRuleSchemaService.validateConfiguration(testRule);
        verify(alertingCompiler, times(1)).validateRule(testRule);
        Assert.assertEquals(ConfigEditorResult.StatusCode.OK, ret.getStatusCode());
    }

    @Test
    public void ValidateRulesError()  {
        alertingAttributes.setMessage("error");
        alertingAttributes.setException("exception");
        alertingResult = new AlertingResult(AlertingResult.StatusCode.ERROR, alertingAttributes);
        Mockito.when(alertingCompiler.validateRules(anyString())).thenReturn(alertingResult);
        ConfigEditorResult ret = alertingRuleSchemaService.validateConfigurations(testRules);
        verify(alertingCompiler, times(1)).validateRules(testRules);
        Assert.assertEquals(ConfigEditorResult.StatusCode.ERROR, ret.getStatusCode());
        Assert.assertEquals("error", ret.getAttributes().getMessage());
        Assert.assertEquals("exception", ret.getAttributes().getException());
    }

    @Test
    public void ValidateRuleError() {
        alertingAttributes.setMessage("error");
        alertingAttributes.setException("exception");
        alertingResult = new AlertingResult(AlertingResult.StatusCode.ERROR, alertingAttributes);
        Mockito.when(alertingCompiler.validateRule(anyString())).thenReturn(alertingResult);
        ConfigEditorResult ret = alertingRuleSchemaService.validateConfiguration(testRule);
        verify(alertingCompiler, times(1)).validateRule(testRule);
        Assert.assertEquals(ConfigEditorResult.StatusCode.ERROR, ret.getStatusCode());
        Assert.assertEquals("error", ret.getAttributes().getMessage());
        Assert.assertEquals("exception", ret.getAttributes().getException());
    }

    @Test
    public void TestRuleOK() {
        alertingAttributes.setMessage(testResultOutput);
        ConfigEditorResult ret = alertingRuleSchemaService.testConfiguration(testRule, testSpecification);
        verify(alertingCompiler, times(1)).testRule(testRule, testEvent.trim());
        Assert.assertEquals(ConfigEditorResult.StatusCode.OK, ret.getStatusCode());
        Assert.assertTrue(ret.getAttributes().getTestResultComplete());
        Assert.assertEquals(testResultOutput, ret.getAttributes().getTestResultOutput());
    }

    @Test
    public void TestRulesError() {
        alertingAttributes.setMessage("error");
        alertingAttributes.setException("exception");
        alertingResult = new AlertingResult(AlertingResult.StatusCode.ERROR, alertingAttributes);
        Mockito.when(alertingCompiler.testRules(testRules, testEvent.trim())).thenReturn(alertingResult);
        ConfigEditorResult ret = alertingRuleSchemaService.testConfigurations(testRules, testSpecification);
        verify(alertingCompiler, times(1)).testRules(testRules, testEvent.trim());
        Assert.assertEquals(ConfigEditorResult.StatusCode.ERROR, ret.getStatusCode());
        Assert.assertEquals("error", ret.getAttributes().getMessage());
        Assert.assertEquals("exception", ret.getAttributes().getException());
    }

    @Test
    public void TestRuleError() {
        alertingAttributes.setMessage("error");
        alertingAttributes.setException("exception");
        alertingResult = new AlertingResult(AlertingResult.StatusCode.ERROR, alertingAttributes);
        Mockito.when(alertingCompiler.testRule(testRule, testEvent.trim())).thenReturn(alertingResult);
        ConfigEditorResult ret = alertingRuleSchemaService.testConfiguration(testRule, testSpecification);
        verify(alertingCompiler, times(1)).testRule(testRule, testEvent.trim());
        Assert.assertEquals(ConfigEditorResult.StatusCode.ERROR, ret.getStatusCode());
        Assert.assertEquals("error", ret.getAttributes().getMessage());
        Assert.assertEquals("exception", ret.getAttributes().getException());
    }

    @Test
    public void TestRuleInternalError() {
        alertingResult = new AlertingResult(AlertingResult.StatusCode.OK, alertingAttributes);
        Mockito.when(alertingCompiler.testRule(testRule, testEvent.trim())).thenReturn(alertingResult);
        ConfigEditorResult ret = alertingRuleSchemaService.testConfiguration(testRule, testSpecification);
        verify(alertingCompiler, times(1)).testRule(testRule, testEvent.trim());
        Assert.assertEquals(ConfigEditorResult.StatusCode.ERROR, ret.getStatusCode());
    }
}
