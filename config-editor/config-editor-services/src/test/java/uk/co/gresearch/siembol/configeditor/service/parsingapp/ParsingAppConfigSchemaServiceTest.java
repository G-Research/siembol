package uk.co.gresearch.siembol.configeditor.service.parsingapp;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.internal.verification.VerificationModeFactory;
import uk.co.gresearch.siembol.common.jsonschema.SiembolJsonSchemaValidator;
import uk.co.gresearch.siembol.common.result.SiembolAttributes;
import uk.co.gresearch.siembol.common.result.SiembolResult;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult;
import uk.co.gresearch.siembol.configeditor.service.common.ConfigSchemaServiceContext;
import uk.co.gresearch.siembol.parsers.application.factory.ParsingApplicationFactory;
import uk.co.gresearch.siembol.parsers.application.factory.ParsingApplicationFactoryAttributes;
import uk.co.gresearch.siembol.parsers.application.factory.ParsingApplicationFactoryResult;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.co.gresearch.siembol.parsers.application.factory.ParsingApplicationFactoryResult.StatusCode.ERROR;
import static uk.co.gresearch.siembol.parsers.application.factory.ParsingApplicationFactoryResult.StatusCode.OK;

public class ParsingAppConfigSchemaServiceTest {
    private ParsingAppConfigSchemaService parsingAppConfigSchemaService;
    private final String schema = "dummmy schema";
    private final String adminSchema = "admin schema";
    private final String testConfig = "dummmy config";
    private final String testConfigs = "dummmy configs";

    private ParsingApplicationFactory parsingAppFactory;
    private ParsingApplicationFactoryResult factoryResult;
    private ParsingApplicationFactoryAttributes factoryAttributes;
    private ConfigSchemaServiceContext context;
    private SiembolJsonSchemaValidator adminConfigValidator;
    private SiembolResult validationResult;

    @Before
    public void setUp() {
        context = new ConfigSchemaServiceContext();
        context.setConfigSchema(schema);
        parsingAppFactory = Mockito.mock(ParsingApplicationFactory.class);
        adminConfigValidator = Mockito.mock(SiembolJsonSchemaValidator.class);
        validationResult = new SiembolResult(SiembolResult.StatusCode.OK, new SiembolAttributes());
        when(adminConfigValidator.validate(eq(testConfig))).thenReturn(validationResult);

        context.setAdminConfigSchema(adminSchema);
        context.setAdminConfigValidator(adminConfigValidator);
        this.parsingAppConfigSchemaService = new ParsingAppConfigSchemaService(parsingAppFactory, context);
        factoryAttributes = new ParsingApplicationFactoryAttributes();
        factoryResult = new ParsingApplicationFactoryResult(OK, factoryAttributes);
        Mockito.when(parsingAppFactory.validateConfiguration(anyString())).thenReturn(factoryResult);
        Mockito.when(parsingAppFactory.validateConfigurations(anyString())).thenReturn(factoryResult);
    }

    @Test
    public void getSchemaOK() {
        factoryAttributes.setJsonSchema(schema);
        ConfigEditorResult ret = parsingAppConfigSchemaService.getSchema();
        Assert.assertEquals(ConfigEditorResult.StatusCode.OK, ret.getStatusCode());
        Assert.assertEquals(schema, ret.getAttributes().getRulesSchema());
    }

    @Test
    public void validateConfigurationsOK() {
        ConfigEditorResult ret = parsingAppConfigSchemaService.validateConfigurations(testConfigs);
        Mockito.verify(parsingAppFactory, times(1)).validateConfigurations(testConfigs);
        Assert.assertEquals(ConfigEditorResult.StatusCode.OK, ret.getStatusCode());
    }

    @Test
    public void validateConfigurationsError() {
        ConfigEditorResult ret = parsingAppConfigSchemaService.validateConfigurations(testConfigs);
        Mockito.verify(parsingAppFactory, times(1)).validateConfigurations(testConfigs);
        Assert.assertEquals(ConfigEditorResult.StatusCode.OK, ret.getStatusCode());
    }

    @Test
    public void validateConfigurationOK() {
        ConfigEditorResult ret = parsingAppConfigSchemaService.validateConfiguration(testConfig);
        Mockito.verify(parsingAppFactory, times(1)).validateConfiguration(testConfig);
        Assert.assertEquals(ConfigEditorResult.StatusCode.OK, ret.getStatusCode());
    }

    @Test
    public void validateConfigsError() {
        factoryAttributes.setMessage("error");
        factoryResult = new ParsingApplicationFactoryResult(ERROR, factoryAttributes);
        Mockito.when(parsingAppFactory.validateConfigurations(anyString())).thenReturn(factoryResult);
        ConfigEditorResult ret = parsingAppConfigSchemaService.validateConfigurations(testConfigs);
        Mockito.verify(parsingAppFactory, times(1)).validateConfigurations(testConfigs);
        Assert.assertEquals(ConfigEditorResult.StatusCode.ERROR, ret.getStatusCode());
        Assert.assertEquals("error", ret.getAttributes().getMessage());
    }

    @Test
    public void validateCongfigError() {
        factoryAttributes.setMessage("error");
        factoryResult = new ParsingApplicationFactoryResult(ERROR, factoryAttributes);
        Mockito.when(parsingAppFactory.validateConfiguration(anyString())).thenReturn(factoryResult);
        ConfigEditorResult ret = parsingAppConfigSchemaService.validateConfiguration(testConfig);
        Mockito.verify(parsingAppFactory, times(1)).validateConfiguration(testConfig);
        Assert.assertEquals(ConfigEditorResult.StatusCode.ERROR, ret.getStatusCode());
        Assert.assertEquals("error", ret.getAttributes().getMessage());
    }

    @Test
    public void getAdminConfigSchemaOK() {
        ConfigEditorResult ret = parsingAppConfigSchemaService.getAdminConfigurationSchema();
        Assert.assertEquals(ConfigEditorResult.StatusCode.OK, ret.getStatusCode());
        Assert.assertEquals(adminSchema, ret.getAttributes().getAdminConfigSchema());
    }

    @Test
    public void validateAdminConfigOK() {
        ConfigEditorResult ret = parsingAppConfigSchemaService.validateAdminConfiguration(testConfig);
        Assert.assertEquals(ConfigEditorResult.StatusCode.OK, ret.getStatusCode());
        verify(adminConfigValidator, VerificationModeFactory.times(1)).validate(testConfig);
    }

    @Test
    public void validateAdminConfigInvalid() {
        when(adminConfigValidator.validate(eq(testConfig)))
                .thenReturn(new SiembolResult(SiembolResult.StatusCode.ERROR, new SiembolAttributes()));
        ConfigEditorResult ret = parsingAppConfigSchemaService.validateAdminConfiguration(testConfig);
        Assert.assertEquals(ConfigEditorResult.StatusCode.BAD_REQUEST, ret.getStatusCode());
        verify(adminConfigValidator, VerificationModeFactory.times(1)).validate(testConfig);
    }

    @Test
    public void getImportersEmpty() throws Exception {
        ConfigEditorResult ret = parsingAppConfigSchemaService.getImporters();
        Assert.assertEquals(ConfigEditorResult.StatusCode.OK, ret.getStatusCode());
        Assert.assertNotNull(ret.getAttributes().getConfigImporters());
        Assert.assertTrue(ret.getAttributes().getConfigImporters().isEmpty());
    }
}
