package uk.co.gresearch.siembol.deployment.storm.service;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.adrianwalker.multilinestring.Multiline;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.actuate.health.Status;
import uk.co.gresearch.siembol.common.model.StormTopologiesDto;
import uk.co.gresearch.siembol.common.model.ZooKeeperAttributesDto;
import uk.co.gresearch.siembol.common.testing.TestingZooKeeperConnectorFactory;
import uk.co.gresearch.siembol.common.zookeeper.ZooKeeperConnector;
import uk.co.gresearch.siembol.deployment.storm.model.StormResponseDto;
import uk.co.gresearch.siembol.deployment.storm.model.TopologyManagerInfoDto;
import uk.co.gresearch.siembol.deployment.storm.providers.KubernetesProvider;
import uk.co.gresearch.siembol.deployment.storm.providers.StormProvider;

import static org.mockito.Mockito.*;
import static uk.co.gresearch.siembol.deployment.storm.model.TopologyStateDto.*;

public class TopologyManagerServiceImplTests {
    private static final ObjectReader READER = new ObjectMapper()
            .readerFor(StormTopologiesDto.class);

    private static final ObjectReader READER_STORM = new ObjectMapper()
            .readerFor(StormResponseDto.class);

    private static final ObjectWriter WRITER = new ObjectMapper()
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .writerFor(StormTopologiesDto.class);

    /**
     * {"topologies":[{"id":"id1","name":"topology1"}]}
     */
    @Multiline
    private static String runningTopologies;

    /**
     * {"topologies":[{"id":"id1","name":"topology1"},{"id":"id2","name":"topology2"},{"id":"id3","name":"topology3"},{"id":"id4","name":"topology4"}]}
     */
    @Multiline
    private static String runningTopologies2;

    /**
     * {"topologies": []}
     *
     **/
    @Multiline
    private static String empty;

    /**
     * {"topologies": [
     * { "topology_id": "t1", "topology_name": "topology1", "image": "", "attributes": ["", ""] }
     * ]}
     *
     **/
    @Multiline
    private static String topologies1;

    /**
     * {"topologies": [
     * { "topology_id": "t2", "topology_name": "topology1", "image": "", "attributes": ["", ""] }
     * ]}
     *
     **/
    @Multiline
    private static String topologies1changed;

    /**
     * {"topologies": [
     * { "topology_id": "t1", "topology_name": "topology1", "image": "", "attributes": ["", ""] },
     * { "topology_id": "t2", "topology_name": "topology2", "image": "", "attributes": ["", ""] }
     * ]}
     *
     **/
    @Multiline
    private static String topologies2;

    /**
     * {"topologies": [
     * { "topology_id": "t3", "topology_name": "topology1", "image": "", "attributes": ["", ""] },
     * { "topology_id": "t4", "topology_name": "topology2", "image": "", "attributes": ["", ""] }
     * ]}
     *
     **/
    @Multiline
    private static String topologies2changed;

    /**
     * {"topologies": [
     * { "topology_id": "t1", "topology_name": "topology1", "image": "", "attributes": ["", ""] },
     * { "topology_id": "t2", "topology_name": "topology2", "image": "", "attributes": ["", ""] },
     * { "topology_id": "t3", "topology_name": "topology3", "image": "", "attributes": ["", ""] }
     * ]}
     *
     **/
    @Multiline
    private static String topologies3;

    /**
     * {"topologies": [
     * { "topology_id": "t1", "topology_name": "topology1", "image": "", "attributes": ["", ""] },
     * { "topology_id": "t2", "topology_name": "topology2", "image": "", "attributes": ["", ""] },
     * { "topology_id": "t3", "topology_name": "topology3", "image": "", "attributes": ["", ""] },
     * { "topology_id": "t4", "topology_name": "topology4", "image": "", "attributes": ["", ""] }
     * ]}
     *
     **/
    @Multiline
    private static String topologies4;

    /**
     * {"topologies": [
     * { "topology_id": "t8", "topology_name": "topology1", "image": "", "attributes": ["", ""] },
     * { "topology_id": "t2", "topology_name": "topology2", "image": "", "attributes": ["", ""] },
     * { "topology_id": "t3", "topology_name": "topology3", "image": "", "attributes": ["", ""] },
     * { "topology_id": "t4", "topology_name": "topology4", "image": "", "attributes": ["", ""] }
     * ]}
     *
     **/
    @Multiline
    private static String topologies4changed;

