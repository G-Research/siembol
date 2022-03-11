package uk.co.gresearch.siembol.enrichments.storm;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import uk.co.gresearch.siembol.common.filesystem.SiembolFileSystem;
import uk.co.gresearch.siembol.common.filesystem.SiembolFileSystemFactory;
import uk.co.gresearch.siembol.common.metrics.SiembolMetrics;
import uk.co.gresearch.siembol.common.metrics.test.StormMetricsTestRegistrarFactoryImpl;
import uk.co.gresearch.siembol.common.model.StormEnrichmentAttributesDto;
import uk.co.gresearch.siembol.common.model.ZooKeeperAttributesDto;
import uk.co.gresearch.siembol.common.storm.SiembolMetricsCounters;
import uk.co.gresearch.siembol.common.testing.TestingZooKeeperConnectorFactory;
import uk.co.gresearch.siembol.enrichments.common.EnrichmentCommand;
import uk.co.gresearch.siembol.enrichments.storm.common.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

public class MemoryTableEnrichmentBoltTest {
    private final String event = """
            {"a": "string", "b": 1, "c": true}
            """;

    private final String tablesUpdate = """
            {
                "enrichment_tables" : [
                {
                  "name" : "test_table",
                  "path": "/siembol/tables/enrichment/test.json"
                }]
            }
            """;

    private final String tablesUpdateSame = """
            {
                "enrichment_tables" : [
                {
                  "name" : "test_table",
                  "path": "/siembol/tables/enrichment/test.json"
                },
                {
                  "name" : "test_table_2",
                  "path": "/siembol/tables/enrichment/test2.json"
                }]
            }
            """;

    private final String tablesUpdateDifferent = """
            {
                "enrichment_tables" : [
                {
                  "name" : "test_table",
                  "path": "/siembol/tables/enrichment/different_test.json"
                },
                {
                  "name" : "test_table_2",
                  "path": "/siembol/tables/enrichment/test2.json"
                }]
            }
            """;

    private final String simpleOneField = """    
            {
              "1.2.3.1" : { "is_malicious" : "true" },
              "1.2.3.2" : { "is_malicious" : "true"},
              "1.2.3.3" : { "is_malicious" : "false"},
              "1.2.3.4" : { "is_malicious" : "true"},
              "1.2.3.5" : { "is_malicious" : "true"}
            }
            """;

    private Tuple tuple;
    private OutputCollector collector;
    private EnrichmentExceptions exceptions;
    private EnrichmentCommands commands;
    private MemoryTableEnrichmentBolt memoryTableBolt;
    private final String zooKeeperPath = "path";
    private ZooKeeperAttributesDto zooKeeperAttributes;
    private StormEnrichmentAttributesDto attributes;
    private final TestingZooKeeperConnectorFactory zooKeeperConnectorFactory = new TestingZooKeeperConnectorFactory();
    private SiembolFileSystemFactory fileSystemFactory;
    private SiembolFileSystem fileSystem;
    private ArgumentCaptor<Values> argumentEmitCaptor;
    private StormMetricsTestRegistrarFactoryImpl metricsTestRegistrarFactory;

