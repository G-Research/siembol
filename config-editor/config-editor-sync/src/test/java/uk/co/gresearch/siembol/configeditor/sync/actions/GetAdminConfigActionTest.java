package uk.co.gresearch.siembol.configeditor.sync.actions;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorServiceContext;
import uk.co.gresearch.siembol.configeditor.sync.common.ConfigServiceHelper;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult.StatusCode.ERROR;
import static uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult.StatusCode.OK;

public class GetAdminConfigActionTest {
    private ConfigServiceHelper serviceHelper;
    private String adminConfig = "ADMIN_CONFIG";
    private GetAdminConfigAction getAdminConfigAction;
    private ConfigEditorServiceContext context;
    private int version = 1;

    @Before
    public void setUp() {
        context = new ConfigEditorServiceContext();
        serviceHelper = Mockito.mock(ConfigServiceHelper.class);

        when(serviceHelper.getAdminConfig()).thenReturn(Optional.of(adminConfig));
        when(serviceHelper.getAdminConfigVersion(eq(adminConfig))).thenReturn(version);
        when(serviceHelper.validateAdminConfiguration(eq(adminConfig))).thenReturn(true);
        getAdminConfigAction = new GetAdminConfigAction(serviceHelper);
    }

    @Test
    public void getAdminConfigOk() {
        ConfigEditorResult result = getAdminConfigAction.execute(context);
        Assert.assertEquals(OK, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes().getServiceContext());
        Assert.assertEquals(adminConfig, result.getAttributes().getServiceContext().getAdminConfig());
        verify(serviceHelper, times(1)).getAdminConfig();
        verify(serviceHelper, times(1)).validateAdminConfiguration(eq(adminConfig));
    }

    @Test
    public void getAdminConfigInvalidError() {
        when(serviceHelper.validateAdminConfiguration(eq(adminConfig))).thenReturn(false);
        ConfigEditorResult result = getAdminConfigAction.execute(context);
        Assert.assertEquals(ERROR, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes().getMessage());
        verify(serviceHelper, times(1)).getAdminConfig();
        verify(serviceHelper, times(0)).getAdminConfigVersion(eq(adminConfig));
        verify(serviceHelper, times(1)).validateAdminConfiguration(eq(adminConfig));
    }

    @Test
    public void getAdminConfigEmptyError() {
        when(serviceHelper.getAdminConfig()).thenReturn(Optional.empty());
        ConfigEditorResult result = getAdminConfigAction.execute(context);
        Assert.assertEquals(ERROR, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes().getMessage());
        verify(serviceHelper, times(1)).getAdminConfig();
        verify(serviceHelper, times(0)).getAdminConfigVersion(eq(adminConfig));
        verify(serviceHelper, times(0)).validateAdminConfiguration(eq(adminConfig));
    }
}