    StormResponseDto stormTopologies = READER_STORM.readValue(runningTopologies);

    TopologyManagerService service;
    KubernetesProvider kubernetesProvider = mock(KubernetesProvider.class);
    StormProvider stormProvider = mock(StormProvider.class);
    ZooKeeperConnector desiredZookeeper;
    ZooKeeperConnector savedZookeeper;
    TestingZooKeeperConnectorFactory zooKeeperConnectorFactory;
    ZooKeeperAttributesDto desiredSpec;
    ZooKeeperAttributesDto savedSpec;

    public TopologyManagerServiceImplTests() throws JsonProcessingException {
    }

    @Before
    public void setUp() {
        desiredSpec = new ZooKeeperAttributesDto();
        savedSpec = new ZooKeeperAttributesDto();
        desiredSpec.setZkPath("/siembol/desired");
        savedSpec.setZkPath("/siembol/saved");

        zooKeeperConnectorFactory = new TestingZooKeeperConnectorFactory();
        desiredZookeeper = zooKeeperConnectorFactory.createZookeeperConnector(desiredSpec);
        savedZookeeper = zooKeeperConnectorFactory.createZookeeperConnector(savedSpec);
        service = new TopologyManagerServiceImpl(stormProvider, kubernetesProvider, desiredZookeeper, savedZookeeper, 0);
    }

    @Test
    public void testSyncStart1() throws Exception {
        when(stormProvider.listTopologies()).thenReturn(stormTopologies.getTopologies());
        when(stormProvider.killTopology(any())).thenReturn(true);

        stormTopologies = READER_STORM.readValue(runningTopologies);
        StormTopologiesDto saved = READER.readValue(topologies2);
        StormTopologiesDto desired = READER.readValue(topologies1);

        desiredZookeeper.setData(WRITER.writeValueAsString(saved));
        savedZookeeper.setData(WRITER.writeValueAsString(desired));

        Thread.sleep(100);

        verify(stormProvider, times(1)).listTopologies();
        verify(kubernetesProvider, times(1)).createOrReplaceJob(any());

        StormTopologiesDto desiredState = READER.readValue(desiredZookeeper.getData());
        StormTopologiesDto savedState = READER.readValue(savedZookeeper.getData());

        Assert.assertTrue(
                EqualsBuilder.reflectionEquals(desiredState.getTopologies(), savedState.getTopologies())
        );
        Assert.assertEquals(Status.UP, service.checkHealth().getStatus());
    }

    @Test
    public void testSyncStart2() throws Exception {
        when(stormProvider.listTopologies()).thenReturn(stormTopologies.getTopologies());
        when(stormProvider.killTopology(any())).thenReturn(true);

        stormTopologies = READER_STORM.readValue(runningTopologies);
        StormTopologiesDto saved = READER.readValue(topologies1);
        StormTopologiesDto desired = READER.readValue(topologies4);

        savedZookeeper.setData(WRITER.writeValueAsString(saved));
        desiredZookeeper.setData(WRITER.writeValueAsString(desired));

        Thread.sleep(100);

        verify(stormProvider, times(1)).listTopologies();
        verify(kubernetesProvider, times(3)).createOrReplaceJob(any());

        StormTopologiesDto desiredState = READER.readValue(desiredZookeeper.getData());
        StormTopologiesDto savedState = READER.readValue(savedZookeeper.getData());

        Assert.assertTrue(
                EqualsBuilder.reflectionEquals(desiredState.getTopologies(), savedState.getTopologies())
        );
        Assert.assertEquals(Status.UP, service.checkHealth().getStatus());
    }

