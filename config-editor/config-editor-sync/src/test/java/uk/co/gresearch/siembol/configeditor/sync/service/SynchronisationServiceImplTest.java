package uk.co.gresearch.siembol.configeditor.sync.service;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.boot.actuate.health.Status;
import uk.co.gresearch.siembol.common.model.StormTopologyDto;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorServiceContext;
import uk.co.gresearch.siembol.configeditor.sync.actions.SynchronisationAction;
import uk.co.gresearch.siembol.configeditor.sync.common.SynchronisationType;

import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class SynchronisationServiceImplTest {
    private StormApplicationProvider stormProvider;
    private SynchronisationServiceImpl.Builder builder;
    private SynchronisationServiceImpl synchronisationService;
    SynchronisationAction action;
    ConfigEditorResult actionResult;
    ConfigEditorServiceContext context = new ConfigEditorServiceContext();

    @Before
    public void setUp() {
        action = Mockito.mock(SynchronisationAction.class);
        actionResult = ConfigEditorResult.fromServiceContext(context);
        when(action.execute(any())).thenReturn(actionResult);

        stormProvider = Mockito.mock(StormApplicationProvider.class);
        builder = new SynchronisationServiceImpl.Builder(stormProvider);
        builder.allServiceNames = Arrays.asList("a", "b");

        Map<String, SynchronisationAction> actionsMap = new HashMap<>();
        actionsMap.put("a", action);

        builder.syncTypeToActionsMap = new HashMap<>();
        builder.syncTypeToActionsMap.put(SynchronisationType.RELEASE, actionsMap);
        builder.syncTypeToActionsMap.put(SynchronisationType.ADMIN_CONFIG, actionsMap);
        builder.syncTypeToActionsMap.put(SynchronisationType.ALL, actionsMap);
        synchronisationService = new SynchronisationServiceImpl(builder);
    }

    @Test
    public void syncAllOk() {
        ConfigEditorResult result = synchronisationService.synchroniseAllServices(SynchronisationType.ALL);
        Assert.assertEquals(ConfigEditorResult.StatusCode.OK, result.getStatusCode());
        verify(action, times(1)).execute(any());
    }

    @Test
    public void syncOneServiceOk() {
        ConfigEditorResult result = synchronisationService.synchroniseServices(List.of("a"), SynchronisationType.ALL);
        Assert.assertEquals(ConfigEditorResult.StatusCode.OK, result.getStatusCode());
        verify(action, times(1)).execute(any());
    }

    @Test
    public void checkHealthOk() {
        Assert.assertEquals(Status.UP, synchronisationService.checkHealth().getStatus());
    }

    @Test
    public void syncAllError() {
        when(action.execute(any())).thenReturn(ConfigEditorResult.fromMessage(ConfigEditorResult.StatusCode.ERROR,
                "error"));
        ConfigEditorResult result = synchronisationService.synchroniseAllServices(SynchronisationType.ALL);
        Assert.assertEquals(ConfigEditorResult.StatusCode.ERROR, result.getStatusCode());
        verify(action, times(1)).execute(any());
        Assert.assertEquals(Status.DOWN, synchronisationService.checkHealth().getStatus());
    }

    @Test
    public void syncAllWithTopologiesOk() {
        when(stormProvider.updateStormTopologies(any(), any(), eq(true))).thenReturn(
                ConfigEditorResult.fromMessage(ConfigEditorResult.StatusCode.OK, "ok"));
        context.setStormTopologies(Optional.of(Arrays.asList(new StormTopologyDto())));
        ConfigEditorResult result = synchronisationService.synchroniseAllServices(SynchronisationType.ALL);
        Assert.assertEquals(ConfigEditorResult.StatusCode.OK, result.getStatusCode());
        verify(action, times(1)).execute(any());
        verify(stormProvider, times(1)).updateStormTopologies(any(), any(), eq(true));
    }

    @Test
    public void syncOneServiceWithTopologyOk() {
        when(stormProvider.updateStormTopologies(any(), any(), eq(false))).thenReturn(
                ConfigEditorResult.fromMessage(ConfigEditorResult.StatusCode.OK, "ok"));
        context.setStormTopologies(Optional.of(Arrays.asList(new StormTopologyDto())));
        ConfigEditorResult result = synchronisationService.synchroniseServices(List.of("a"),
                SynchronisationType.ALL);
        Assert.assertEquals(ConfigEditorResult.StatusCode.OK, result.getStatusCode());
        verify(action, times(1)).execute(any());
        verify(stormProvider, times(1)).updateStormTopologies(any(), any(), eq(false));
    }

    @Test
    public void syncAllWithTopologiesError() {
        when(stormProvider.updateStormTopologies(any(), any(), eq(true))).thenReturn(
                ConfigEditorResult.fromMessage(ConfigEditorResult.StatusCode.ERROR, "error"));
        context.setStormTopologies(Optional.of(Arrays.asList(new StormTopologyDto())));
        ConfigEditorResult result = synchronisationService.synchroniseAllServices(SynchronisationType.ALL);
        Assert.assertEquals(ConfigEditorResult.StatusCode.ERROR, result.getStatusCode());
        Assert.assertEquals(Status.DOWN, synchronisationService.checkHealth().getStatus());
        verify(action, times(1)).execute(any());
        verify(stormProvider, times(1)).updateStormTopologies(any(), any(), eq(true));
    }
}
