package uk.co.gresearch.siembol.enrichments.storm;

import org.adrianwalker.multilinestring.Multiline;
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
import uk.co.gresearch.siembol.common.model.StormEnrichmentAttributesDto;
import uk.co.gresearch.siembol.common.model.ZooKeeperAttributesDto;
import uk.co.gresearch.siembol.common.zookeeper.ZooKeeperConnectorFactory;
import uk.co.gresearch.siembol.common.zookeeper.ZooKeeperConnector;
import uk.co.gresearch.siembol.enrichments.common.EnrichmentCommand;
import uk.co.gresearch.siembol.enrichments.storm.common.*;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class MemoryTableEnrichmentBoltTest {
    /**
     * {"a": "string", "b": 1, "c": true}
     **/
    @Multiline
    public static String event;

    /**
     * {
     *     "hdfs_tables" : [
     *     {
     *       "name" : "test_table",
     *        "path": "/siembol/tables/enrichment/test.json"
     *     }]
     * }
     **/
    @Multiline
    public static String tablesUpdate;

    /**
     *
     * {
     *   "1.2.3.1" : { "is_malicious" : "true" },
     *   "1.2.3.2" : { "is_malicious" : "true"},
     *   "1.2.3.3" : {"is_malicious" : "false"},
     *   "1.2.3.4" : {"is_malicious" : "true"},
     *   "1.2.3.5" : {"is_malicious" : "true"}
     * }
     **/
    @Multiline
    public static String simpleOneField;

    private Tuple tuple;
    private OutputCollector collector;
    private EnrichmentExceptions exceptions;
    private EnrichmentCommands commands;
    MemoryTableEnrichmentBolt memoryTableBolt;
    ZooKeeperAttributesDto zookeperAttributes;
    StormEnrichmentAttributesDto attributes;
    ZooKeeperConnector zooKeeperConnector;
    ZooKeeperConnectorFactory zooKeeperConnectorFactory;
    SiembolFileSystemFactory fileSystemFactory;
    SiembolFileSystem fileSystem;
    ArgumentCaptor<Values> argumentEmitCaptor;

    @Before
    public void setUp() throws Exception {
        zookeperAttributes = new ZooKeeperAttributesDto();
        attributes = new StormEnrichmentAttributesDto();
        attributes.setEnrichingTablesAttributes(zookeperAttributes);

        exceptions = new EnrichmentExceptions();
        commands = new EnrichmentCommands();
        tuple = Mockito.mock(Tuple.class);
        collector = Mockito.mock(OutputCollector.class);
        argumentEmitCaptor = ArgumentCaptor.forClass(Values.class);
        zooKeeperConnectorFactory = Mockito.mock(ZooKeeperConnectorFactory.class);
        fileSystemFactory = Mockito.mock(SiembolFileSystemFactory.class);
        fileSystem = Mockito.mock(SiembolFileSystem.class);

        zooKeeperConnector = Mockito.mock(ZooKeeperConnector.class);
        when(zooKeeperConnectorFactory.createZookeeperConnector(zookeperAttributes)).thenReturn(zooKeeperConnector);
        when(fileSystemFactory.create()).thenReturn(fileSystem);

        when(tuple.getStringByField(eq(EnrichmentTuples.EVENT.toString()))).thenReturn(event);
        when(tuple.getValueByField(eq(EnrichmentTuples.COMMANDS.toString()))).thenReturn(commands);
        when(tuple.getValueByField(eq(EnrichmentTuples.EXCEPTIONS.toString()))).thenReturn(exceptions);
        when(collector.emit(eq(tuple), argumentEmitCaptor.capture())).thenReturn(new ArrayList<>());
        when(zooKeeperConnector.getData()).thenReturn(tablesUpdate);
        when(fileSystem.openInputStream(anyString())).thenReturn(new ByteArrayInputStream(simpleOneField.getBytes()));

        memoryTableBolt = new MemoryTableEnrichmentBolt(attributes, zooKeeperConnectorFactory, fileSystemFactory);
        memoryTableBolt.prepare(null, null, collector);
    }

    @Test
    public void testEmptyExceptionsEmptyCommands() {
        memoryTableBolt.execute(tuple);
        Values values = argumentEmitCaptor.getValue();
        Assert.assertNotNull(values);
        Assert.assertEquals(3, values.size());
        Assert.assertTrue(values.get(0) instanceof String);
        Assert.assertTrue(values.get(1) instanceof EnrichmentPairs);
        Assert.assertTrue(values.get(2) instanceof EnrichmentExceptions);
        Assert.assertEquals(event, values.get(0));
        Assert.assertTrue(((EnrichmentPairs)values.get(1)).isEmpty());
        Assert.assertTrue(((EnrichmentExceptions)values.get(2)).isEmpty());
    }

    @Test
    public void testNoneMptyExceptionsEmptyCommands() {
        exceptions.add("dummy1");
        exceptions.add("dummy2");
        memoryTableBolt.execute(tuple);
        Values values = argumentEmitCaptor.getValue();
        Assert.assertNotNull(values);
        Assert.assertEquals(3, values.size());
        Assert.assertTrue(values.get(0) instanceof String);
        Assert.assertTrue(values.get(1) instanceof EnrichmentPairs);
        Assert.assertTrue(values.get(2) instanceof EnrichmentExceptions);
        Assert.assertEquals(event, values.get(0));
        Assert.assertTrue(((EnrichmentPairs)values.get(1)).isEmpty());
        EnrichmentExceptions exceptions = (EnrichmentExceptions)values.get(2);
        Assert.assertEquals(2, exceptions.size());
        Assert.assertEquals("dummy1", exceptions.get(0));
        Assert.assertEquals("dummy2", exceptions.get(1));
    }

    @Test
    public void testEmptyExceptionsCommandNoTable() {
        EnrichmentCommand command = new EnrichmentCommand();
        commands.add(command);
        command.setTableName("invalid");
        memoryTableBolt.execute(tuple);
        Values values = argumentEmitCaptor.getValue();
        Assert.assertNotNull(values);
        Assert.assertEquals(3, values.size());
        Assert.assertTrue(values.get(0) instanceof String);
        Assert.assertTrue(values.get(1) instanceof EnrichmentPairs);
        Assert.assertTrue(values.get(2) instanceof EnrichmentExceptions);
        Assert.assertEquals(event, values.get(0));
        Assert.assertTrue(((EnrichmentPairs)values.get(1)).isEmpty());
        Assert.assertTrue(((EnrichmentExceptions)values.get(2)).isEmpty());
    }

    @Test
    public void testEmptyExceptionsCommandMatchTags() {
        EnrichmentCommand command = new EnrichmentCommand();
        commands.add(command);
        command.setTableName("test_table");
        command.setKey("1.2.3.1");
        command.setTags(new ArrayList<>(Arrays.asList(Pair.of("is_test", "true"))));
        memoryTableBolt.execute(tuple);
        Values values = argumentEmitCaptor.getValue();
        Assert.assertNotNull(values);
        Assert.assertEquals(3, values.size());
        Assert.assertTrue(values.get(0) instanceof String);
        Assert.assertTrue(values.get(1) instanceof EnrichmentPairs);
        Assert.assertTrue(values.get(2) instanceof EnrichmentExceptions);
        Assert.assertEquals(event, values.get(0));
        EnrichmentPairs enrichments = (EnrichmentPairs)values.get(1);
        Assert.assertEquals(1, enrichments.size());
        Assert.assertTrue(((EnrichmentExceptions)values.get(2)).isEmpty());
    }

    @Test
    public void testEmptyExceptionsCommandNoMatch() {
        EnrichmentCommand command = new EnrichmentCommand();
        commands.add(command);
        command.setTableName("test_table");
        command.setKey("unknown");
        memoryTableBolt.execute(tuple);
        Values values = argumentEmitCaptor.getValue();
        Assert.assertNotNull(values);
        Assert.assertEquals(3, values.size());
        Assert.assertTrue(values.get(0) instanceof String);
        Assert.assertTrue(values.get(1) instanceof EnrichmentPairs);
        Assert.assertTrue(values.get(2) instanceof EnrichmentExceptions);
        Assert.assertEquals(event, values.get(0));
        Assert.assertTrue(((EnrichmentPairs)values.get(1)).isEmpty());
        Assert.assertTrue(((EnrichmentExceptions)values.get(2)).isEmpty());
    }
}
