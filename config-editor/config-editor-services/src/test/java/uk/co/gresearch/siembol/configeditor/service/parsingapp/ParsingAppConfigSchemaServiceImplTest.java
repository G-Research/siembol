package uk.co.gresearch.siembol.configeditor.service.parsingapp;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult;
import uk.co.gresearch.siembol.parsers.application.factory.ParsingApplicationFactory;
import uk.co.gresearch.siembol.parsers.application.factory.ParsingApplicationFactoryAttributes;
import uk.co.gresearch.siembol.parsers.application.factory.ParsingApplicationFactoryResult;
import uk.co.gresearch.siembol.parsers.common.ParserResult;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static uk.co.gresearch.siembol.parsers.application.factory.ParsingApplicationFactoryResult.StatusCode.ERROR;
import static uk.co.gresearch.siembol.parsers.application.factory.ParsingApplicationFactoryResult.StatusCode.OK;

public class ParsingAppConfigSchemaServiceImplTest {
    private ParsingAppConfigSchemaServiceImpl parserConfigSchemaService;
    private final String schema = "dummmy schema";
    private final String testConfig = "dummmy config";
    private final String testConfigs = "dummmy configs";

    private ParsingApplicationFactory parsingAppFactory;
    private ParsingApplicationFactoryResult factoryResult;
    private ParsingApplicationFactoryAttributes factoryAttributes;
    private ParserResult parsingAppResult;

    @Before
    public void Setup() {
        parsingAppFactory = Mockito.mock(ParsingApplicationFactory.class);
        this.parserConfigSchemaService = new ParsingAppConfigSchemaServiceImpl(parsingAppFactory, schema);
        factoryAttributes = new ParsingApplicationFactoryAttributes();
        factoryResult = new ParsingApplicationFactoryResult(OK, factoryAttributes);
        parsingAppResult = new ParserResult();
        Mockito.when(parsingAppFactory.validateConfiguration(any())).thenReturn(factoryResult);
        Mockito.when(parsingAppFactory.validateConfigurations(any())).thenReturn(factoryResult);
    }

    @Test
    public void getSchemaOK() {
        factoryAttributes.setJsonSchema(schema);
        ConfigEditorResult ret = parserConfigSchemaService.getSchema();
        Assert.assertEquals(ConfigEditorResult.StatusCode.OK, ret.getStatusCode());
        Assert.assertEquals(schema, ret.getAttributes().getRulesSchema());
    }

    @Test
    public void validateConfigurationsOK() {
        ConfigEditorResult ret = parserConfigSchemaService.validateConfigurations(testConfigs);
        Mockito.verify(parsingAppFactory, times(1)).validateConfigurations(testConfigs);
        Assert.assertEquals(ConfigEditorResult.StatusCode.OK, ret.getStatusCode());
    }

    @Test
    public void validateConfigurationsError() {
        ConfigEditorResult ret = parserConfigSchemaService.validateConfigurations(testConfigs);
        Mockito.verify(parsingAppFactory, times(1)).validateConfigurations(testConfigs);
        Assert.assertEquals(ret.getStatusCode(), ConfigEditorResult.StatusCode.OK);
    }

    @Test
    public void validateConfigurationOK() {
        ConfigEditorResult ret = parserConfigSchemaService.validateConfiguration(testConfig);
        Mockito.verify(parsingAppFactory, times(1)).validateConfiguration(testConfig);
        Assert.assertEquals(ConfigEditorResult.StatusCode.OK, ret.getStatusCode());
    }

    @Test
    public void ValidateConfigsError() {
        factoryAttributes.setMessage("error");
        factoryResult = new ParsingApplicationFactoryResult(ERROR, factoryAttributes);
        Mockito.when(parsingAppFactory.validateConfigurations(any())).thenReturn(factoryResult);
        ConfigEditorResult ret = parserConfigSchemaService.validateConfigurations(testConfigs);
        Mockito.verify(parsingAppFactory, times(1)).validateConfigurations(testConfigs);
        Assert.assertEquals(ret.getStatusCode(), ConfigEditorResult.StatusCode.ERROR);
        Assert.assertEquals(ret.getAttributes().getMessage(), "error");
    }

    @Test
    public void ValidateCongError() {
        factoryAttributes.setMessage("error");
        factoryResult = new ParsingApplicationFactoryResult(ERROR, factoryAttributes);
        Mockito.when(parsingAppFactory.validateConfiguration(any())).thenReturn(factoryResult);
        ConfigEditorResult ret = parserConfigSchemaService.validateConfiguration(testConfig);
        Mockito.verify(parsingAppFactory, times(1)).validateConfiguration(testConfig);
        Assert.assertEquals(ret.getStatusCode(), ConfigEditorResult.StatusCode.ERROR);
        Assert.assertEquals(ret.getAttributes().getMessage(), "error");
    }
}
