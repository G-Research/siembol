package uk.co.gresearch.nortem.configeditor.service.centrifuge;

import org.adrianwalker.multilinestring.Multiline;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import uk.co.gresearch.nortem.common.utils.HttpProvider;
import uk.co.gresearch.nortem.configeditor.common.ConfigEditorUtils;
import org.mockito.Mockito;

import uk.co.gresearch.nortem.configeditor.service.centrifuge.model.CentrifugeResponseDto;

import java.io.*;
import static org.mockito.Mockito.verify;
import static org.mockito.internal.verification.VerificationModeFactory.times;


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

    @Before
    public void setup() throws IOException {
        mockHttp = Mockito.mock(HttpProvider.class);
        rulesSchemaResponse = ConfigEditorUtils.readTextFromResources("testSchema.json").get();
        fieldsResponse = ConfigEditorUtils.readTextFromResources("fieldsResponse.json").get();
        uiConfigTest = ConfigEditorUtils.readTextFromResources("uiConfigTest.json").get();
        Mockito.when(mockHttp.get("/api/v1/rules/schema")).thenReturn(rulesSchemaResponse);
        Mockito.when(mockHttp.get("/api/v1/fields")).thenReturn(fieldsResponse);
    }

    @Test
    public void serviceBuildOK() throws IOException {
        centrifugeSchemaService =  new CentrifugeSchemaService.Builder(mockHttp, uiConfigTest)
                .build();
        Assert.assertNotNull(centrifugeSchemaService.getFields());
        Assert.assertNotNull(centrifugeSchemaService.getRulesSchema());
        verify(mockHttp, times(1)).get("/api/v1/rules/schema");
        verify(mockHttp, times(1)).get("/api/v1/fields");
    }

    @Test
    public void testNonExistentFileReturnsDefault() {
        String defaultLayout = ConfigEditorUtils.readUiLayoutFile("INVALID").get();
        Assert.assertEquals("{\"layout\": {}}", defaultLayout);
    }

    @Test(expected = IOException.class)
    public void serviceExceptionRuleSchema() throws IOException {
        Mockito.when(mockHttp.get("/api/v1/rules/schema")).thenThrow(new IOException());
        centrifugeSchemaService =  new CentrifugeSchemaService.Builder(mockHttp, uiConfigTest)
                .build();
    }

    @Test(expected = IOException.class)
    public void serviceWrongRuleSchema() throws IOException {
        Mockito.when(mockHttp.get("/api/v1/rules/schema")).thenReturn("INVALID JSON");
        centrifugeSchemaService =  new CentrifugeSchemaService.Builder(mockHttp, uiConfigTest)
                .build();
    }

    @Test(expected = IllegalStateException.class)
    public void serviceEmptyRuleSchema2() throws IOException {
        Mockito.when(mockHttp.get("/api/v1/rules/schema")).thenReturn("{}");
        centrifugeSchemaService =  new CentrifugeSchemaService.Builder(mockHttp, uiConfigTest)
                .build();
    }

    @Test(expected = IOException.class)
    public void serviceExceptionFields() throws IOException {
        Mockito.when(mockHttp.get("/api/v1/fields")).thenThrow(new IOException());
        centrifugeSchemaService =  new CentrifugeSchemaService.Builder(mockHttp, uiConfigTest)
                .build();
    }

    @Test(expected = IOException.class)
    public void serviceWrongFields() throws IOException {
        Mockito.when(mockHttp.get("/api/v1/fields")).thenReturn("INVALID JSON");
        centrifugeSchemaService =  new CentrifugeSchemaService.Builder(mockHttp, uiConfigTest)
                .build();
    }

    @Test(expected = IllegalStateException.class)
    public void serviceEmptyFields() throws IOException {
        Mockito.when(mockHttp.get("/api/v1/fields")).thenReturn("{}");
        centrifugeSchemaService =  new CentrifugeSchemaService.Builder(mockHttp, uiConfigTest)
                .build();
    }

    @Test(expected = com.fasterxml.jackson.core.JsonParseException.class)
    public void wrongUIConfig() throws IOException {
        centrifugeSchemaService =  new CentrifugeSchemaService.Builder(mockHttp, "INVALID")
                .build();
    }

    @Test
    public void validateRuleOK() throws IOException {
        Mockito.when(mockHttp.post("/api/v1/rules/validate", "DUMMY")).thenReturn(validateOK);
        centrifugeSchemaService =  new CentrifugeSchemaService.Builder(mockHttp, uiConfigTest)
                .build();
        CentrifugeResponseDto response = centrifugeSchemaService.validateRules("DUMMY");
        Assert.assertEquals(CentrifugeResponseDto.StatusCode.OK, response.getStatusCode());
    }

    @Test
    public void validateRuleBad() throws IOException {
        Mockito.when(mockHttp.post("/api/v1/rules/validate", "DUMMY")).thenReturn(validateBadRequest);
        centrifugeSchemaService =  new CentrifugeSchemaService.Builder(mockHttp, uiConfigTest)
                .build();
        CentrifugeResponseDto response = centrifugeSchemaService.validateRules("DUMMY");
        Assert.assertEquals(CentrifugeResponseDto.StatusCode.BAD_REQUEST, response.getStatusCode());
    }

    @Test(expected = IOException.class)
    public void validateRuleException() throws IOException {
        Mockito.when(mockHttp.post("/api/v1/rules/validate", "DUMMY")).thenThrow(new IOException());
        centrifugeSchemaService =  new CentrifugeSchemaService.Builder(mockHttp, uiConfigTest)
                .build();
        centrifugeSchemaService.validateRules("DUMMY");
    }
}
