package uk.co.gresearch.siembol.configeditor.sync.service;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import uk.co.gresearch.siembol.common.model.EnrichmentTableDto;
import uk.co.gresearch.siembol.common.zookeeper.ZooKeeperConnector;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.when;

public class EnrichmentTablesProviderTest {
    private final String tablesUpdateServiceA = """
            {
               "enrichment_tables": [
                 {
                   "name": "test_table",
                   "path": "/siembol/tables/enrichment/test.json"
                 },
                 {
                   "name": "dns_table",
                   "path": "/siembol/tables/enrichment/dns.json"
                 }
               ]
            }
            """;

    private final String tablesUpdateServiceB = """
            {
               "enrichment_tables": [
                 {
                   "name": "users_table",
                   "path": "/siembol/tables/enrichment/users.json"
                 },
                 {
                   "name": "url_table",
                   "path": "/siembol/tables/enrichment/url.json"
                 },
                 {
                   "name": "ioc_table",
                   "path": "/siembol/tables/enrichment/ioc.json"
                 }
               ]
            }
            """;

    private Map<String, ZooKeeperConnector> zooKeeperConnectorMap;

    private EnrichmentTablesProviderImpl enrichmentTablesProvider;
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

    @Test(expected = IllegalStateException.class)
    public void initInvalidTables() {
        when(zooKeeperConnectorMap.get(serviceA).getData()).thenReturn("INVALID");
        enrichmentTablesProvider = new EnrichmentTablesProviderImpl(zooKeeperConnectorMap);
    }

    @Test
    public void getTablesOkServiceA() {
        ConfigEditorResult result = enrichmentTablesProvider.getEnrichmentTables(serviceA);
        Assert.assertEquals(ConfigEditorResult.StatusCode.OK, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes().getEnrichmentTables());
        Assert.assertEquals(2, result.getAttributes().getEnrichmentTables().size());
    }

    @Test
    public void getTablesOkServiceB() {
        ConfigEditorResult result = enrichmentTablesProvider.getEnrichmentTables(serviceB);
        Assert.assertEquals(ConfigEditorResult.StatusCode.OK, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes().getEnrichmentTables());
        Assert.assertEquals(3, result.getAttributes().getEnrichmentTables().size());
    }

    @Test
    public void getTableUnknownService() {
        ConfigEditorResult result = enrichmentTablesProvider.getEnrichmentTables("unknown");
        Assert.assertEquals(ConfigEditorResult.StatusCode.BAD_REQUEST, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes().getMessage());
    }

    @Test
    public void addTableOk() {
        EnrichmentTableDto table = new EnrichmentTableDto();
        table.setName("new_table");
        table.setPath("/dummy.json");
        ConfigEditorResult result = enrichmentTablesProvider.addEnrichmentTable(serviceA, table);

        Assert.assertEquals(ConfigEditorResult.StatusCode.OK, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes().getEnrichmentTables());
        Assert.assertEquals(3, result.getAttributes().getEnrichmentTables().size());
    }

    @Test
    public void addTableUnknownService() {
        EnrichmentTableDto table = new EnrichmentTableDto();
        table.setName("new_table");
        table.setPath("/dummy.json");
        ConfigEditorResult result = enrichmentTablesProvider.addEnrichmentTable("unknown", table);

        Assert.assertEquals(ConfigEditorResult.StatusCode.BAD_REQUEST, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes().getMessage());
    }

    @Test
    public void addTableExistingTable() {
        EnrichmentTableDto table = new EnrichmentTableDto();
        table.setName("test_table");
        table.setPath("/dummy.json");
        ConfigEditorResult result = enrichmentTablesProvider.addEnrichmentTable(serviceA, table);

        Assert.assertEquals(ConfigEditorResult.StatusCode.BAD_REQUEST, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes().getMessage());
    }

    @Test
    public void updateTableOk() {
        EnrichmentTableDto table = new EnrichmentTableDto();
        table.setName("test_table");
        table.setPath("/dummy.json");
        ConfigEditorResult result = enrichmentTablesProvider.updateEnrichmentTable(serviceA, table);

        Assert.assertEquals(ConfigEditorResult.StatusCode.OK, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes().getEnrichmentTables());
        Assert.assertEquals(2, result.getAttributes().getEnrichmentTables().size());
    }

    @Test
    public void updateTableUnknownService() {
        EnrichmentTableDto table = new EnrichmentTableDto();
        table.setName("test_table");
        table.setPath("/dummy.json");
        ConfigEditorResult result = enrichmentTablesProvider.updateEnrichmentTable("unknown", table);

        Assert.assertEquals(ConfigEditorResult.StatusCode.BAD_REQUEST, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes().getMessage());
    }

    @Test
    public void updateInvalidTable() {
        EnrichmentTableDto table = new EnrichmentTableDto();
        table.setPath("/dummy.json");
        ConfigEditorResult result = enrichmentTablesProvider.updateEnrichmentTable(serviceA, table);

        Assert.assertEquals(ConfigEditorResult.StatusCode.BAD_REQUEST, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes().getMessage());
    }

    @Test
    public void updateTableNonExistingTable() {
        EnrichmentTableDto table = new EnrichmentTableDto();
        table.setName("new_table");
        table.setPath("/dummy.json");
        ConfigEditorResult result = enrichmentTablesProvider.updateEnrichmentTable(serviceA, table);

        Assert.assertEquals(ConfigEditorResult.StatusCode.BAD_REQUEST, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes().getMessage());
    }
}
