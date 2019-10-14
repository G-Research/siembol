package uk.co.gresearch.nortem.configeditor.service.centrifuge;

import org.adrianwalker.multilinestring.Multiline;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.boot.actuate.health.Status;
import uk.co.gresearch.nortem.configeditor.model.ConfigEditorResult;
import uk.co.gresearch.nortem.configeditor.service.centrifuge.model.CentrifugeResponseDto;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.internal.verification.VerificationModeFactory.times;
import static uk.co.gresearch.nortem.configeditor.service.centrifuge.model.CentrifugeResponseDto.StatusCode.ERROR;
import static uk.co.gresearch.nortem.configeditor.service.centrifuge.model.CentrifugeResponseDto.StatusCode.OK;

public class RuleCentrifugeSchemaServiceImplTest {
    private CentrifugeSchemaService service;

    /**
     *{"rule_name": "Walle_heartbeat","rule_author": "mariann","rule_version": 1,"rule_description": "A Rule for implementing health check.","conditions": [{"field_name": "ava:heartbeat","field_pattern": "true"}],"enrichments": {},"actions": {"elk_store": false,"heartbeat": true}}
     */
    @Multiline
    private static String ruleJson;

    /**
     *{"rules_version":1,"rules":[{"rule_name":"Walle_heartbeat","rule_author":"mariann","rule_version":1,"rule_description":"A Rule for implementing health check.","conditions":[{"field_name":"ava:heartbeat","field_pattern":"true"}],"enrichments":{},"actions":{"elk_store":false,"heartbeat":true}}]}*/
    @Multiline
    private static String expectedWrappedRuleJson;

    private CentrifugeRuleSchemaImpl schemaServiceImpl;
    private CentrifugeResponseDto response;

    @Before
    public void Setup() {
        service = Mockito.mock(CentrifugeSchemaService.class);
        this.schemaServiceImpl = new CentrifugeRuleSchemaImpl(service);
        response = new CentrifugeResponseDto();
    }

    @Test
    public void getRulesSchemaOK() {
        Mockito.when(service.getRulesSchema()).thenReturn("DUMMY");
        ConfigEditorResult ret = schemaServiceImpl.getSchema();
        verify(service, times(1)).getRulesSchema();
        Assert.assertEquals(ConfigEditorResult.StatusCode.OK, ret.getStatusCode());
        Assert.assertEquals("DUMMY", ret.getAttributes().getRulesSchema());
    }

    @Test
    public void getFieldsOK() {
        Mockito.when(service.getFields()).thenReturn("DUMMY");
        ConfigEditorResult ret = schemaServiceImpl.getFields();
        verify(service, times(1)).getFields();
        Assert.assertEquals(ConfigEditorResult.StatusCode.OK, ret.getStatusCode());
        Assert.assertEquals("DUMMY", ret.getAttributes().getFields());
    }

    @Test
    public void validateRulesOK() throws IOException {
        Mockito.when(service.validateRules("DUMMY")).thenReturn(response);
        response.setStatusCode(OK);
        ConfigEditorResult ret = schemaServiceImpl.validateConfigurations("DUMMY");
        verify(service, times(1)).validateRules("DUMMY");
        Assert.assertEquals(ConfigEditorResult.StatusCode.OK, ret.getStatusCode());
        Assert.assertEquals(Status.UP, schemaServiceImpl.checkHealth().getStatus());
    }

    @Test
    public void validateRulesBad() throws IOException {
        Mockito.when(service.validateRules("DUMMY")).thenReturn(response);
        response.setStatusCode(ERROR);
        response.getAttributes().setMessage("MESSAGE");
        response.getAttributes().setException("EXCEPTION");
        ConfigEditorResult ret = schemaServiceImpl.validateConfigurations("DUMMY");
        verify(service, times(1)).validateRules("DUMMY");
        Assert.assertEquals(ConfigEditorResult.StatusCode.ERROR, ret.getStatusCode());
        Assert.assertEquals("MESSAGE", ret.getAttributes().getMessage() );
        Assert.assertEquals("EXCEPTION", ret.getAttributes().getException());
        Assert.assertEquals(Status.UP, schemaServiceImpl.checkHealth().getStatus());
    }

    @Test
    public void validateRulesException() throws IOException {
        Mockito.when(service.validateRules("DUMMY")).thenThrow(new IOException());
        ConfigEditorResult ret = schemaServiceImpl.validateConfigurations("DUMMY");
        verify(service, times(1)).validateRules("DUMMY");
        Assert.assertEquals( ConfigEditorResult.StatusCode.ERROR, ret.getStatusCode());
        Assert.assertTrue(ret.getAttributes().getException().contains("java.io.IOException"));
        Assert.assertEquals(Status.DOWN, schemaServiceImpl.checkHealth().getStatus());

        response.setStatusCode(OK);
        Mockito.when(service.validateRules("DUMMY2")).thenReturn(response);
        ret = schemaServiceImpl.validateConfigurations("DUMMY2");
        Assert.assertEquals(Status.UP, schemaServiceImpl.checkHealth().getStatus());
    }