    @Before
    public void setUp() throws Exception {
        zooKeeperAttributes = new ZooKeeperAttributesDto();
        zooKeeperAttributes.setZkPath(zooKeeperPath);
        zooKeeperConnectorFactory.setData(zooKeeperPath, tablesUpdate);
        attributes = new StormEnrichmentAttributesDto();
        attributes.setEnrichingTablesAttributes(zooKeeperAttributes);

        exceptions = new EnrichmentExceptions();
        commands = new EnrichmentCommands();
        tuple = Mockito.mock(Tuple.class);
        collector = Mockito.mock(OutputCollector.class);
        argumentEmitCaptor = ArgumentCaptor.forClass(Values.class);
        fileSystemFactory = Mockito.mock(SiembolFileSystemFactory.class);
        fileSystem = Mockito.mock(SiembolFileSystem.class);

        when(fileSystemFactory.create()).thenReturn(fileSystem);

        when(tuple.getStringByField(eq(EnrichmentTuples.EVENT.toString()))).thenReturn(event);
        when(tuple.getValueByField(eq(EnrichmentTuples.COMMANDS.toString()))).thenReturn(commands);
        when(tuple.getValueByField(eq(EnrichmentTuples.EXCEPTIONS.toString()))).thenReturn(exceptions);
        when(collector.emit(eq(tuple), argumentEmitCaptor.capture())).thenReturn(new ArrayList<>());

        when(fileSystem.openInputStream(eq("/siembol/tables/enrichment/test.json")))
                .thenReturn(new ByteArrayInputStream(simpleOneField.getBytes()));
        when(fileSystem.openInputStream(eq("/siembol/tables/enrichment/test2.json")))
                .thenReturn(new ByteArrayInputStream(simpleOneField.getBytes()));
        when(fileSystem.openInputStream(eq("/siembol/tables/enrichment/different_test.json")))
                .thenReturn(new ByteArrayInputStream(simpleOneField.getBytes()));

        metricsTestRegistrarFactory = new StormMetricsTestRegistrarFactoryImpl();

        memoryTableBolt = new MemoryTableEnrichmentBolt(attributes,
                zooKeeperConnectorFactory,
                fileSystemFactory,
                metricsTestRegistrarFactory::createSiembolMetricsRegistrar);
        memoryTableBolt.prepare(null, null, collector);
    }

    @Test
    public void testEmptyExceptionsEmptyCommands() {
        memoryTableBolt.execute(tuple);
        Values values = argumentEmitCaptor.getValue();
        Assert.assertNotNull(values);
        Assert.assertEquals(4, values.size());
        Assert.assertTrue(values.get(0) instanceof String);
        Assert.assertTrue(values.get(1) instanceof EnrichmentPairs);
        Assert.assertTrue(values.get(2) instanceof EnrichmentExceptions);
        Assert.assertTrue(values.get(3) instanceof SiembolMetricsCounters);
        Assert.assertEquals(event, values.get(0));
        Assert.assertTrue(((EnrichmentPairs)values.get(1)).isEmpty());
        Assert.assertTrue(((EnrichmentExceptions)values.get(2)).isEmpty());
        Assert.assertTrue(((SiembolMetricsCounters)values.get(3)).isEmpty());
        var counters = (SiembolMetricsCounters)values.get(3);
        Assert.assertTrue(counters.isEmpty());
    }

    @Test
    public void testNonemptyExceptionsEmptyCommands() {
        exceptions.add("dummy1");
        exceptions.add("dummy2");
        memoryTableBolt.execute(tuple);
        Values values = argumentEmitCaptor.getValue();
        Assert.assertNotNull(values);
        Assert.assertEquals(4, values.size());
        Assert.assertTrue(values.get(0) instanceof String);
        Assert.assertTrue(values.get(1) instanceof EnrichmentPairs);
        Assert.assertTrue(values.get(2) instanceof EnrichmentExceptions);
        Assert.assertTrue(values.get(3) instanceof SiembolMetricsCounters);
        Assert.assertEquals(event, values.get(0));
        Assert.assertTrue(((EnrichmentPairs)values.get(1)).isEmpty());
        EnrichmentExceptions exceptions = (EnrichmentExceptions)values.get(2);
        Assert.assertEquals(2, exceptions.size());
        Assert.assertEquals("dummy1", exceptions.get(0));
        Assert.assertEquals("dummy2", exceptions.get(1));
        var counters = (SiembolMetricsCounters)values.get(3);
        Assert.assertTrue(counters.isEmpty());
    }

    @Test
    public void testEmptyExceptionsCommandNoTable() {
        EnrichmentCommand command = new EnrichmentCommand();
        commands.add(command);
        command.setTableName("invalid");
        command.setRuleName("test_rule");
        memoryTableBolt.execute(tuple);
        Values values = argumentEmitCaptor.getValue();
        Assert.assertNotNull(values);
        Assert.assertEquals(4, values.size());
        Assert.assertTrue(values.get(0) instanceof String);
        Assert.assertTrue(values.get(1) instanceof EnrichmentPairs);
        Assert.assertTrue(values.get(2) instanceof EnrichmentExceptions);
        Assert.assertTrue(values.get(3) instanceof SiembolMetricsCounters);
        Assert.assertEquals(event, values.get(0));
        Assert.assertTrue(((EnrichmentPairs)values.get(1)).isEmpty());
        Assert.assertTrue(((EnrichmentExceptions)values.get(2)).isEmpty());
        var counters = (SiembolMetricsCounters)values.get(3);
        Assert.assertTrue(counters.isEmpty());
    }

