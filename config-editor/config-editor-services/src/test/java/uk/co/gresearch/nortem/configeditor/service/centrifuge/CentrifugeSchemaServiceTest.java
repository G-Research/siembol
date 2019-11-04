package uk.co.gresearch.nortem.configeditor.service.centrifuge;

import com.fasterxml.jackson.core.JsonParseException;
import org.adrianwalker.multilinestring.Multiline;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import uk.co.gresearch.nortem.common.utils.HttpProvider;
import uk.co.gresearch.nortem.configeditor.common.ConfigEditorUtils;
import org.mockito.Mockito;

import uk.co.gresearch.nortem.configeditor.service.centrifuge.model.CentrifugeResponseDto;

import java.io.*;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.internal.verification.VerificationModeFactory.times;
import static uk.co.gresearch.nortem.configeditor.service.centrifuge.model.CentrifugeResponseDto.StatusCode.OK;


public class CentrifugeSchemaServiceTest {
    private HttpProvider mockHttp;
    private CentrifugeSchemaService centrifugeSchemaService;
    private String rulesSchemaResponse;
    private String fieldsResponse;
    private String uiConfigTest;

    /**
     {
        "statusCode": "BAD_REQUEST",
        "attributes": {
            "exception": "com.fasterxml.jackson.core.JsonParseException: Unexpected character ..."
        }
     }
     *
     **/
    @Multiline
    private static String validateBadRequest;

    /**
     {
        "statusCode": "OK"
     }
     *
     **/
    @Multiline
    private static String validateOK;

    /**
     * {
     *      "statusCode": "OK",
     *      "attributes" : {
     *          "message" : "testing completed"
     *      }
     * }
     */
    @Multiline
    private static String testResponseOK;

    /**
     {"event" : {"test":true}}
     *
     **/
    @Multiline
    private static String testExample;

    /**
     {"event":{"test":true},"json_rules":{"rules":true}}
     **/
    @Multiline
    private static String testRequest;

    @Before
    public void setup() throws IOException {
        mockHttp = Mockito.mock(HttpProvider.class);
        rulesSchemaResponse = ConfigEditorUtils.readTextFromResources("testSchema.json").get();
        fieldsResponse = ConfigEditorUtils.readTextFromResources("fieldsResponse.json").get();
        uiConfigTest = ConfigEditorUtils.readTextFromResources("uiConfigTest.json").get();
        Mockito.when(mockHttp.get("/api/v1/rules/schema")).thenReturn(rulesSchemaResponse);
        Mockito.when(mockHttp.get("/api/v1/fields")).thenReturn(fieldsResponse);
        Mockito.when(mockHttp.post("/api/v1/rules/test", testRequest.trim())).thenReturn(testResponseOK);
    }

    @Test
    public void serviceBuildOK() throws Exception {
        centrifugeSchemaService =  new CentrifugeSchemaService.Builder(mockHttp, uiConfigTest)
                .build();
        Assert.assertNotNull(centrifugeSchemaService.getFields());
        Assert.assertNotNull(centrifugeSchemaService.getRulesSchema());
        Assert.assertNotNull(centrifugeSchemaService.getTestSchema());
        verify(mockHttp, times(1)).get("/api/v1/rules/schema");
        verify(mockHttp, times(1)).get("/api/v1/fields");
    }

    @Test(expected = IOException.class)
    public void serviceExceptionRuleSchema() throws Exception {
        Mockito.when(mockHttp.get("/api/v1/rules/schema")).thenThrow(new IOException());
        centrifugeSchemaService =  new CentrifugeSchemaService.Builder(mockHttp, uiConfigTest)
                .build();
    }

    @Test(expected = IOException.class)
    public void serviceWrongRuleSchema() throws Exception {
        Mockito.when(mockHttp.get("/api/v1/rules/schema")).thenReturn("INVALID JSON");
        centrifugeSchemaService =  new CentrifugeSchemaService.Builder(mockHttp, uiConfigTest)
                .build();
    }

