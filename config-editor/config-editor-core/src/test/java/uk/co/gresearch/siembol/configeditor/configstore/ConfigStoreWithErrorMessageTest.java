package uk.co.gresearch.siembol.configeditor.configstore;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.boot.actuate.health.Health;
import uk.co.gresearch.siembol.configeditor.common.UserInfo;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorAttributes;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult;

import static org.mockito.Mockito.*;

public class ConfigStoreWithErrorMessageTest {
    private ConfigStore service;
    private ConfigStore serviceWithErrorMessage;
    private ConfigEditorResult result;
    private ConfigEditorAttributes attributes;
    private UserInfo user;

    @Before
    public void setUp() {
        service = Mockito.mock(ConfigStore.class);
        when(service.withErrorMessage()).thenCallRealMethod();
        serviceWithErrorMessage = service.withErrorMessage();
        attributes = new ConfigEditorAttributes();
        result = new ConfigEditorResult(ConfigEditorResult.StatusCode.OK, attributes);
        user = Mockito.mock(UserInfo.class);
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
    public void getConfigs() {
        when(service.getConfigs()).thenReturn(result);
        var ret = serviceWithErrorMessage.getConfigs();
        Assert.assertEquals(ret, result);
        verify(service, times(1)).getConfigs();
    }

    @Test
    public void getTestCases() {
        when(service.getTestCases()).thenReturn(result);
        var ret = serviceWithErrorMessage.getTestCases();
        Assert.assertEquals(ret, result);
        verify(service, times(1)).getTestCases();
    }

    @Test
    public void getAdminConfig() {
        when(service.getAdminConfig()).thenReturn(result);
        var ret = serviceWithErrorMessage.getAdminConfig();
        Assert.assertEquals(ret, result);
        verify(service, times(1)).getAdminConfig();
    }

    @Test
    public void getAdminConfigFromCache() {
        when(service.getAdminConfigFromCache()).thenReturn(result);
        var ret = serviceWithErrorMessage.getAdminConfigFromCache();
        Assert.assertEquals(ret, result);
        verify(service, times(1)).getAdminConfigFromCache();
    }

    @Test
    public void getAdminConfigStatus() {
        when(service.getAdminConfigStatus()).thenReturn(result);
        var ret = serviceWithErrorMessage.getAdminConfigStatus();
        Assert.assertEquals(ret, result);
        verify(service, times(1)).getAdminConfigStatus();
    }

    @Test
    public void getRepositories() {
        when(service.getRepositories()).thenReturn(result);
        var ret = serviceWithErrorMessage.getRepositories();
        Assert.assertEquals(ret, result);
        verify(service, times(1)).getRepositories();
    }

    @Test
    public void getConfigsRelease() {
        when(service.getConfigsRelease()).thenReturn(result);
        var ret = serviceWithErrorMessage.getConfigsRelease();
        Assert.assertEquals(ret, result);
        verify(service, times(1)).getConfigsRelease();
    }

    @Test
    public void getConfigsReleaseStatus() {
        when(service.getConfigsReleaseStatus()).thenReturn(result);
        var ret = serviceWithErrorMessage.getConfigsReleaseStatus();
        Assert.assertEquals(ret, result);
        verify(service, times(1)).getConfigsReleaseStatus();
    }

    @Test
    public void getConfigsReleaseFromCache() {
        when(service.getConfigsReleaseFromCache()).thenReturn(result);
        var ret = serviceWithErrorMessage.getConfigsReleaseFromCache();
        Assert.assertEquals(ret, result);
        verify(service, times(1)).getConfigsReleaseFromCache();
    }

    @Test
    public void addTestCaseOk() {
        when(service.addTestCase(eq(user), eq("a"))).thenReturn(result);
        var ret = serviceWithErrorMessage.addTestCase(user, "a");
        Assert.assertEquals(ret, result);
        verify(service, times(1)).addTestCase(eq(user), eq("a"));
    }

    @Test
    public void addTestCaseBadRequest() {
        result = new ConfigEditorResult(ConfigEditorResult.StatusCode.BAD_REQUEST, attributes);

        when(service.addTestCase(eq(user), eq("a"))).thenReturn(result);
        var ret = serviceWithErrorMessage.addTestCase(user, "a");
        Assert.assertEquals(ConfigEditorResult.StatusCode.BAD_REQUEST, ret.getStatusCode());
        verify(service, times(1)).addTestCase(eq(user), eq("a"));
        Assert.assertNotNull(ret.getAttributes().getMessage());
        Assert.assertNotNull(ret.getAttributes().getErrorResolution());
        Assert.assertNotNull(ret.getAttributes().getErrorTitle());
    }

    @Test
    public void updateTestCaseOk() {
        when(service.updateTestCase(eq(user), eq("a"))).thenReturn(result);
        var ret = serviceWithErrorMessage.updateTestCase(user, "a");
        Assert.assertEquals(ret, result);
        verify(service, times(1)).updateTestCase(eq(user), eq("a"));
    }

    @Test
    public void updateTestCaseBadRequest() {
        result = new ConfigEditorResult(ConfigEditorResult.StatusCode.BAD_REQUEST, attributes);

        when(service.updateTestCase(eq(user), eq("a"))).thenReturn(result);
        var ret = serviceWithErrorMessage.updateTestCase(user, "a");
        Assert.assertEquals(ConfigEditorResult.StatusCode.BAD_REQUEST, ret.getStatusCode());
        verify(service, times(1)).updateTestCase(eq(user), eq("a"));
        Assert.assertNotNull(ret.getAttributes().getMessage());
        Assert.assertNotNull(ret.getAttributes().getErrorResolution());
        Assert.assertNotNull(ret.getAttributes().getErrorTitle());
    }

    @Test
    public void deleteTestCaseOk() {
        when(service.deleteTestCase(eq(user), eq("a"), eq("b"))).thenReturn(result);
        var ret = serviceWithErrorMessage.deleteTestCase(user, "a", "b");
        Assert.assertEquals(ret, result);
        verify(service, times(1)).deleteTestCase(eq(user), eq("a"), eq("b"));
    }

    @Test
    public void deleteTestCaseBadRequest() {
        result = new ConfigEditorResult(ConfigEditorResult.StatusCode.BAD_REQUEST, attributes);

        when(service.deleteTestCase(eq(user), eq("a"), eq("b"))).thenReturn(result);
        var ret = serviceWithErrorMessage.deleteTestCase(user, "a", "b");
        Assert.assertEquals(ConfigEditorResult.StatusCode.BAD_REQUEST, ret.getStatusCode());
        verify(service, times(1)).deleteTestCase(eq(user), eq("a"), eq("b"));
        Assert.assertNotNull(ret.getAttributes().getMessage());
        Assert.assertNotNull(ret.getAttributes().getErrorResolution());
        Assert.assertNotNull(ret.getAttributes().getErrorTitle());
    }

    @Test
    public void addConfigOk() {
        when(service.addConfig(eq(user), eq("a"))).thenReturn(result);
        var ret = serviceWithErrorMessage.addConfig(user, "a");
        Assert.assertEquals(ret, result);
        verify(service, times(1)).addConfig(eq(user), eq("a"));
    }

    @Test
    public void addConfigBadRequest() {
        result = new ConfigEditorResult(ConfigEditorResult.StatusCode.BAD_REQUEST, attributes);

        when(service.addConfig(eq(user), eq("a"))).thenReturn(result);
        var ret = serviceWithErrorMessage.addConfig(user, "a");
        Assert.assertEquals(ConfigEditorResult.StatusCode.BAD_REQUEST, ret.getStatusCode());
        verify(service, times(1)).addConfig(eq(user), eq("a"));
        Assert.assertNotNull(ret.getAttributes().getMessage());
        Assert.assertNotNull(ret.getAttributes().getErrorResolution());
        Assert.assertNotNull(ret.getAttributes().getErrorTitle());
    }

    @Test
    public void updateConfigOk() {
        when(service.updateConfig(eq(user), eq("a"))).thenReturn(result);
        var ret = serviceWithErrorMessage.updateConfig(user, "a");
        Assert.assertEquals(ret, result);
        verify(service, times(1)).updateConfig(eq(user), eq("a"));
    }

    @Test
    public void updateConfigBadRequest() {
        result = new ConfigEditorResult(ConfigEditorResult.StatusCode.BAD_REQUEST, attributes);

        when(service.updateConfig(eq(user), eq("a"))).thenReturn(result);
        var ret = serviceWithErrorMessage.updateConfig(user, "a");
        Assert.assertEquals(ConfigEditorResult.StatusCode.BAD_REQUEST, ret.getStatusCode());
        verify(service, times(1)).updateConfig(eq(user), eq("a"));
        Assert.assertNotNull(ret.getAttributes().getMessage());
        Assert.assertNotNull(ret.getAttributes().getErrorResolution());
        Assert.assertNotNull(ret.getAttributes().getErrorTitle());
    }

    @Test
    public void deleteConfigOk() {
        when(service.deleteConfig(eq(user), eq("a"))).thenReturn(result);
        var ret = serviceWithErrorMessage.deleteConfig(user, "a");
        Assert.assertEquals(ret, result);
        verify(service, times(1)).deleteConfig(eq(user), eq("a"));
    }

    @Test
    public void deleteConfigBadRequest() {
        result = new ConfigEditorResult(ConfigEditorResult.StatusCode.BAD_REQUEST, attributes);

        when(service.deleteConfig(eq(user), eq("a"))).thenReturn(result);
        var ret = serviceWithErrorMessage.deleteConfig(user, "a");
        Assert.assertEquals(ConfigEditorResult.StatusCode.BAD_REQUEST, ret.getStatusCode());
        verify(service, times(1)).deleteConfig(eq(user), eq("a"));
        Assert.assertNotNull(ret.getAttributes().getMessage());
        Assert.assertNotNull(ret.getAttributes().getErrorResolution());
        Assert.assertNotNull(ret.getAttributes().getErrorTitle());
    }

    @Test
    public void submitAdminConfigOk() {
        when(service.submitAdminConfig(eq(user), eq("a"))).thenReturn(result);
        var ret = serviceWithErrorMessage.submitAdminConfig(user, "a");
        Assert.assertEquals(ret, result);
        verify(service, times(1)).submitAdminConfig(eq(user), eq("a"));
    }

    @Test
    public void submitAdminConfigBadRequest() {
        result = new ConfigEditorResult(ConfigEditorResult.StatusCode.BAD_REQUEST, attributes);

        when(service.submitAdminConfig(eq(user), eq("a"))).thenReturn(result);
        var ret = serviceWithErrorMessage.submitAdminConfig(user, "a");
        Assert.assertEquals(ConfigEditorResult.StatusCode.BAD_REQUEST, ret.getStatusCode());
        verify(service, times(1)).submitAdminConfig(eq(user), eq("a"));
        Assert.assertNotNull(ret.getAttributes().getMessage());
        Assert.assertNotNull(ret.getAttributes().getErrorResolution());
        Assert.assertNotNull(ret.getAttributes().getErrorTitle());
    }

    @Test
    public void submitReleaseOk() {
        when(service.submitConfigsRelease(eq(user), eq("a"))).thenReturn(result);
        var ret = serviceWithErrorMessage.submitConfigsRelease(user, "a");
        Assert.assertEquals(ret, result);
        verify(service, times(1)).submitConfigsRelease(eq(user), eq("a"));
    }

    @Test
    public void submitConfigsReleaseBadRequest() {
        result = new ConfigEditorResult(ConfigEditorResult.StatusCode.BAD_REQUEST, attributes);

        when(service.submitConfigsRelease(eq(user), eq("a"))).thenReturn(result);
        var ret = serviceWithErrorMessage.submitConfigsRelease(user, "a");
        Assert.assertEquals(ConfigEditorResult.StatusCode.BAD_REQUEST, ret.getStatusCode());
        verify(service, times(1)).submitConfigsRelease(eq(user), eq("a"));
        Assert.assertNotNull(ret.getAttributes().getMessage());
        Assert.assertNotNull(ret.getAttributes().getErrorResolution());
        Assert.assertNotNull(ret.getAttributes().getErrorTitle());
    }
}
