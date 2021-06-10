package uk.co.gresearch.siembol.configeditor.service.parserconfig;

import org.adrianwalker.multilinestring.Multiline;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import uk.co.gresearch.siembol.configeditor.common.ConfigSchemaService;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorUiLayout;
import uk.co.gresearch.siembol.configeditor.service.common.ConfigSchemaServiceContext;
import uk.co.gresearch.siembol.parsers.common.ParserResult;
import uk.co.gresearch.siembol.parsers.factory.ParserFactory;
import uk.co.gresearch.siembol.parsers.factory.ParserFactoryAttributes;
import uk.co.gresearch.siembol.parsers.factory.ParserFactoryResult;

import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;

public class ParserConfigSchemaServiceTest {
    /**
     * {
     *   "encoding" : "utf8_string",
     *   "log" : "dummy log"
     * }
     **/
    @Multiline
    public static String logUtf8;

    /**
     * {
     *   "encoding" : "hex_string",
     *   "log" : "64756D6D79206C6F67"
     * }
     **/
    @Multiline
    public static String logHex;

    private ParserConfigSchemaService parserConfigSchemaService;
    private final String schema = "dummmy schema";
    private final String testConfig = "dummmy parser config";
    private final String testConfigs = "dummmy parser configs";
    private final String testLog = "dummy log";

    private ParserFactory parserFactory;
    private ParserFactoryResult parserFactoryResult;
    private ParserFactoryAttributes parserFactoryAttributes;
    private ParserResult parserResult;
    private ConfigSchemaServiceContext context;

    @Before
    public void setup() throws Exception {
        context = new ConfigSchemaServiceContext();
        context.setConfigSchema(schema);
        context.setTestSchema(schema);
        parserFactory = Mockito.mock(ParserFactory.class);
        this.parserConfigSchemaService = new ParserConfigSchemaService(parserFactory, context);
        parserFactoryAttributes = new ParserFactoryAttributes();
        parserFactoryResult = new ParserFactoryResult(ParserFactoryResult.StatusCode.OK, parserFactoryAttributes);
        parserResult = new ParserResult();
        Mockito.when(parserFactory.create(anyString())).thenReturn(parserFactoryResult);

        Mockito.when(parserFactory.validateConfiguration(anyString())).thenReturn(parserFactoryResult);
        Mockito.when(parserFactory.validateConfigurations(anyString())).thenReturn(parserFactoryResult);
        Mockito.when(parserFactory.test(anyString(), eq(null), ArgumentMatchers.<byte[]>any())).thenReturn(parserFactoryResult);
    }

    @Test
    public void getSchemaOK() {
        ConfigEditorResult ret = parserConfigSchemaService.getSchema();
        Assert.assertEquals(ConfigEditorResult.StatusCode.OK, ret.getStatusCode());
        Assert.assertEquals(schema, ret.getAttributes().getRulesSchema());
    }

    @Test
    public void getTestSchemaOK() {
        ConfigEditorResult ret = parserConfigSchemaService.getTestSchema();
        Assert.assertEquals(ConfigEditorResult.StatusCode.OK, ret.getStatusCode());
        Assert.assertNotNull(ret.getAttributes().getTestSchema());
    }

    @Test
    public void validateConfigurationsOK() {
        ConfigEditorResult ret = parserConfigSchemaService.validateConfigurations(testConfigs);
        Mockito.verify(parserFactory, times(1)).validateConfigurations(testConfigs);
        Assert.assertEquals(ConfigEditorResult.StatusCode.OK, ret.getStatusCode());
    }

    @Test
    public void validateConfigurationsError() {
        ConfigEditorResult ret = parserConfigSchemaService.validateConfigurations(testConfigs);
        Mockito.verify(parserFactory, times(1)).validateConfigurations(testConfigs);
        Assert.assertEquals(ConfigEditorResult.StatusCode.OK, ret.getStatusCode());
    }

    @Test
    public void validateConfigurationOK() {
        ConfigEditorResult ret = parserConfigSchemaService.validateConfiguration(testConfig);
        Mockito.verify(parserFactory, times(1)).validateConfiguration(testConfig);
        Assert.assertEquals(ConfigEditorResult.StatusCode.OK, ret.getStatusCode());
    }

    @Test
    public void validateRulesError() {
        parserFactoryAttributes.setMessage("error");
        parserFactoryResult = new ParserFactoryResult(ParserFactoryResult.StatusCode.ERROR, parserFactoryAttributes);
        Mockito.when(parserFactory.validateConfigurations(anyString())).thenReturn(parserFactoryResult);
        ConfigEditorResult ret = parserConfigSchemaService.validateConfigurations(testConfigs);
        Mockito.verify(parserFactory, times(1)).validateConfigurations(testConfigs);
        Assert.assertEquals(ConfigEditorResult.StatusCode.ERROR, ret.getStatusCode());
        Assert.assertEquals("error", ret.getAttributes().getMessage());
    }

    @Test
    public void validateRuleError() {
        parserFactoryAttributes.setMessage("error");
        
        parserFactoryResult = new ParserFactoryResult(ParserFactoryResult.StatusCode.ERROR, parserFactoryAttributes);
        Mockito.when(parserFactory.validateConfiguration(anyString())).thenReturn(parserFactoryResult);
        ConfigEditorResult ret = parserConfigSchemaService.validateConfiguration(testConfig);
        Mockito.verify(parserFactory, times(1)).validateConfiguration(testConfig);
        Assert.assertEquals(ConfigEditorResult.StatusCode.ERROR, ret.getStatusCode());
        Assert.assertEquals("error", ret.getAttributes().getMessage());
    }

