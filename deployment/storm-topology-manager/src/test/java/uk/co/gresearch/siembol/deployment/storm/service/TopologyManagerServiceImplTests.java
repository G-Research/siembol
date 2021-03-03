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
import uk.co.gresearch.siembol.common.model.StormTopologiesDto;
import uk.co.gresearch.siembol.common.model.ZookeeperAttributesDto;
import uk.co.gresearch.siembol.common.testing.TestingZookeeperConnectorFactory;
import uk.co.gresearch.siembol.common.zookeper.ZookeeperConnector;
import uk.co.gresearch.siembol.deployment.storm.model.StormResponseDto;
import uk.co.gresearch.siembol.deployment.storm.providers.KubernetesProvider;
import uk.co.gresearch.siembol.deployment.storm.providers.StormProvider;

import static org.mockito.Mockito.*;

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
    ZookeeperConnector desiredZookeeper;
    ZookeeperConnector savedZookeper;

    public TopologyManagerServiceImplTests() throws JsonProcessingException {
    }

    @Before
    public void setUp() {
        ZookeeperAttributesDto desiredSpec = new ZookeeperAttributesDto();
        ZookeeperAttributesDto savedSpec = new ZookeeperAttributesDto();
        desiredSpec.setZkPath("/siembol/desired");
        savedSpec.setZkPath("/siembol/saved");

        desiredZookeeper = new TestingZookeeperConnectorFactory().createZookeeperConnector(desiredSpec);
        savedZookeper = new TestingZookeeperConnectorFactory().createZookeeperConnector(savedSpec);
        service = new TopologyManagerServiceImpl(stormProvider, kubernetesProvider, desiredZookeeper, savedZookeper, 0);
    }

    @Test
    public void testSyncStart1() throws Exception {
        when(stormProvider.listTopologies()).thenReturn(stormTopologies.getTopologies());
        when(stormProvider.killTopology(any())).thenReturn(true);

        stormTopologies = READER_STORM.readValue(runningTopologies);
        StormTopologiesDto saved = READER.readValue(topologies2);
        StormTopologiesDto desired = READER.readValue(topologies1);

        desiredZookeeper.setData(WRITER.writeValueAsString(saved));
        savedZookeper.setData(WRITER.writeValueAsString(desired));

        Thread.sleep(100);

        verify(stormProvider, times(1)).listTopologies();
        verify(kubernetesProvider, times(1)).createOrReplaceJob(any());

        StormTopologiesDto desiredState = READER.readValue(desiredZookeeper.getData());
        StormTopologiesDto savedState = READER.readValue(savedZookeper.getData());

        Assert.assertTrue(
                EqualsBuilder.reflectionEquals(desiredState.getTopologies(), savedState.getTopologies())
        );
    }

    @Test
    public void testSyncStart2() throws Exception {
        when(stormProvider.listTopologies()).thenReturn(stormTopologies.getTopologies());
        when(stormProvider.killTopology(any())).thenReturn(true);

        stormTopologies = READER_STORM.readValue(runningTopologies);
        StormTopologiesDto saved = READER.readValue(topologies1);
        StormTopologiesDto desired = READER.readValue(topologies4);

        savedZookeper.setData(WRITER.writeValueAsString(saved));
        desiredZookeeper.setData(WRITER.writeValueAsString(desired));

        Thread.sleep(100);

        verify(stormProvider, times(1)).listTopologies();
        verify(kubernetesProvider, times(3)).createOrReplaceJob(any());

        StormTopologiesDto desiredState = READER.readValue(desiredZookeeper.getData());
        StormTopologiesDto savedState = READER.readValue(savedZookeper.getData());

        Assert.assertTrue(
                EqualsBuilder.reflectionEquals(desiredState.getTopologies(), savedState.getTopologies())
        );
    }

    @Test
    public void testSyncUpgradeTopology1() throws Exception {
        when(stormProvider.listTopologies()).thenReturn(stormTopologies.getTopologies());
        when(stormProvider.killTopology(any())).thenReturn(true);

        stormTopologies = READER_STORM.readValue(runningTopologies);
        StormTopologiesDto saved = READER.readValue(topologies1);
        StormTopologiesDto desired = READER.readValue(topologies1changed);

        savedZookeper.setData(WRITER.writeValueAsString(saved));
        desiredZookeeper.setData(WRITER.writeValueAsString(desired));

        Thread.sleep(100);

        verify(stormProvider, times(1)).listTopologies();
        verify(stormProvider, times(1)).killTopology("id1");
        verify(kubernetesProvider, times(1)).createOrReplaceJob(any());

        StormTopologiesDto desiredState = READER.readValue(desiredZookeeper.getData());
        StormTopologiesDto savedState = READER.readValue(savedZookeper.getData());

        Assert.assertTrue(
                EqualsBuilder.reflectionEquals(desiredState.getTopologies(), savedState.getTopologies())
        );
    }

    @Test
    public void testSyncUpgradeTopology2() throws Exception {
        when(stormProvider.listTopologies()).thenReturn(stormTopologies.getTopologies());
        when(stormProvider.killTopology(any())).thenReturn(true);

        stormTopologies = READER_STORM.readValue(runningTopologies);
        StormTopologiesDto saved = READER.readValue(topologies1);
        StormTopologiesDto desired = READER.readValue(topologies4changed);

        savedZookeper.setData(WRITER.writeValueAsString(saved));
        desiredZookeeper.setData(WRITER.writeValueAsString(desired));

        Thread.sleep(100);

        verify(stormProvider, times(1)).listTopologies();
        verify(stormProvider, times(1)).killTopology("id1");
        verify(kubernetesProvider, times(4)).createOrReplaceJob(any());

        StormTopologiesDto desiredState = READER.readValue(desiredZookeeper.getData());
        StormTopologiesDto savedState = READER.readValue(savedZookeper.getData());

        Assert.assertTrue(
                EqualsBuilder.reflectionEquals(desiredState.getTopologies(), savedState.getTopologies())
        );
    }

    @Test
    public void testDeletedTopology1() throws Exception {
        stormTopologies = READER_STORM.readValue(runningTopologies2);
        when(stormProvider.listTopologies()).thenReturn(stormTopologies.getTopologies());
        when(stormProvider.killTopology(any())).thenReturn(true);

        StormTopologiesDto saved = READER.readValue(topologies4);
        StormTopologiesDto desired = READER.readValue(empty);


        savedZookeper.setData(WRITER.writeValueAsString(saved));
        desiredZookeeper.setData(WRITER.writeValueAsString(desired));

        Thread.sleep(100);

        verify(stormProvider, times(1)).listTopologies();
        verify(stormProvider, times(4)).killTopology(any());

        StormTopologiesDto desiredState = READER.readValue(desiredZookeeper.getData());
        StormTopologiesDto savedState = READER.readValue(savedZookeper.getData());

        Assert.assertTrue(
                EqualsBuilder.reflectionEquals(desiredState.getTopologies(), savedState.getTopologies())
        );
    }

    @Test
    public void testStormTopologyNameConflict1() throws Exception {
        stormTopologies = READER_STORM.readValue(runningTopologies);
        when(stormProvider.listTopologies()).thenReturn(stormTopologies.getTopologies());
        when(stormProvider.killTopology(any())).thenReturn(true);

        StormTopologiesDto saved = READER.readValue(empty);
        StormTopologiesDto desired = READER.readValue(topologies1);

        savedZookeper.setData(WRITER.writeValueAsString(saved));
        desiredZookeeper.setData(WRITER.writeValueAsString(desired));

        Thread.sleep(100);

        verify(stormProvider, times(1)).listTopologies();
        verify(stormProvider, times(1)).killTopology("id1");
        verify(kubernetesProvider, times(1)).createOrReplaceJob(any());

        StormTopologiesDto desiredState = READER.readValue(desiredZookeeper.getData());
        StormTopologiesDto savedState = READER.readValue(savedZookeper.getData());

        Assert.assertTrue(
                EqualsBuilder.reflectionEquals(desiredState.getTopologies(), savedState.getTopologies())
        );
    }

    @Test
    public void testTopologyIsMissingFromStorm() throws Exception {
        stormTopologies = READER_STORM.readValue(runningTopologies);
        when(stormProvider.listTopologies()).thenReturn(stormTopologies.getTopologies());
        when(stormProvider.killTopology(any())).thenReturn(true);

        StormTopologiesDto saved = READER.readValue(topologies2);
        StormTopologiesDto desired = READER.readValue(topologies2);

        savedZookeper.setData(WRITER.writeValueAsString(saved));
        desiredZookeeper.setData(WRITER.writeValueAsString(desired));

        Thread.sleep(100);

        verify(stormProvider, times(1)).listTopologies();
        verify(kubernetesProvider, times(1)).createOrReplaceJob(any());

        StormTopologiesDto desiredState = READER.readValue(desiredZookeeper.getData());
        StormTopologiesDto savedState = READER.readValue(savedZookeper.getData());

        Assert.assertTrue(
                EqualsBuilder.reflectionEquals(desiredState.getTopologies(), savedState.getTopologies())
        );
    }
}

