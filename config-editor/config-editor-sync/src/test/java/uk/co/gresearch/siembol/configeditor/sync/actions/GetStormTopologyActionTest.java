package uk.co.gresearch.siembol.configeditor.sync.actions;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import uk.co.gresearch.siembol.common.model.StormTopologyDto;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorServiceContext;
import uk.co.gresearch.siembol.configeditor.sync.common.ConfigServiceHelper;

import java.util.Base64;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult.StatusCode.ERROR;
import static uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult.StatusCode.OK;

public class GetStormTopologyActionTest {
    private ConfigServiceHelper serviceHelper;
    private String adminConfig = "ADMIN_CONFIG";
    private GetStormTopologyAction getStormTopologyAction;
    private ConfigEditorServiceContext context;
    private String topologyName = "dummyTopologyName";
    private String topologyImage = "dummyImage";
    private String serviceName = "dummyService";

    @Before
    public void setUp() {
        context = new ConfigEditorServiceContext();
        context.setAdminConfig(adminConfig);

        serviceHelper = Mockito.mock(ConfigServiceHelper.class);
        when(serviceHelper.getStormTopologyName(eq(adminConfig))).thenReturn(Optional.of(topologyName));
        when(serviceHelper.getStormTopologyImage()).thenReturn(Optional.of(topologyImage));
        when(serviceHelper.getName()).thenReturn(serviceName);

        getStormTopologyAction = new GetStormTopologyAction(serviceHelper);
    }

    @Test
    public void getStormTopologyOk() {
        ConfigEditorResult result = getStormTopologyAction.execute(context);
        Assert.assertEquals(OK, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes().getServiceContext());
        Assert.assertTrue(result.getAttributes().getServiceContext().getStormTopologies().isPresent());
        Assert.assertEquals(1, result.getAttributes().getServiceContext().getStormTopologies().get().size());
        verify(serviceHelper, times(1)).getStormTopologyName(eq(adminConfig));
        verify(serviceHelper, times(1)).getStormTopologyImage();
        verify(serviceHelper, times(1)).getName();

        StormTopologyDto topology = result.getAttributes().getServiceContext().getStormTopologies().get().get(0);
        Assert.assertEquals(serviceName, topology.getServiceName());
        Assert.assertEquals(topologyName, topology.getTopologyName());
        Assert.assertEquals(topologyImage, topology.getImage());
        Assert.assertNotNull(topology.getTopologyId());
        Assert.assertEquals(1, topology.getAttributes().size());
        Assert.assertEquals(adminConfig, new String(Base64.getDecoder().decode(topology.getAttributes().get(0))));
    }

    @Test
    public void getStormTopologyMissingTopologyName() {
        when(serviceHelper.getStormTopologyName(eq(adminConfig))).thenReturn(Optional.empty());
        ConfigEditorResult result = getStormTopologyAction.execute(context);
        Assert.assertEquals(ERROR, result.getStatusCode());
        verify(serviceHelper, times(1)).getStormTopologyName(eq(adminConfig));
    }

    @Test
    public void getStormTopologyMissingAdminConfig() {
        context.setAdminConfig(null);
        ConfigEditorResult result = getStormTopologyAction.execute(context);
        Assert.assertEquals(ERROR, result.getStatusCode());
    }

    @Test
    public void getStormTopologyMissingServiceName() {
        when(serviceHelper.getStormTopologyImage()).thenReturn(Optional.empty());
        ConfigEditorResult result = getStormTopologyAction.execute(context);
        Assert.assertEquals(ERROR, result.getStatusCode());
        verify(serviceHelper, times(1)).getStormTopologyImage();
    }
}
