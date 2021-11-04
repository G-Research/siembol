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

public class GetReleaseActionTest {
    private ConfigServiceHelper serviceHelper;
    private final String release = "RELEASE";
    private GetReleaseAction getReleaseAction;
    private ConfigEditorServiceContext context;
    private final int version = 1;

    @Before
    public void setUp() {
        context = new ConfigEditorServiceContext();
        serviceHelper = Mockito.mock(ConfigServiceHelper.class);

        when(serviceHelper.getConfigsRelease()).thenReturn(Optional.of(release));

        when(serviceHelper.getReleaseVersion(eq(release))).thenReturn(version);
        when(serviceHelper.validateConfigurations(eq(release))).thenReturn(true);
        getReleaseAction = new GetReleaseAction(serviceHelper);
    }

    @Test
    public void getReleaseOk() {
        ConfigEditorResult result = getReleaseAction.execute(context);
        Assert.assertEquals(OK, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes().getServiceContext());
        Assert.assertEquals(release, result.getAttributes().getServiceContext().getConfigRelease());
        verify(serviceHelper, times(1)).getConfigsRelease();
        verify(serviceHelper, times(1)).validateConfigurations(eq(release));
    }

    @Test
    public void getReleaseInvalidError() {
        when(serviceHelper.validateConfigurations(eq(release))).thenReturn(false);
        ConfigEditorResult result = getReleaseAction.execute(context);
        Assert.assertEquals(ERROR, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes().getMessage());
        verify(serviceHelper, times(1)).getConfigsRelease();
        verify(serviceHelper, times(1)).validateConfigurations(eq(release));
    }

    @Test
    public void getReleaseEmptyError() {
        when(serviceHelper.getConfigsRelease()).thenReturn(Optional.empty());
        ConfigEditorResult result = getReleaseAction.execute(context);
        Assert.assertEquals(ERROR, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes().getMessage());
        verify(serviceHelper, times(1)).getConfigsRelease();
        verify(serviceHelper, times(0)).validateConfigurations(eq(release));
    }
}
