package uk.co.gresearch.siembol.configeditor.common;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.boot.actuate.health.Health;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorAttributes;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult;

import static org.mockito.Mockito.*;

public class ConfigSchemaServiceWithErrorMessageTest {
    private ConfigSchemaService service;
    private ConfigSchemaService serviceWithErrorMessage;
    private ConfigEditorResult result;
    private ConfigEditorAttributes attributes;
    private final String configuration = "dummy config";
    private final String configurations = "dummy configs";
    private UserInfo user;

    @Before
    public void setUp() {
        service = Mockito.mock(ConfigSchemaService.class);
        when(service.withErrorMessage()).thenCallRealMethod();
        serviceWithErrorMessage = service.withErrorMessage();
        attributes = new ConfigEditorAttributes();
        result = new ConfigEditorResult(ConfigEditorResult.StatusCode.OK, attributes);
        user = Mockito.mock(UserInfo.class);
    }

    @Test
    public void getSchemaOk() {
        when(service.getSchema()).thenReturn(result);
        var ret = serviceWithErrorMessage.getSchema();
        Assert.assertEquals(ret, result);
        verify(service, times(1)).getSchema();
    }

    @Test
    public void getAdminConfigSchemaOk() {
        when(service.getAdminConfigurationSchema()).thenReturn(result);
        var ret = serviceWithErrorMessage.getAdminConfigurationSchema();
        Assert.assertEquals(ret, result);
        verify(service, times(1)).getAdminConfigurationSchema();
    }

    @Test
    public void getImportersOk() {
        when(service.getImporters()).thenReturn(result);
        var ret = serviceWithErrorMessage.getImporters();
        Assert.assertEquals(result, ret);
        verify(service, times(1)).getImporters();
    }

    @Test
    public void getConfigTestersOk() {
        when(service.getConfigTesters()).thenReturn(result);
        var ret = serviceWithErrorMessage.getConfigTesters();
        Assert.assertEquals(result, ret);
        verify(service, times(1)).getConfigTesters();
    }

    @Test
    public void getConfigTesterOk() {
        ConfigTester tester = Mockito.mock(ConfigTester.class);
        when(service.getConfigTester(eq("dummy"))).thenReturn(tester);
        var ret = serviceWithErrorMessage.getConfigTester("dummy");
        Assert.assertEquals(tester, ret);
        verify(service, times(1)).getConfigTester(eq("dummy"));
    }

    @Test
    public void checkHealthOk() {
        var health = Health.up().build();
        when(service.checkHealth()).thenReturn(health);
        var ret = serviceWithErrorMessage.checkHealth();
        Assert.assertEquals(health, ret);
        verify(service, times(1)).checkHealth();
    }

    @Test
    public void validateConfigurationOk() {
        when(service.validateConfiguration(eq(configuration))).thenReturn(result);
        var ret = serviceWithErrorMessage.validateConfiguration(configuration);
        Assert.assertEquals(ret, result);
        verify(service, times(1)).validateConfiguration(eq(configuration));
    }

    @Test
    public void validateConfigurationBadRequest() {
        result = new ConfigEditorResult(ConfigEditorResult.StatusCode.BAD_REQUEST, attributes);

        when(service.validateConfiguration(eq(configuration))).thenReturn(result);
        var ret = serviceWithErrorMessage.validateConfiguration(configuration);
        Assert.assertEquals(ConfigEditorResult.StatusCode.BAD_REQUEST, ret.getStatusCode());
        verify(service, times(1)).validateConfiguration(eq(configuration));
        Assert.assertNotNull(ret.getAttributes().getMessage());
        Assert.assertNotNull(ret.getAttributes().getErrorResolution());
        Assert.assertNotNull(ret.getAttributes().getErrorTitle());
    }

    @Test
    public void validateAdminConfigurationOk() {
        when(service.validateAdminConfiguration(eq(configuration))).thenReturn(result);
        var ret = serviceWithErrorMessage.validateAdminConfiguration(configuration);
        Assert.assertEquals(ret, result);
        verify(service, times(1)).validateAdminConfiguration(eq(configuration));
    }

    @Test
    public void validateAdminConfigurationBadRequest() {
        result = new ConfigEditorResult(ConfigEditorResult.StatusCode.BAD_REQUEST, attributes);

        when(service.validateAdminConfiguration(eq(configuration))).thenReturn(result);
        var ret = serviceWithErrorMessage.validateAdminConfiguration(configuration);
        Assert.assertEquals(ConfigEditorResult.StatusCode.BAD_REQUEST, ret.getStatusCode());
        verify(service, times(1)).validateAdminConfiguration(eq(configuration));
        Assert.assertNotNull(ret.getAttributes().getMessage());
        Assert.assertNotNull(ret.getAttributes().getErrorResolution());
        Assert.assertNotNull(ret.getAttributes().getErrorTitle());
    }

    @Test
    public void validateConfigurationsOk() {
        when(service.validateConfigurations(eq(configurations))).thenReturn(result);
        var ret = serviceWithErrorMessage.validateConfigurations(configurations);
        Assert.assertEquals(ret, result);
        verify(service, times(1)).validateConfigurations(eq(configurations));
    }

    @Test
    public void validateConfigurationsBadRequest() {
        result = new ConfigEditorResult(ConfigEditorResult.StatusCode.BAD_REQUEST, attributes);

        when(service.validateConfigurations(eq(configurations))).thenReturn(result);
        var ret = serviceWithErrorMessage.validateConfigurations(configurations);
        Assert.assertEquals(ConfigEditorResult.StatusCode.BAD_REQUEST, ret.getStatusCode());
        verify(service, times(1)).validateConfigurations(eq(configurations));
        Assert.assertNotNull(ret.getAttributes().getMessage());
        Assert.assertNotNull(ret.getAttributes().getErrorResolution());
        Assert.assertNotNull(ret.getAttributes().getErrorTitle());
    }

    @Test
    public void testImportConfigurationsOk() {
        when(service.importConfig(eq(user), eq("a"), eq("b"), eq("c"))).thenReturn(result);
        var ret = serviceWithErrorMessage
                .importConfig(user, "a", "b", "c");
        Assert.assertEquals(ret, result);
        verify(service, times(1))
                .importConfig(eq(user), eq("a"), eq("b"), eq("c"));
    }

    @Test
    public void testImportConfigurationsBadRequest() {
        result = new ConfigEditorResult(ConfigEditorResult.StatusCode.BAD_REQUEST, attributes);
        when(service.importConfig(eq(user), eq("a"), eq("b"), eq("c"))).thenReturn(result);
        var ret = serviceWithErrorMessage
                .importConfig(user, "a", "b", "c");
        Assert.assertNotNull(ret.getAttributes().getMessage());
        Assert.assertNotNull(ret.getAttributes().getErrorResolution());
        Assert.assertNotNull(ret.getAttributes().getErrorTitle());

        verify(service, times(1))
                .importConfig(eq(user), eq("a"), eq("b"), eq("c"));
    }
}