    @Test
    public void testEmptyExceptionsCommandMatchTags() {
        EnrichmentCommand command = new EnrichmentCommand();
        commands.add(command);
        command.setTableName("test_table");
        command.setKey("1.2.3.1");
        command.setTags(new ArrayList<>(Arrays.asList(Pair.of("is_test", "true"))));
        command.setRuleName("test_rule");
        memoryTableBolt.execute(tuple);
        Values values = argumentEmitCaptor.getValue();
        Assert.assertNotNull(values);
        Assert.assertEquals(4, values.size());
        Assert.assertTrue(values.get(0) instanceof String);
        Assert.assertTrue(values.get(1) instanceof EnrichmentPairs);
        Assert.assertTrue(values.get(2) instanceof EnrichmentExceptions);
        Assert.assertTrue(values.get(3) instanceof SiembolMetricsCounters);
        Assert.assertEquals(event, values.get(0));
        EnrichmentPairs enrichments = (EnrichmentPairs)values.get(1);
        Assert.assertEquals(1, enrichments.size());
        Assert.assertTrue(((EnrichmentExceptions)values.get(2)).isEmpty());
        var counters = (SiembolMetricsCounters)values.get(3);
        Assert.assertEquals(2, counters.size());
        Assert.assertTrue(counters.contains(SiembolMetrics.ENRICHMENT_TABLE_APPLIED.getMetricName("test_table")));
        Assert.assertTrue(counters.contains(SiembolMetrics.ENRICHMENT_RULE_APPLIED.getMetricName("test_rule")));
    }

    @Test
    public void testEmptyExceptionsCommandNoMatch() {
        EnrichmentCommand command = new EnrichmentCommand();
        commands.add(command);
        command.setTableName("test_table");
        command.setKey("unknown");
        command.setRuleName("test_rule");
        memoryTableBolt.execute(tuple);
        Values values = argumentEmitCaptor.getValue();
        Assert.assertNotNull(values);
        Assert.assertEquals(4, values.size());
        Assert.assertTrue(values.get(0) instanceof String);
        Assert.assertTrue(values.get(1) instanceof EnrichmentPairs);
        Assert.assertTrue(values.get(2) instanceof EnrichmentExceptions);
        Assert.assertTrue(values.get(3) instanceof SiembolMetricsCounters);
        Assert.assertEquals(event, values.get(0));
        Assert.assertTrue(((EnrichmentPairs)values.get(1)).isEmpty());
        Assert.assertTrue(((EnrichmentExceptions)values.get(2)).isEmpty());
        Assert.assertTrue(((SiembolMetricsCounters)values.get(3)).isEmpty());
    }

    @Test
    public void updateTablesWithSame() throws Exception {
        zooKeeperConnectorFactory.getZooKeeperConnector(zooKeeperPath).setData(tablesUpdateSame);
        Mockito.verify(fileSystem, times(1))
                .openInputStream(eq("/siembol/tables/enrichment/test.json"));
        Mockito.verify(fileSystem, times(1))
                .openInputStream(eq("/siembol/tables/enrichment/test2.json"));
        Assert.assertEquals(1,
                metricsTestRegistrarFactory
                        .getCounterValue(SiembolMetrics.ENRICHMENT_TABLE_UPDATED.getMetricName("test_table")));
        Assert.assertEquals(1,
                metricsTestRegistrarFactory
                        .getCounterValue(SiembolMetrics.ENRICHMENT_TABLE_UPDATED.getMetricName("test_table_2")));
    }

