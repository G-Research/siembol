package uk.co.gresearch.siembol.configeditor.sync.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import org.adrianwalker.multilinestring.Multiline;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import uk.co.gresearch.siembol.common.model.StormTopologiesDto;
import uk.co.gresearch.siembol.common.model.StormTopologyDto;
import uk.co.gresearch.siembol.common.zookeeper.ZooKeeperConnector;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult;


import java.io.IOException;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class StormApplicationProviderTest {
    private static ObjectReader TOPOLOGIES_READER = new ObjectMapper()
            .readerFor(StormTopologiesDto.class);

    /**
     * {
     *   "timestamp": 1,
     *   "topologies": [
     *     {
     *       "topology_name": "a",
     *       "topology_id": "1",
     *       "image": "secret",
     *       "service_name": "alert",
     *       "attributes": [
     *         "a1",
     *         "a2"
     *       ]
     *     },
     *     {
     *       "topology_name": "b",
     *       "topology_id": "2",
     *       "image": "secret",
     *       "service_name": "parsing",
     *       "attributes": [
     *         "a1",
     *         "a2",
     *         "a3"
     *       ]
     *     },
     *     {
     *       "topology_name": "c",
     *       "topology_id": "3",
     *       "image": "secret",
     *       "service_name": "parsing",
     *       "attributes": [
     *         "a1",
     *         "a2",
     *         "a3"
     *       ]
     *     }
     *   ]
     * }
     **/
    @Multiline
    public static String initTopologies;

    /**
     * {
     *   "timestamp": 1,
     *   "topologies": [
     *     {
     *       "topology_name": "a",
     *       "topology_id": "1",
     *       "image": "secret",
     *       "service_name": "alert",
     *       "attributes": [
     *         "a1",
     *         "a3"
     *       ]
     *     },
     *     {
     *       "topology_name": "b",
     *       "topology_id": "2",
     *       "image": "secret",
     *       "service_name": "parsing",
     *       "attributes": [
     *         "a1",
     *         "a2",
     *         "a3"
     *       ]
     *     },
     *     {
     *       "topology_name": "c",
     *       "topology_id": "3",
     *       "image": "secret",
     *       "service_name": "parsing",
     *       "attributes": [
     *         "a1",
     *         "a2",
     *         "a4"
     *       ]
     *     }
     *   ]
     * }
     **/
    @Multiline
    public static String updatedTopologies;


    private ZooKeeperConnector zooKeeperConnector;
    private StormApplicationProviderImpl stormApplicationProvider;
    private Set<String> services;
    private List<StormTopologyDto> topologiesToUpdate;

    @Before
    public void setUp() throws IOException {
        services = new HashSet<>();
        zooKeeperConnector = Mockito.mock(ZooKeeperConnector.class);
        when(zooKeeperConnector.getData()).thenReturn(initTopologies);
        doNothing().when(zooKeeperConnector).addCacheListener(any());
        stormApplicationProvider = new StormApplicationProviderImpl(zooKeeperConnector);
        topologiesToUpdate = ((StormTopologiesDto)TOPOLOGIES_READER.readValue(updatedTopologies)).getTopologies();
    }

    @Test
    public void getTopologies() {
        ConfigEditorResult result = stormApplicationProvider.getStormTopologies();
        Assert.assertEquals(ConfigEditorResult.StatusCode.OK, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes().getTopologies());
        Assert.assertEquals(3, result.getAttributes().getTopologies().size());
    }

    @Test
    public void getTopologiesForServiceOk() {
        ConfigEditorResult result = stormApplicationProvider.getStormTopologies("parsing");
        Assert.assertEquals(ConfigEditorResult.StatusCode.OK, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes().getTopologies());
        Assert.assertEquals(2, result.getAttributes().getTopologies().size());
    }

    @Test
    public void getTopologiesForServiceOk2() {
        ConfigEditorResult result = stormApplicationProvider.getStormTopologies("alert");
        Assert.assertEquals(ConfigEditorResult.StatusCode.OK, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes().getTopologies());
        Assert.assertEquals(1, result.getAttributes().getTopologies().size());
    }

    @Test
    public void getTopologiesForServiceNoExisting() {
        ConfigEditorResult result = stormApplicationProvider.getStormTopologies("nonexisting");
        Assert.assertEquals(ConfigEditorResult.StatusCode.OK, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes().getTopologies());
        Assert.assertEquals(0, result.getAttributes().getTopologies().size());
    }

    @Test
    public void restartStormTopologyOk() {
        ConfigEditorResult result = stormApplicationProvider.restartStormTopology("alert", "a");
        Assert.assertEquals(ConfigEditorResult.StatusCode.OK, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes().getTopologies());
        Assert.assertEquals(3, result.getAttributes().getTopologies().size());
        StormTopologyDto restarted = result.getAttributes().getTopologies().stream()
                .filter(x -> x.getTopologyName().equals("a"))
                .findFirst().orElseGet(null);

        Assert.assertNotNull(restarted);
        Assert.assertNotNull(restarted.getTopologyId());
        Assert.assertNotEquals("1", restarted.getTopologyId());
    }

    @Test
    public void restartStormTopologyNonexisting() {
        ConfigEditorResult result = stormApplicationProvider
                .restartStormTopology("alert", "q");
        Assert.assertEquals(ConfigEditorResult.StatusCode.BAD_REQUEST, result.getStatusCode());
        Assert.assertNull(result.getAttributes().getTopologies());
        Assert.assertNotNull(result.getAttributes().getMessage());
    }

    @Test
    public void restartStormTopologyWrongService() {
        ConfigEditorResult result = stormApplicationProvider
                .restartStormTopology("parsing", "a");
        Assert.assertEquals(ConfigEditorResult.StatusCode.BAD_REQUEST, result.getStatusCode());
        Assert.assertNull(result.getAttributes().getTopologies());
        Assert.assertNotNull(result.getAttributes().getMessage());
    }

    @Test
    public void removeAllTopologiesServices() throws Exception {
        services.addAll(Arrays.asList("alert", "parsing"));
        ConfigEditorResult result = stormApplicationProvider.updateStormTopologies(new ArrayList<>(), services);
        Assert.assertEquals(ConfigEditorResult.StatusCode.OK, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes().getTopologies());
        Assert.assertTrue(result.getAttributes().getTopologies().isEmpty());
        verify(zooKeeperConnector, times(1)).setData(any());
    }

    @Test
    public void removeTopologiesFromOneService() throws Exception {
        services.addAll(Arrays.asList("parsing"));
        ConfigEditorResult result = stormApplicationProvider.updateStormTopologies(new ArrayList<>(), services);
        Assert.assertEquals(ConfigEditorResult.StatusCode.OK, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes().getTopologies());
        Assert.assertEquals(1, result.getAttributes().getTopologies().size());
        Assert.assertEquals("alert", result.getAttributes().getTopologies().get(0).getServiceName());
        Assert.assertEquals("1", result.getAttributes().getTopologies().get(0).getTopologyId());
        Assert.assertEquals("secret", result.getAttributes().getTopologies().get(0).getImage());
        verify(zooKeeperConnector, times(1)).setData(any());
    }

    @Test
    public void updateTopologiesOk() throws Exception {
        services.addAll(Arrays.asList("alert", "parsing"));
        ConfigEditorResult result = stormApplicationProvider.updateStormTopologies(topologiesToUpdate, services);
        Assert.assertEquals(ConfigEditorResult.StatusCode.OK, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes().getTopologies());
        Assert.assertEquals(3, result.getAttributes().getTopologies().size());
        verify(zooKeeperConnector, times(1)).setData(any());
    }

    @Test
    public void updateTopologiesNothingToUpdate() throws Exception {
        services.addAll(Arrays.asList("alert", "parsing"));
        topologiesToUpdate =  ((StormTopologiesDto)TOPOLOGIES_READER.readValue(initTopologies)).getTopologies();
        ConfigEditorResult result = stormApplicationProvider.updateStormTopologies(topologiesToUpdate, services);
        Assert.assertEquals(ConfigEditorResult.StatusCode.OK, result.getStatusCode());
        Assert.assertNull(result.getAttributes().getTopologies());
        Assert.assertNotNull(result.getAttributes().getMessage());
        verify(zooKeeperConnector, times(0)).setData(any());
    }

    @Test(expected = IllegalStateException.class)
    public void wrongInitTopologies() throws Exception {
        when(zooKeeperConnector.getData()).thenReturn("INVALID");
        doNothing().when(zooKeeperConnector).addCacheListener(any());
        stormApplicationProvider = new StormApplicationProviderImpl(zooKeeperConnector);
    }

    @Test
    public void testHealth() throws Exception {
        Health health = stormApplicationProvider.checkHealth();
        Assert.assertEquals(Status.UP, health.getStatus());
    }

    @Test
    public void updateDuplicatesError() throws Exception {
        topologiesToUpdate.get(0).setTopologyName("b");
        services.addAll(Arrays.asList("alert", "parsing"));
        ConfigEditorResult result = stormApplicationProvider.updateStormTopologies(topologiesToUpdate, services);
        Assert.assertEquals(ConfigEditorResult.StatusCode.ERROR, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes().getException());
        Health health = stormApplicationProvider.checkHealth();
        Assert.assertEquals(Status.DOWN, health.getStatus());
    }
}