    @Test
    public void testSyncUpgradeTopology1() throws Exception {
        when(stormProvider.listTopologies()).thenReturn(stormTopologies.getTopologies());
        when(stormProvider.killTopology(any())).thenReturn(true);

        stormTopologies = READER_STORM.readValue(runningTopologies);
        StormTopologiesDto saved = READER.readValue(topologies1);
        StormTopologiesDto desired = READER.readValue(topologies1changed);

        savedZookeeper.setData(WRITER.writeValueAsString(saved));
        desiredZookeeper.setData(WRITER.writeValueAsString(desired));

        Thread.sleep(100);

        verify(stormProvider, times(1)).listTopologies();
        verify(stormProvider, times(1)).killTopology("id1");
        verify(kubernetesProvider, times(1)).createOrReplaceJob(any());

        StormTopologiesDto desiredState = READER.readValue(desiredZookeeper.getData());
        StormTopologiesDto savedState = READER.readValue(savedZookeeper.getData());

        Assert.assertTrue(
                EqualsBuilder.reflectionEquals(desiredState.getTopologies(), savedState.getTopologies())
        );
        Assert.assertEquals(Status.UP, service.checkHealth().getStatus());
    }

    @Test
    public void testSyncUpgradeTopology2() throws Exception {
        when(stormProvider.listTopologies()).thenReturn(stormTopologies.getTopologies());
        when(stormProvider.killTopology(any())).thenReturn(true);

        stormTopologies = READER_STORM.readValue(runningTopologies);
        StormTopologiesDto saved = READER.readValue(topologies1);
        StormTopologiesDto desired = READER.readValue(topologies4changed);

        savedZookeeper.setData(WRITER.writeValueAsString(saved));
        desiredZookeeper.setData(WRITER.writeValueAsString(desired));

        Thread.sleep(100);

        verify(stormProvider, times(1)).listTopologies();
        verify(stormProvider, times(1)).killTopology("id1");
        verify(kubernetesProvider, times(4)).createOrReplaceJob(any());

        StormTopologiesDto desiredState = READER.readValue(desiredZookeeper.getData());
        StormTopologiesDto savedState = READER.readValue(savedZookeeper.getData());

        Assert.assertTrue(
                EqualsBuilder.reflectionEquals(desiredState.getTopologies(), savedState.getTopologies())
        );
        Assert.assertEquals(Status.UP, service.checkHealth().getStatus());
    }

    @Test
    public void testDeletedTopology1() throws Exception {
        stormTopologies = READER_STORM.readValue(runningTopologies2);
        when(stormProvider.listTopologies()).thenReturn(stormTopologies.getTopologies());
        when(stormProvider.killTopology(any())).thenReturn(true);

        StormTopologiesDto saved = READER.readValue(topologies4);
        StormTopologiesDto desired = READER.readValue(empty);


        savedZookeeper.setData(WRITER.writeValueAsString(saved));
        desiredZookeeper.setData(WRITER.writeValueAsString(desired));

        Thread.sleep(100);

        verify(stormProvider, times(1)).listTopologies();
        verify(stormProvider, times(4)).killTopology(any());

        StormTopologiesDto desiredState = READER.readValue(desiredZookeeper.getData());
        StormTopologiesDto savedState = READER.readValue(savedZookeeper.getData());

        Assert.assertTrue(
                EqualsBuilder.reflectionEquals(desiredState.getTopologies(), savedState.getTopologies())
        );
        Assert.assertEquals(Status.UP, service.checkHealth().getStatus());
    }

    @Test
    public void testStormTopologyNameConflict1() throws Exception {
        stormTopologies = READER_STORM.readValue(runningTopologies);
        when(stormProvider.listTopologies()).thenReturn(stormTopologies.getTopologies());
        when(stormProvider.killTopology(any())).thenReturn(true);

        StormTopologiesDto saved = READER.readValue(empty);
        StormTopologiesDto desired = READER.readValue(topologies1);

        savedZookeeper.setData(WRITER.writeValueAsString(saved));
        desiredZookeeper.setData(WRITER.writeValueAsString(desired));

        Thread.sleep(100);

        verify(stormProvider, times(1)).listTopologies();
        verify(stormProvider, times(1)).killTopology("id1");
        verify(kubernetesProvider, times(1)).createOrReplaceJob(any());

        StormTopologiesDto desiredState = READER.readValue(desiredZookeeper.getData());
        StormTopologiesDto savedState = READER.readValue(savedZookeeper.getData());

        Assert.assertTrue(
                EqualsBuilder.reflectionEquals(desiredState.getTopologies(), savedState.getTopologies())
        );
        Assert.assertEquals(Status.UP, service.checkHealth().getStatus());
    }

