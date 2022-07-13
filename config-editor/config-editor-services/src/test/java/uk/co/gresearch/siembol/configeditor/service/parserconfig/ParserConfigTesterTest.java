package uk.co.gresearch.siembol.configeditor.service.parserconfig;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import uk.co.gresearch.siembol.common.jsonschema.SiembolJsonSchemaValidator;
import uk.co.gresearch.siembol.common.model.testing.ParserConfingTestSpecificationDto;
import uk.co.gresearch.siembol.configeditor.common.ConfigTester;
import uk.co.gresearch.siembol.configeditor.common.ConfigTesterFlag;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult;
import uk.co.gresearch.siembol.parsers.common.ParserResult;
import uk.co.gresearch.siembol.parsers.factory.ParserFactory;
import uk.co.gresearch.siembol.parsers.factory.ParserFactoryAttributes;
import uk.co.gresearch.siembol.parsers.factory.ParserFactoryResult;

import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;

public class ParserConfigTesterTest {
    private final String logUtf8 = """
            {
              "encoding" : "utf8_string",
              "log" : "dummy log"
            }
            """;

    private final String logHex = """
            {
              "encoding" : "hex_string",
              "log" : "64756D6D79206C6F67"
            }
            """;

    private final String schema = "dummy schema";
    private final String testConfig = "dummy parser config";
    private final String testConfigs = "dummy parser configs";
    private final String testLog = "dummy log";

    private ParserFactory parserFactory;
    private ParserFactoryResult parserFactoryResult;
    private ParserFactoryAttributes parserFactoryAttributes;
    private ParserResult parserResult;

    private SiembolJsonSchemaValidator testValidator;

    private ParserConfigTester configTester;

    @Before
    public void setUp() throws Exception {
        parserFactory = Mockito.mock(ParserFactory.class);
        testValidator = new SiembolJsonSchemaValidator(ParserConfingTestSpecificationDto.class);

        parserFactoryAttributes = new ParserFactoryAttributes();
        parserFactoryResult = new ParserFactoryResult(ParserFactoryResult.StatusCode.OK, parserFactoryAttributes);
        parserResult = new ParserResult();
        Mockito.when(parserFactory.test(anyString(), eq(null), ArgumentMatchers.<byte[]>any()))
                .thenReturn(parserFactoryResult);

        configTester = new ParserConfigTester(testValidator, schema, parserFactory);
    }

    @Test
    public void testConfigurationOk() {
        parserResult.setParsedMessages(new ArrayList<>());
        parserFactoryAttributes.setParserResult(parserResult);
        ConfigEditorResult ret = configTester.testConfiguration(testConfig, logUtf8);
        Mockito.verify(parserFactory, times(1)).test(testConfig, null, testLog.getBytes());
        Assert.assertEquals(ConfigEditorResult.StatusCode.OK, ret.getStatusCode());
    }

    @Test
    public void testConfigurationHexOk() {
        parserResult.setParsedMessages(new ArrayList<>());
        parserFactoryAttributes.setParserResult(parserResult);
        ConfigEditorResult ret = configTester.testConfiguration(testConfig, logHex);
        Mockito.verify(parserFactory, times(1)).test(testConfig,
                null,
                testLog.getBytes());
        Assert.assertEquals(ConfigEditorResult.StatusCode.OK, ret.getStatusCode());
    }

    @Test
    public void testConfigurationHexError() {
        parserResult.setParsedMessages(new ArrayList<>());
        parserFactoryAttributes.setParserResult(parserResult);
        ConfigEditorResult ret = configTester.testConfiguration(testConfig,
                logHex.replace("64", "XX"));
        Assert.assertEquals(ConfigEditorResult.StatusCode.ERROR, ret.getStatusCode());
        Assert.assertTrue(ret.getAttributes().getException().contains("Unrecognized character: X"));
    }

    @Test
    public void testConfigurationsError() {
        parserFactoryAttributes.setMessage("error");
        parserFactoryResult = new ParserFactoryResult(ParserFactoryResult.StatusCode.ERROR, parserFactoryAttributes);
        Mockito.when(parserFactory.test(testConfigs, null, testLog.getBytes())).thenReturn(parserFactoryResult);
        ConfigEditorResult ret = configTester.testConfigurations(testConfigs, testLog);
        Assert.assertEquals(ConfigEditorResult.StatusCode.BAD_REQUEST, ret.getStatusCode());
        Assert.assertEquals("This type of testing is not supported", ret.getAttributes().getMessage());
    }

    @Test
    public void testConfigurationError() {
        parserFactoryAttributes.setMessage("error");
        parserFactoryResult = new ParserFactoryResult(ParserFactoryResult.StatusCode.ERROR, parserFactoryAttributes);
        Mockito.when(parserFactory.test(testConfig, null, testLog.getBytes())).thenReturn(parserFactoryResult);
        ConfigEditorResult ret = configTester.testConfiguration(testConfig, logUtf8);
        Mockito.verify(parserFactory, times(1)).test(testConfig,
                null,
                testLog.getBytes());
        Assert.assertEquals(ConfigEditorResult.StatusCode.BAD_REQUEST, ret.getStatusCode());
        Assert.assertEquals("error", ret.getAttributes().getMessage());
    }


    @Test
    public void testConfigurationParserResultError() {
        parserResult.setException(new IllegalStateException("dummy"));
        parserFactoryAttributes.setParserResult(parserResult);

        parserFactoryResult = new ParserFactoryResult(ParserFactoryResult.StatusCode.OK, parserFactoryAttributes);
        Mockito.when(parserFactory.test(testConfig, null, testLog.getBytes())).thenReturn(parserFactoryResult);
        ConfigEditorResult ret = configTester.testConfiguration(testConfig, logUtf8);
        Mockito.verify(parserFactory, times(1)).test(testConfig,
                null,
                testLog.getBytes());
        Assert.assertEquals(ConfigEditorResult.StatusCode.OK, ret.getStatusCode());
        Assert.assertTrue(ret.getAttributes().getTestResultOutput().contains("dummy"));
        Assert.assertTrue(ret.getAttributes().getTestResultRawOutput().contains("dummy"));
    }

    @Test
    public void testConfigurationInvalidTestSpecification() {
        ConfigEditorResult ret = configTester.testConfigurations(testConfigs, "INVALID");
        Assert.assertEquals(ConfigEditorResult.StatusCode.BAD_REQUEST, ret.getStatusCode());
        Assert.assertNotNull(ret.getAttributes().getMessage());
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

    @Test
    public void getTesterInfo() {
        var ret = configTester.getConfigTesterInfo();
        Assert.assertEquals(ConfigTester.DEFAULT_NAME, ret.getName());
        Assert.assertNotNull(ret.getTestSchema());
        Assert.assertTrue(ret.isConfigTesting());
        Assert.assertFalse(ret.isReleaseTesting());
        Assert.assertTrue(ret.isTestCaseTesting());
        Assert.assertFalse(ret.isIncompleteResult());
    }
}