    @Test
    public void updateTablesWithDifferent() throws Exception {
        zooKeeperConnectorFactory.getZooKeeperConnector(zooKeeperPath).setData(tablesUpdateDifferent);
        Mockito.verify(fileSystem, times(1))
                .openInputStream(eq("/siembol/tables/enrichment/test.json"));
        Mockito.verify(fileSystem, times(1))
                .openInputStream(eq("/siembol/tables/enrichment/different_test.json"));
        Mockito.verify(fileSystem, times(1))
                .openInputStream(eq("/siembol/tables/enrichment/test2.json"));
        Assert.assertEquals(2,
                metricsTestRegistrarFactory
                        .getCounterValue(SiembolMetrics.ENRICHMENT_TABLE_UPDATED.getMetricName("test_table")));
        Assert.assertEquals(1,
                metricsTestRegistrarFactory
                        .getCounterValue(SiembolMetrics.ENRICHMENT_TABLE_UPDATED.getMetricName("test_table_2")));
    }

    @Test
    public void updateTablesEmpty() throws IOException {
        fileSystem = Mockito.mock(SiembolFileSystem.class);
        when(fileSystemFactory.create()).thenReturn(fileSystem);
        when(fileSystem.openInputStream(anyString())).thenReturn(new ByteArrayInputStream(simpleOneField.getBytes()));
        zooKeeperConnectorFactory.setData(zooKeeperPath, "{}");
        memoryTableBolt = new MemoryTableEnrichmentBolt(attributes,
                zooKeeperConnectorFactory,
                fileSystemFactory,
                metricsTestRegistrarFactory::createSiembolMetricsRegistrar);
        memoryTableBolt.prepare(null, null, collector);
        Mockito.verify(fileSystem, times(0)).openInputStream(anyString());
    }

    @Test(expected = IllegalStateException.class)
    public void prepareInvalidAttributes() throws IOException {
        attributes.setEnrichingTablesAttributes(null);
        fileSystem = Mockito.mock(SiembolFileSystem.class);
        when(fileSystemFactory.create()).thenReturn(fileSystem);
        when(fileSystem.openInputStream(anyString())).thenReturn(new ByteArrayInputStream(simpleOneField.getBytes()));
        memoryTableBolt = new MemoryTableEnrichmentBolt(attributes,
                zooKeeperConnectorFactory,
                fileSystemFactory,
                metricsTestRegistrarFactory::createSiembolMetricsRegistrar);
        memoryTableBolt.prepare(null, null, collector);
        Mockito.verify(fileSystem, times(0)).openInputStream(anyString());
    }

    @Test
    public void updateTablesWithDifferentAndException() throws Exception {
        when(fileSystem.openInputStream(eq("/siembol/tables/enrichment/different_test.json")))
                .thenThrow(new IllegalArgumentException());
        zooKeeperConnectorFactory.getZooKeeperConnector(zooKeeperPath).setData(tablesUpdateDifferent);

        Mockito.verify(fileSystem, times(1))
                .openInputStream(eq("/siembol/tables/enrichment/test.json"));
        Mockito.verify(fileSystem, times(1))
                .openInputStream(eq("/siembol/tables/enrichment/different_test.json"));
        Mockito.verify(fileSystem, times(1))
                .openInputStream(eq("/siembol/tables/enrichment/test2.json"));
        Assert.assertEquals(1,
                metricsTestRegistrarFactory
                        .getCounterValue(SiembolMetrics.ENRICHMENT_TABLE_UPDATED.getMetricName("test_table")));
        Assert.assertEquals(1,
                metricsTestRegistrarFactory
                        .getCounterValue(SiembolMetrics.ENRICHMENT_TABLE_UPDATE_ERROR.getMetricName("test_table")));
        Assert.assertEquals(1,
                metricsTestRegistrarFactory
                        .getCounterValue(SiembolMetrics.ENRICHMENT_TABLE_UPDATED.getMetricName("test_table_2")));
    }

    @Test(expected = RuntimeException.class)
    public void updateTablesInvalid() throws Exception {
        Mockito.verify(fileSystem, times(1))
                .openInputStream(eq("/siembol/tables/enrichment/test.json"));
        zooKeeperConnectorFactory.getZooKeeperConnector(zooKeeperPath).setData("INVALID");
    }
}
