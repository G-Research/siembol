package uk.co.gresearch.siembol.configeditor.service.response;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import uk.co.gresearch.siembol.common.jsonschema.SiembolJsonSchemaValidator;
import uk.co.gresearch.siembol.common.model.testing.ResponseTestSpecificationDto;
import uk.co.gresearch.siembol.common.utils.HttpProvider;
import uk.co.gresearch.siembol.configeditor.common.ConfigTester;
import uk.co.gresearch.siembol.configeditor.common.ConfigTesterFlag;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult;
import uk.co.gresearch.siembol.common.constants.ResponseApplicationPaths;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.*;
import static uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult.StatusCode.*;

public class ResponseConfigTesterTest {
    private HttpProvider httpProvider;
    private ResponseHttpProvider responseHttpProvider;

    private ResponseConfigTester configTester;

    private String testSchema = "schema";
    private SiembolJsonSchemaValidator testValidator;

    private final String okMessage = """
            {
              "statusCode":"OK",
              "attributes":{ "message" : "dummy"}}
            }
            """;

    private final String dummyJsonObject = "{ \"dummy\" : true }";
    private final String dummyJsonObject2 = "{ \"dummy2\" : true }";

    private final String errorMessage = "error";

    @Before
    public void setUp() throws Exception {
        httpProvider = Mockito.mock(HttpProvider.class);
        Mockito.when(httpProvider.post(eq(ResponseApplicationPaths.TEST_RULES.toString()), anyString()))
                .thenReturn(okMessage);

        responseHttpProvider = new ResponseHttpProvider(httpProvider);
        testValidator = new SiembolJsonSchemaValidator(ResponseTestSpecificationDto.class);
        configTester = new ResponseConfigTester(testValidator, testSchema, responseHttpProvider);
    }

    @Test
    public void testRulesOk() {

        ConfigEditorResult result = configTester.testConfigurations(dummyJsonObject, dummyJsonObject2);
        Assert.assertEquals(OK, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes().getTestResultOutput());
        Assert.assertNotNull(result.getAttributes().getTestResultRawOutput());
        Assert.assertEquals("dummy", result.getAttributes().getTestResultOutput());
    }

    @Test
    public void testRuleOk() {
        ConfigEditorResult result = configTester.testConfiguration(dummyJsonObject, dummyJsonObject2);
        Assert.assertEquals(OK, result.getStatusCode());
    }

    @Test
    public void testRulesError() throws Exception {
        Mockito.when(httpProvider.post(eq(ResponseApplicationPaths.TEST_RULES.toString()), any()))
                .thenReturn(errorMessage);

        ConfigEditorResult result = configTester.testConfigurations(dummyJsonObject, dummyJsonObject2);
        Assert.assertEquals(ERROR, result.getStatusCode());
    }

    @Test
    public void testRulesException() throws Exception {
        Mockito.when(httpProvider.post(eq(ResponseApplicationPaths.TEST_RULES.toString()), any()))
                .thenThrow(new IOException());
        ConfigEditorResult result = configTester.testConfigurations(dummyJsonObject, dummyJsonObject2);
        Assert.assertEquals(ConfigEditorResult.StatusCode.ERROR, result.getStatusCode());
    }


    @Test
    public void testRuleError() throws Exception {
        Mockito.when(httpProvider.post(eq(ResponseApplicationPaths.TEST_RULES.toString()), any()))
                .thenReturn(errorMessage);
        ConfigEditorResult result = configTester.testConfiguration(dummyJsonObject, dummyJsonObject2);
        Assert.assertEquals(ERROR, result.getStatusCode());
    }

    @Test
    public void getFlags() {
        var flags = configTester.getFlags();
        Assert.assertTrue(flags.contains(ConfigTesterFlag.CONFIG_TESTING));
        Assert.assertFalse(flags.contains(ConfigTesterFlag.TEST_CASE_TESTING));
        Assert.assertTrue(flags.contains(ConfigTesterFlag.RELEASE_TESTING));
    }

    @Test
    public void getTestSpecificationSchemaOK() {
        ConfigEditorResult ret = configTester.getTestSpecificationSchema();
        Assert.assertEquals(OK, ret.getStatusCode());
        Assert.assertNotNull(ret.getAttributes().getTestSchema());
        Assert.assertEquals(testSchema, ret.getAttributes().getTestSchema());
    }

    @Test
    public void getTesterInfo() {
        var ret = configTester.getConfigTesterInfo();
        Assert.assertEquals(ConfigTester.DEFAULT_NAME, ret.getName());
        Assert.assertNotNull(ret.getTestSchema());
        Assert.assertTrue(ret.isConfigTesting());
        Assert.assertTrue(ret.isReleaseTesting());
        Assert.assertFalse(ret.isTestCaseTesting());
        Assert.assertFalse(ret.isIncompleteResult());
    }
}
