package uk.co.gresearch.siembol.configeditor.common;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorAttributes;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult;

import static org.mockito.Mockito.*;

public class ConfigTesterWithErrorMessageTest {
    private ConfigTester configTester;
    private ConfigTester configTesterWithErrorMessage;
    private ConfigEditorResult result;
    private ConfigEditorAttributes attributes;
    private final String configuration = "dummy config";
    private final String testSpecification = "dummy config";
    private final String configurations = "dummy configs";
    private UserInfo user;

    @Before
    public void setUp() {
        configTester = Mockito.mock(ConfigTester.class);
        when(configTester.withErrorMessage()).thenCallRealMethod();
        configTesterWithErrorMessage = configTester.withErrorMessage();
        attributes = new ConfigEditorAttributes();
        result = new ConfigEditorResult(ConfigEditorResult.StatusCode.OK, attributes);
        user = Mockito.mock(UserInfo.class);
    }

    @Test
    public void getTestSchemaOk() {
        when(configTester.getTestSpecificationSchema()).thenReturn(result);
        var ret = configTesterWithErrorMessage.getTestSpecificationSchema();
        Assert.assertEquals(ret, result);
        verify(configTester, times(1)).getTestSpecificationSchema();
    }

    @Test
    public void testConfigurationOk() {
        when(configTester.testConfiguration(eq(configuration), eq(testSpecification))).thenReturn(result);
        var ret = configTesterWithErrorMessage.testConfiguration(configuration, testSpecification);
        Assert.assertEquals(ret, result);
        verify(configTester, times(1)).testConfiguration(eq(configuration), eq(testSpecification));
    }

    @Test
    public void testConfigurationBadRequest() {
        result = new ConfigEditorResult(ConfigEditorResult.StatusCode.BAD_REQUEST, attributes);

        when(configTester.testConfiguration(eq(configuration), eq(testSpecification))).thenReturn(result);
        var ret = configTesterWithErrorMessage.testConfiguration(configuration, testSpecification);
        Assert.assertEquals(ConfigEditorResult.StatusCode.BAD_REQUEST, ret.getStatusCode());
        verify(configTester, times(1)).testConfiguration(eq(configuration), eq(testSpecification));
        Assert.assertNotNull(ret.getAttributes().getMessage());
        Assert.assertNotNull(ret.getAttributes().getErrorResolution());
        Assert.assertNotNull(ret.getAttributes().getErrorTitle());
    }

    @Test
    public void testConfigurationsOk() {
        when(configTester.testConfigurations(eq(configurations), eq(testSpecification))).thenReturn(result);
        var ret = configTesterWithErrorMessage.testConfigurations(configurations, testSpecification);
        Assert.assertEquals(ret, result);
        verify(configTester, times(1)).testConfigurations(eq(configurations), eq(testSpecification));
    }

    @Test
    public void testConfigurationsBadRequest() {
        result = new ConfigEditorResult(ConfigEditorResult.StatusCode.BAD_REQUEST, attributes);

        when(configTester.testConfigurations(eq(configurations), eq(testSpecification))).thenReturn(result);
        var ret = configTesterWithErrorMessage.testConfigurations(configurations, testSpecification);
        Assert.assertEquals(ConfigEditorResult.StatusCode.BAD_REQUEST, ret.getStatusCode());
        verify(configTester, times(1)).testConfigurations(eq(configurations), eq(testSpecification));
        Assert.assertNotNull(ret.getAttributes().getMessage());
        Assert.assertNotNull(ret.getAttributes().getErrorResolution());
        Assert.assertNotNull(ret.getAttributes().getErrorTitle());
    }
    
}
