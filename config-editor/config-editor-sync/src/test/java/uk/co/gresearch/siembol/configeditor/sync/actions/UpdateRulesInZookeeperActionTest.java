package uk.co.gresearch.siembol.configeditor.sync.actions;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import uk.co.gresearch.siembol.common.zookeeper.ZooKeeperConnector;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorServiceContext;
import uk.co.gresearch.siembol.configeditor.sync.common.ConfigServiceHelper;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult.StatusCode.ERROR;
import static uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult.StatusCode.OK;

public class UpdateRulesInZookeeperActionTest {
    private ConfigServiceHelper serviceHelper;
    private String release = "RELEASE";
    private UpdateReleaseInZookeeperAction updateReleaseInZookeeperAction;
    private ZooKeeperConnector zooKeeperConnector;
    private ConfigEditorServiceContext context;
    private String currentRelease = "ZK_RELEASE";

    @Before
    public void setUp() throws Exception {
        context = new ConfigEditorServiceContext();
        context.setConfigRelease(release);

        zooKeeperConnector = Mockito.mock(ZooKeeperConnector.class);
        when(zooKeeperConnector.getData()).thenReturn(currentRelease);
        doNothing().when(zooKeeperConnector).setData(eq(release));

        serviceHelper = Mockito.mock(ConfigServiceHelper.class);
        when(serviceHelper.getReleaseVersion(eq(currentRelease))).thenReturn(5);
        when(serviceHelper.isInitRelease(eq(currentRelease))).thenReturn(false);
        when(serviceHelper.getReleaseVersion(eq(release))).thenReturn(6);
        when(serviceHelper.getZookeeperReleaseConnector()).thenReturn(Optional.of(zooKeeperConnector));
        when(serviceHelper.getName()).thenReturn("dummy_service");

        updateReleaseInZookeeperAction = new UpdateReleaseInZookeeperAction(serviceHelper);
        verify(serviceHelper, times(1)).getZookeeperReleaseConnector();
    }

    @Test
    public void getUpdateReleaseOk() throws Exception {
        ConfigEditorResult result = updateReleaseInZookeeperAction.execute(context);
        Assert.assertEquals(OK, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes().getServiceContext());
        verify(serviceHelper, times(1)).getReleaseVersion(eq(currentRelease));
        verify(serviceHelper, times(1)).isInitRelease(eq(release));
        verify(serviceHelper, times(1)).getReleaseVersion(eq(release));
        verify(zooKeeperConnector, times(1)).setData(eq(release));
    }

    @Test
    public void getUpdateInitReleaseOk() throws Exception {
        when(serviceHelper.isInitRelease(eq(release))).thenReturn(true);
        ConfigEditorResult result = updateReleaseInZookeeperAction.execute(context);
        Assert.assertEquals(OK, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes().getServiceContext());
        verify(serviceHelper, times(0)).getReleaseVersion(eq(release));
        verify(serviceHelper, times(1)).isInitRelease(eq(release));
        verify(serviceHelper, times(0)).getReleaseVersion(eq(release));
        verify(zooKeeperConnector, times(0)).setData(eq(release));
    }

    @Test
    public void getUpdateMissingConfigRelease() {
        context.setConfigRelease(null);
        ConfigEditorResult result = updateReleaseInZookeeperAction.execute(context);
        Assert.assertEquals(ERROR, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes().getMessage());
    }

    @Test
    public void getUpdateNotNeededOk() throws Exception {
        when(serviceHelper.getReleaseVersion(eq(release))).thenReturn(5);
        ConfigEditorResult result = updateReleaseInZookeeperAction.execute(context);
        Assert.assertEquals(OK, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes().getMessage());
        verify(serviceHelper, times(1)).getReleaseVersion(eq(currentRelease));
        verify(serviceHelper, times(1)).getReleaseVersion(eq(release));
        verify(zooKeeperConnector, times(0)).setData(eq(release));
    }

    @Test(expected = IllegalArgumentException.class)
    public void missingConnector() {
        when(serviceHelper.getZookeeperReleaseConnector()).thenReturn(Optional.empty());
        updateReleaseInZookeeperAction = new UpdateReleaseInZookeeperAction(serviceHelper);
    }

    @Test
    public void getUpdateReleaseSendDataException() throws Exception {
        doThrow(new RuntimeException()).when(zooKeeperConnector).setData(eq(release));
        ConfigEditorResult result = updateReleaseInZookeeperAction.execute(context);
        Assert.assertEquals(ERROR, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes().getException());
        verify(serviceHelper, times(1)).getReleaseVersion(eq(currentRelease));
        verify(serviceHelper, times(1)).getReleaseVersion(eq(release));
        verify(zooKeeperConnector, times(1)).setData(eq(release));
    }
}