    @Test
    public void validateRule() throws IOException {
        response.setStatusCode(OK);
        Mockito.when(service.validateRules(anyString())).thenReturn(response);
        ConfigEditorResult ret = schemaServiceImpl.validateConfiguration(ruleJson);

        ArgumentCaptor<String> argument = ArgumentCaptor.forClass(String.class);
        verify(service).validateRules(argument.capture());

        Assert.assertEquals(ConfigEditorResult.StatusCode.OK, ret.getStatusCode());
        Assert.assertEquals(Status.UP, schemaServiceImpl.checkHealth().getStatus());
        Assert.assertEquals(expectedWrappedRuleJson, argument.getValue());
    }

    @Test
    public void testRulesOK() throws IOException {
        Mockito.when(service.testRules("DUMMY_RULES", "DUMMY_EVENT")).thenReturn(response);
        response.setStatusCode(OK);
        response.getAttributes().setMessage("DUMMY_RESULT");
        ConfigEditorResult ret = schemaServiceImpl.testConfigurations("DUMMY_RULES", "DUMMY_EVENT");
        verify(service, times(1)).testRules("DUMMY_RULES", "DUMMY_EVENT");
        Assert.assertEquals(ConfigEditorResult.StatusCode.OK, ret.getStatusCode());
        Assert.assertTrue(ret.getAttributes().getTestResultComplete());
        Assert.assertEquals("DUMMY_RESULT", ret.getAttributes().getTestResultOutput());
        Assert.assertEquals(Status.UP, schemaServiceImpl.checkHealth().getStatus());
    }

    @Test
    public void testRulesBad() throws IOException {
        Mockito.when(service.testRules("DUMMY_RULES", "DUMMY_EVENT")).thenReturn(response);
        response.setStatusCode(ERROR);
        response.getAttributes().setMessage("MESSAGE");
        response.getAttributes().setException("EXCEPTION");
        ConfigEditorResult ret = schemaServiceImpl.testConfigurations("DUMMY_RULES", "DUMMY_EVENT");
        verify(service, times(1)).testRules("DUMMY_RULES", "DUMMY_EVENT");
        Assert.assertEquals(ConfigEditorResult.StatusCode.ERROR, ret.getStatusCode());
        Assert.assertEquals("MESSAGE", ret.getAttributes().getMessage());
        Assert.assertEquals("EXCEPTION", ret.getAttributes().getException());
        Assert.assertEquals(Status.UP, schemaServiceImpl.checkHealth().getStatus());
    }

    @Test
    public void testRulesException() throws IOException {
        Mockito.when(service.testRules("DUMMY_RULES", "DUMMY_EVENT"))
                .thenThrow(new IOException());
        ConfigEditorResult ret = schemaServiceImpl.testConfigurations("DUMMY_RULES", "DUMMY_EVENT");
        verify(service, times(1)).testRules("DUMMY_RULES", "DUMMY_EVENT");
        Assert.assertEquals(ConfigEditorResult.StatusCode.ERROR, ret.getStatusCode());
        Assert.assertTrue(ret.getAttributes().getException().contains("java.io.IOException"));
        Assert.assertEquals(Status.DOWN, schemaServiceImpl.checkHealth().getStatus());

        response.setStatusCode(OK);
        Mockito.when(service.testRules("DUMMY_RULES_2", "DUMMY_EVENT")).thenReturn(response);
        ret = schemaServiceImpl.testConfigurations("DUMMY_RULES_2", "DUMMY_EVENT");
        Assert.assertEquals(Status.UP, schemaServiceImpl.checkHealth().getStatus());
    }

    @Test
    public void testRule() throws IOException {
        response.setStatusCode(OK);
        Mockito.when(service.testRules(anyString(), anyString())).thenReturn(response);
        ConfigEditorResult ret = schemaServiceImpl.testConfiguration(ruleJson, "{}");

        ArgumentCaptor<String> argument1 = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> argument2= ArgumentCaptor.forClass(String.class);
        verify(service).testRules(argument1.capture(), argument2.capture());

        Assert.assertEquals(ConfigEditorResult.StatusCode.OK, ret.getStatusCode());
        Assert.assertEquals(Status.UP, schemaServiceImpl.checkHealth().getStatus());
        Assert.assertEquals(expectedWrappedRuleJson, argument1.getValue());
        Assert.assertEquals("{}", argument2.getValue());
    }

    @Test
    public void testRuleBad() {
        ConfigEditorResult ret = schemaServiceImpl.testConfiguration("INVALID", "{}");
        Assert.assertEquals(ConfigEditorResult.StatusCode.BAD_REQUEST, ret.getStatusCode());
        Assert.assertTrue(ret.getAttributes().getException().contains("JsonParseException"));
    }

    @Test
    public void validateRuleBad() {
        ConfigEditorResult ret = schemaServiceImpl.validateConfiguration("INVALID");
        Assert.assertEquals(ConfigEditorResult.StatusCode.BAD_REQUEST, ret.getStatusCode());
        Assert.assertTrue(ret.getAttributes().getException().contains("JsonParseException"));
    }
}