    @Test(expected = IllegalStateException.class)
    public void serviceEmptyRuleSchema2() throws Exception {
        Mockito.when(mockHttp.get("/api/v1/rules/schema")).thenReturn("{}");
        centrifugeSchemaService =  new CentrifugeSchemaService.Builder(mockHttp, uiConfigTest)
                .build();
    }

    @Test(expected = IOException.class)
    public void serviceExceptionFields() throws Exception {
        Mockito.when(mockHttp.get("/api/v1/fields")).thenThrow(new IOException());
        centrifugeSchemaService =  new CentrifugeSchemaService.Builder(mockHttp, uiConfigTest)
                .build();
    }

    @Test(expected = IOException.class)
    public void serviceWrongFields() throws Exception {
        Mockito.when(mockHttp.get("/api/v1/fields")).thenReturn("INVALID JSON");
        centrifugeSchemaService =  new CentrifugeSchemaService.Builder(mockHttp, uiConfigTest)
                .build();
    }

    @Test(expected = IllegalStateException.class)
    public void serviceEmptyFields() throws Exception {
        Mockito.when(mockHttp.get("/api/v1/fields")).thenReturn("{}");
        centrifugeSchemaService =  new CentrifugeSchemaService.Builder(mockHttp, uiConfigTest)
                .build();
    }

    @Test(expected = com.fasterxml.jackson.core.JsonParseException.class)
    public void wrongUIConfig() throws Exception {
        centrifugeSchemaService =  new CentrifugeSchemaService.Builder(mockHttp, "INVALID")
                .build();
    }

    @Test
    public void validateRuleOK() throws Exception {
        Mockito.when(mockHttp.post("/api/v1/rules/validate", "DUMMY")).thenReturn(validateOK);
        centrifugeSchemaService =  new CentrifugeSchemaService.Builder(mockHttp, uiConfigTest)
                .build();
        CentrifugeResponseDto response = centrifugeSchemaService.validateRules("DUMMY");
        Assert.assertEquals(OK, response.getStatusCode());
    }

    @Test
    public void validateRuleBad() throws Exception {
        Mockito.when(mockHttp.post("/api/v1/rules/validate", "DUMMY")).thenReturn(validateBadRequest);
        centrifugeSchemaService =  new CentrifugeSchemaService.Builder(mockHttp, uiConfigTest)
                .build();
        CentrifugeResponseDto response = centrifugeSchemaService.validateRules("DUMMY");
        Assert.assertEquals(CentrifugeResponseDto.StatusCode.BAD_REQUEST, response.getStatusCode());
    }

    @Test(expected = JsonParseException.class)
    public void testRuleWrongSpecification() throws Exception {
        centrifugeSchemaService =  new CentrifugeSchemaService.Builder(mockHttp, uiConfigTest)
                .build();
        CentrifugeResponseDto response = centrifugeSchemaService.testRules("DUMMY", "INVALID");
    }

    @Test
    public void testOKSpecification() throws Exception {
        centrifugeSchemaService =  new CentrifugeSchemaService.Builder(mockHttp, uiConfigTest)
                .build();
        CentrifugeResponseDto response = centrifugeSchemaService.testRules("{\"rules\":true}", testExample);
        Assert.assertEquals(OK, response.getStatusCode());
        Assert.assertNotNull(response.getAttributes());
        Assert.assertTrue(response.getAttributes().getMessage().contains("testing completed"));
        verify(mockHttp, times(1)).post("/api/v1/rules/test", testRequest.trim());
    }

    @Test(expected = IOException.class)
    public void validateRuleException() throws Exception {
        Mockito.when(mockHttp.post("/api/v1/rules/validate", "DUMMY")).thenThrow(new IOException());
        centrifugeSchemaService =  new CentrifugeSchemaService.Builder(mockHttp, uiConfigTest)
                .build();
        centrifugeSchemaService.validateRules("DUMMY");
    }
}