    @Test
    public void testConfigurationOK() {
        parserResult.setParsedMessages(new ArrayList<>());
        parserFactoryAttributes.setParserResult(parserResult);
        ConfigEditorResult ret = parserConfigSchemaService.testConfiguration(testConfig, logUtf8);
        Mockito.verify(parserFactory, times(1)).test(testConfig, null, testLog.getBytes());
        Assert.assertEquals(ConfigEditorResult.StatusCode.OK, ret.getStatusCode());
        Assert.assertTrue(ret.getAttributes().getTestResultComplete());
    }

    @Test
    public void testConfigurationHexOK() {
        parserResult.setParsedMessages(new ArrayList<>());
        parserFactoryAttributes.setParserResult(parserResult);
        ConfigEditorResult ret = parserConfigSchemaService.testConfiguration(testConfig, logHex);
        Mockito.verify(parserFactory, times(1)).test(testConfig,
                null,
                testLog.getBytes());
        Assert.assertEquals(ConfigEditorResult.StatusCode.OK, ret.getStatusCode());
        Assert.assertTrue(ret.getAttributes().getTestResultComplete());
    }

    @Test
    public void testConfigurationHexError() {
        parserResult.setParsedMessages(new ArrayList<>());
        parserFactoryAttributes.setParserResult(parserResult);
        ConfigEditorResult ret = parserConfigSchemaService.testConfiguration(testConfig,
                logHex.replace("64", "XX"));
        Assert.assertEquals(ConfigEditorResult.StatusCode.ERROR, ret.getStatusCode());
        Assert.assertTrue(ret.getAttributes().getException().contains("Unrecognized character: X"));
    }

    @Test
    public void testConfigurationsError() {
        parserFactoryAttributes.setMessage("error");
        parserFactoryResult = new ParserFactoryResult(ParserFactoryResult.StatusCode.ERROR, parserFactoryAttributes);
        Mockito.when(parserFactory.test(testConfigs, null, testLog.getBytes())).thenReturn(parserFactoryResult);
        ConfigEditorResult ret = parserConfigSchemaService.testConfigurations(testConfigs, testLog);
        Assert.assertEquals(ConfigEditorResult.StatusCode.ERROR, ret.getStatusCode());
        Assert.assertEquals("Not implemented", ret.getAttributes().getMessage());
    }

    @Test
    public void testConfigurationError() {
        parserFactoryAttributes.setMessage("error");
        parserFactoryResult = new ParserFactoryResult(ParserFactoryResult.StatusCode.ERROR, parserFactoryAttributes);
        Mockito.when(parserFactory.test(testConfig, null, testLog.getBytes())).thenReturn(parserFactoryResult);
        ConfigEditorResult ret = parserConfigSchemaService.testConfiguration(testConfig, logUtf8);
        Mockito.verify(parserFactory, times(1)).test(testConfig,
                null,
                testLog.getBytes());
        Assert.assertEquals(ConfigEditorResult.StatusCode.ERROR, ret.getStatusCode());
        Assert.assertEquals("error", ret.getAttributes().getMessage());
    }

    @Test
    public void testConfigurationParserResultError() {
        parserResult.setException(new IllegalStateException("dummy"));
        parserFactoryAttributes.setParserResult(parserResult);

        parserFactoryResult = new ParserFactoryResult(ParserFactoryResult.StatusCode.OK, parserFactoryAttributes);
        Mockito.when(parserFactory.test(testConfig, null, testLog.getBytes())).thenReturn(parserFactoryResult);
        ConfigEditorResult ret = parserConfigSchemaService.testConfiguration(testConfig, logUtf8);
        Mockito.verify(parserFactory, times(1)).test(testConfig,
                null,
                testLog.getBytes());
        Assert.assertEquals(ConfigEditorResult.StatusCode.OK, ret.getStatusCode());
        Assert.assertTrue(ret.getAttributes().getTestResultComplete());
        Assert.assertTrue(ret.getAttributes().getTestResultOutput().contains("dummy"));
        Assert.assertTrue(ret.getAttributes().getTestResultRawOutput().contains("dummy"));
    }

    @Test
    public void createSchemaEmptyTestConfigs() throws Exception {
        ConfigSchemaService service  = ParserConfigSchemaService
                .createParserConfigSchemaService(new ConfigEditorUiLayout());
        Assert.assertNotNull(service.getSchema());
        Assert.assertNotNull(service.getTestSchema());
    }

    @Test
    public void createSchemaConfigs() throws Exception {
        ConfigSchemaService service  = ParserConfigSchemaService
                .createParserConfigSchemaService(new ConfigEditorUiLayout());
        Assert.assertNotNull(service.getSchema());
        Assert.assertNotNull(service.getTestSchema());
    }

    @Test
    public void getAdminConfigSchemaError() throws Exception {
        ConfigSchemaService service  = ParserConfigSchemaService
                .createParserConfigSchemaService(new ConfigEditorUiLayout());
        ConfigEditorResult ret = service.getAdminConfigurationSchema();
        Assert.assertEquals(ConfigEditorResult.StatusCode.ERROR, ret.getStatusCode());
    }

    @Test
    public void validateAdminConfigError() throws Exception {
        ConfigSchemaService service  = ParserConfigSchemaService
                .createParserConfigSchemaService(new ConfigEditorUiLayout());
        ConfigEditorResult ret = service.getAdminConfigurationSchema();
        Assert.assertEquals(ConfigEditorResult.StatusCode.ERROR, ret.getStatusCode());
    }

    @Test
    public void getImportersEmpty() {
        ConfigEditorResult ret = parserConfigSchemaService.getImporters();
        Assert.assertEquals(ConfigEditorResult.StatusCode.OK, ret.getStatusCode());
        Assert.assertNotNull(ret.getAttributes().getConfigImporters());
        Assert.assertTrue(ret.getAttributes().getConfigImporters().isEmpty());
    }
}
