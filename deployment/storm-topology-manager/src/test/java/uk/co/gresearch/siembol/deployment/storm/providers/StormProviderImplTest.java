package uk.co.gresearch.siembol.deployment.storm.providers;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import uk.co.gresearch.siembol.common.utils.HttpProvider;
import uk.co.gresearch.siembol.deployment.storm.model.StormClusterDto;
import uk.co.gresearch.siembol.deployment.storm.model.StormResponseTopologyDto;

import java.io.IOException;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class StormProviderImplTest {

    private final String runningTopologies = """
            {"topologies":[{"id":"id1","name":"topology1"}]}
            """;

    HttpProvider httpProvider = mock(HttpProvider.class);
    StormProvider stormProvider;
    StormClusterDto cluster = new StormClusterDto();

    @Before
    public void setUp() {
        cluster.setKillWaitSeconds(0);
        stormProvider = new StormProviderImpl(httpProvider, cluster.getKillWaitSeconds());
    }

    @Test
    public void testKillTopologyFailNoResponse() throws IOException {
        when(httpProvider.post(any(), any())).thenReturn(null);
        boolean response = stormProvider.killTopology("topology");
        Assert.assertFalse(response);
    }

    @Test
    public void testKillTopologyStatusNotSuccess() throws IOException {
        when(httpProvider.post(any(), any())).thenReturn("{\"status\": \"failed\"}");
        boolean response = stormProvider.killTopology("topology");
        Assert.assertFalse(response);
    }

    @Test
    public void testKillTopologySuccess() throws IOException {
        when(httpProvider.post(any(), any())).thenReturn("{\"status\": \"success\"}");
        boolean response = stormProvider.killTopology("topology");
        Assert.assertTrue(response);
    }

    @Test
    public void testListTopologies() throws IOException {
        when(httpProvider.get(any())).thenReturn(runningTopologies);
        List<StormResponseTopologyDto> stormResponseTopologyDtos = stormProvider.listTopologies();
        Assert.assertEquals(1, stormResponseTopologyDtos.size());
    }
}