    @Test
    public void testTopologyIsMissingFromStorm() throws Exception {
        stormTopologies = READER_STORM.readValue(runningTopologies);
        when(stormProvider.listTopologies()).thenReturn(stormTopologies.getTopologies());
        when(stormProvider.killTopology(any())).thenReturn(true);

        StormTopologiesDto saved = READER.readValue(topologies2);
        StormTopologiesDto desired = READER.readValue(topologies2);

        savedZookeeper.setData(WRITER.writeValueAsString(saved));
        desiredZookeeper.setData(WRITER.writeValueAsString(desired));

        Thread.sleep(100);

        verify(stormProvider, times(1)).listTopologies();
        verify(kubernetesProvider, times(1)).createOrReplaceJob(any());

        StormTopologiesDto desiredState = READER.readValue(desiredZookeeper.getData());
        StormTopologiesDto savedState = READER.readValue(savedZookeeper.getData());

        Assert.assertTrue(
                EqualsBuilder.reflectionEquals(desiredState.getTopologies(), savedState.getTopologies())
        );
        Assert.assertEquals(Status.UP, service.checkHealth().getStatus());
    }

    @Test
    public void testThrowException() throws Exception {
        when(stormProvider.listTopologies()).thenThrow(new IllegalStateException("test exception"));
        StormTopologiesDto saved = READER.readValue(topologies2);
        StormTopologiesDto desired = READER.readValue(topologies2);

        savedZookeeper.setData(WRITER.writeValueAsString(saved));
        desiredZookeeper.setData(WRITER.writeValueAsString(desired));

        Thread.sleep(100);
        Assert.assertEquals(Status.DOWN, service.checkHealth().getStatus());
    }

    @Test
    public void testManagerInfoAllSynced() {
        zooKeeperConnectorFactory.setData(desiredSpec.getZkPath(), topologies4);
        zooKeeperConnectorFactory.setData(savedSpec.getZkPath(), topologies4);
        TopologyManagerInfoDto info = service.getTopologyManagerInfo();
        Assert.assertEquals(4, info.getNumberSynchronised());
        Assert.assertEquals(0, info.getNumberDifferent());
        Assert.assertEquals(4, info.getTopologies().size());
        info.getTopologies().values().forEach(x -> Assert.assertEquals(SYNCHRONISED, x));
    }

    @Test
    public void testManagerInfoDifferent() {
        zooKeeperConnectorFactory.setData(desiredSpec.getZkPath(), topologies2);
        zooKeeperConnectorFactory.setData(savedSpec.getZkPath(), topologies2changed);
        TopologyManagerInfoDto info = service.getTopologyManagerInfo();
        Assert.assertEquals(0, info.getNumberSynchronised());
        Assert.assertEquals(2, info.getNumberDifferent());
        info.getTopologies().values().forEach(x -> Assert.assertEquals(DIFFERENT, x));
    }

    @Test
    public void testManagerInfoMissingDesired() {
        zooKeeperConnectorFactory.setData(desiredSpec.getZkPath(), topologies2);
        zooKeeperConnectorFactory.setData(savedSpec.getZkPath(), topologies3);
        TopologyManagerInfoDto info = service.getTopologyManagerInfo();
        Assert.assertEquals(2, info.getNumberSynchronised());
        Assert.assertEquals(1, info.getNumberDifferent());
        Assert.assertEquals(SAVED_STATE_ONLY, info.getTopologies().get("topology3"));
    }

    @Test
    public void testManagerInfoMissingSaved() {
        zooKeeperConnectorFactory.setData(desiredSpec.getZkPath(), topologies3);
        zooKeeperConnectorFactory.setData(savedSpec.getZkPath(), topologies2);
        TopologyManagerInfoDto info = service.getTopologyManagerInfo();
        Assert.assertEquals(2, info.getNumberSynchronised());
        Assert.assertEquals(1, info.getNumberDifferent());
        Assert.assertEquals(DESIRED_STATE_ONLY, info.getTopologies().get("topology3"));
    }

    @Test
    public void testManagerInfoInvalidSpecification() {
        zooKeeperConnectorFactory.setData(desiredSpec.getZkPath(), "INVALID");
        TopologyManagerInfoDto info = service.getTopologyManagerInfo();
        Assert.assertNotNull(info);
        Assert.assertNull(info.getTopologies());
        Assert.assertEquals(Status.DOWN, service.checkHealth().getStatus());
    }
}

