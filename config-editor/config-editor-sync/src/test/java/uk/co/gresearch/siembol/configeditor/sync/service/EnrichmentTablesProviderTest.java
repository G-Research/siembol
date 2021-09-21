package uk.co.gresearch.siembol.configeditor.sync.service;

import org.adrianwalker.multilinestring.Multiline;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import uk.co.gresearch.siembol.common.model.StormTopologyDto;
import uk.co.gresearch.siembol.common.zookeeper.ZooKeeperConnector;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.mockito.Mockito.when;

public class EnrichmentTablesProviderTest {
    /**
     *
     **/
    @Multiline
    public static String tablesUpdateServiceA;

    /**
     *
     **/
    @Multiline
    public static String tablesUpdateServiceB;

    private Map<String, ZooKeeperConnector> zooKeeperConnectorMap;

    private EnrichmentTablesProviderImpl enrichmentTablesProvider;
    private Set<String> services;
    private List<StormTopologyDto> topologiesToUpdate;
    private final String serviceA = "a";
    private final String serviceB = "b";

    @Before
    public void setUp() throws IOException {
        zooKeeperConnectorMap = new HashMap<>();
        zooKeeperConnectorMap.put(serviceA, Mockito.mock(ZooKeeperConnector.class));
        zooKeeperConnectorMap.put(serviceB, Mockito.mock(ZooKeeperConnector.class));

        when(zooKeeperConnectorMap.get(serviceA).getData()).thenReturn(tablesUpdateServiceA);
        when(zooKeeperConnectorMap.get(serviceB).getData()).thenReturn(tablesUpdateServiceB);

        enrichmentTablesProvider = new EnrichmentTablesProviderImpl(zooKeeperConnectorMap);

    }


    @Test
    public void getTablesOk() {
        ConfigEditorResult result = enrichmentTablesProvider.getEnrichmentTables(serviceA);
        Assert.assertEquals(ConfigEditorResult.StatusCode.OK, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes().getEnrichmentTables());
        //Assert.assertEquals(3, result.getAttributes().getTopologies().size());
    }
}
