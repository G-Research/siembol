package uk.co.gresearch.nortem.enrichments.storm;

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
import uk.co.gresearch.nortem.common.filesystem.NortemFileSystem;
import uk.co.gresearch.nortem.common.filesystem.NortemFileSystemFactory;
import uk.co.gresearch.nortem.common.zookeper.ZookeperAttributes;
import uk.co.gresearch.nortem.common.zookeper.ZookeperConnectorFactory;
import uk.co.gresearch.nortem.common.zookeper.ZookeperConnector;
import uk.co.gresearch.nortem.enrichments.common.EnrichmentCommand;
import uk.co.gresearch.nortem.enrichments.storm.common.*;

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
     *        "path": "/nortem/tables/enrichment/test.json"
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

    private String errorTopic = "error";
    private String outputTopic = "output";

    private Tuple tuple;
    private OutputCollector collector;
    private EnrichmentExceptions exceptions;
    private EnrichmentCommands commands;
    MemoryTableEnrichmentBolt memoryTableBolt;
    ZookeperAttributes zookeperAttributes;
    StormEnrichmentAttributes attributes;
    ZookeperConnector zookeperConnector;
    ZookeperConnectorFactory zookeperConnectorFactory;
    NortemFileSystemFactory fileSystemFactory;
    NortemFileSystem fileSystem;
    ArgumentCaptor<Values> argumentEmitCaptor;

    @Before
    public void setUp() throws Exception {
        zookeperAttributes = new ZookeperAttributes();
        attributes = new StormEnrichmentAttributes();
        attributes.setEnrichingTablesAttributes(zookeperAttributes);

        exceptions = new EnrichmentExceptions();
        commands = new EnrichmentCommands();
        tuple = Mockito.mock(Tuple.class);
        collector = Mockito.mock(OutputCollector.class);
        argumentEmitCaptor = ArgumentCaptor.forClass(Values.class);
        zookeperConnectorFactory = Mockito.mock(ZookeperConnectorFactory.class);
        fileSystemFactory = Mockito.mock(NortemFileSystemFactory.class);
        fileSystem = Mockito.mock(NortemFileSystem.class);

        zookeperConnector = Mockito.mock(ZookeperConnector.class);
        when(zookeperConnectorFactory.createZookeperConnector(zookeperAttributes)).thenReturn(zookeperConnector);
        when(fileSystemFactory.create()).thenReturn(fileSystem);

        when(tuple.getStringByField(eq(EnrichmentTuples.EVENT.toString()))).thenReturn(event);
        when(tuple.getValueByField(eq(EnrichmentTuples.COMMANDS.toString()))).thenReturn(commands);
        when(tuple.getValueByField(eq(EnrichmentTuples.EXCEPTIONS.toString()))).thenReturn(exceptions);
        when(collector.emit(eq(tuple), argumentEmitCaptor.capture())).thenReturn(new ArrayList<>());
        when(zookeperConnector.getData()).thenReturn(tablesUpdate);
        when(fileSystem.openInputStream(anyString())).thenReturn(new ByteArrayInputStream(simpleOneField.getBytes()));

        memoryTableBolt = new MemoryTableEnrichmentBolt(attributes, zookeperConnectorFactory, fileSystemFactory);
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
