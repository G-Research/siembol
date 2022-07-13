package uk.co.gresearch.siembol.configeditor.service.parserconfig;

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
        Assert.assertEquals(ConfigEditorResult.StatusCode.BAD_REQUEST, ret.getStatusCode());
        Assert.assertEquals("error", ret.getAttributes().getMessage());
    }

    @Test
    public void validateRuleError() {
        parserFactoryAttributes.setMessage("error");

        parserFactoryResult = new ParserFactoryResult(ParserFactoryResult.StatusCode.ERROR, parserFactoryAttributes);
        Mockito.when(parserFactory.validateConfiguration(anyString())).thenReturn(parserFactoryResult);
        ConfigEditorResult ret = parserConfigSchemaService.validateConfiguration(testConfig);
        Mockito.verify(parserFactory, times(1)).validateConfiguration(testConfig);
        Assert.assertEquals(ConfigEditorResult.StatusCode.BAD_REQUEST, ret.getStatusCode());
        Assert.assertEquals("error", ret.getAttributes().getMessage());
    }

    @Test
    public void getAdminConfigSchemaError() throws Exception {
        ConfigSchemaService service = ParserConfigSchemaService
                .createParserConfigSchemaService(new ConfigEditorUiLayout());
        ConfigEditorResult ret = service.getAdminConfigurationSchema();
        Assert.assertEquals(ConfigEditorResult.StatusCode.ERROR, ret.getStatusCode());
    }

    @Test
    public void validateAdminConfigError() throws Exception {
        ConfigSchemaService service = ParserConfigSchemaService
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
