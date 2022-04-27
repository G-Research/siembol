package uk.co.gresearch.siembol.configeditor.testcase;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorAttributes;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult;

import static org.mockito.Mockito.*;

public class TestCaseEvaluatorWithErrorMessageTest {
    private TestCaseEvaluator service;
    private TestCaseEvaluator serviceWithErrorMessage;
    private ConfigEditorResult result;
    private ConfigEditorAttributes attributes;

    @Before
    public void setUp() throws Exception {
        service = Mockito.mock(TestCaseEvaluator.class);
        when(service.withErrorMessage()).thenCallRealMethod();
        serviceWithErrorMessage = service.withErrorMessage();
        attributes = new ConfigEditorAttributes();
        result = new ConfigEditorResult(ConfigEditorResult.StatusCode.OK, attributes);
    }

    @Test
    public void getSchemaOk() {
        when(service.getSchema()).thenReturn(result);
        var ret = serviceWithErrorMessage.getSchema();
        Assert.assertEquals(ret, result);
        verify(service, times(1)).getSchema();
    }

    @Test
    public void validateTestCaseOk() {
        when(service.validate(eq("a"))).thenReturn(result);
        var ret = serviceWithErrorMessage.validate("a");
        Assert.assertEquals(ret, result);
        verify(service, times(1)).validate(eq("a"));
    }

    @Test
    public void validateTestCaseBadRequest() {
        result = new ConfigEditorResult(ConfigEditorResult.StatusCode.BAD_REQUEST, attributes);

        when(service.validate(eq("a"))).thenReturn(result);
        var ret = serviceWithErrorMessage.validate("a");
        verify(service, times(1)).validate(eq("a"));

        Assert.assertEquals(ConfigEditorResult.StatusCode.BAD_REQUEST, ret.getStatusCode());
        Assert.assertNotNull(ret.getAttributes().getMessage());
        Assert.assertNotNull(ret.getAttributes().getErrorResolution());
        Assert.assertNotNull(ret.getAttributes().getErrorTitle());
    }

    @Test
    public void evaluateTestCaseOk() {
        when(service.evaluate(eq("a"), eq("b"))).thenReturn(result);
        var ret = serviceWithErrorMessage.evaluate("a", "b");
        Assert.assertEquals(ret, result);
        verify(service, times(1)).evaluate(eq("a"), eq("b"));
    }

    @Test
    public void evaluateTestCaseBadRequest() {
        result = new ConfigEditorResult(ConfigEditorResult.StatusCode.BAD_REQUEST, attributes);

        when(service.evaluate(eq("a"), eq("b"))).thenReturn(result);
        var ret = serviceWithErrorMessage.evaluate("a", "b");
        verify(service, times(1)).evaluate(eq("a"), eq("b"));

        Assert.assertEquals(ConfigEditorResult.StatusCode.BAD_REQUEST, ret.getStatusCode());
        Assert.assertNotNull(ret.getAttributes().getMessage());
        Assert.assertNotNull(ret.getAttributes().getErrorResolution());
        Assert.assertNotNull(ret.getAttributes().getErrorTitle());
    }

}
