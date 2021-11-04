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

import uk.co.gresearch.siembol.common.storm.KafkaBatchWriterMessages;
import uk.co.gresearch.siembol.enrichments.storm.common.EnrichmentTuples;
import uk.co.gresearch.siembol.enrichments.storm.common.EnrichmentPairs;
import uk.co.gresearch.siembol.enrichments.storm.common.EnrichmentExceptions;
import uk.co.gresearch.siembol.common.model.StormEnrichmentAttributesDto;

import java.util.*;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;


public class EnrichmentMergerBoltTest {
    private final String event = """
     {"a": "string","b": 1,"c": true}
     """;

    private final String enrichedEventPrefix = """
     {"a":"string","b":1,"c":true,"siembol_enriching_ts":
     """;

    private final String errorTopic = "error";
    private final String outputTopic = "output";

    private Tuple tuple;
    private OutputCollector collector;
    private EnrichmentExceptions exceptions;
    private EnrichmentPairs enrichments;
    EnrichmentMergerBolt mergerBolt;
    ArgumentCaptor<Values> argumentEmitCaptor;

    @Before
    public void setUp() {
        exceptions = new EnrichmentExceptions();
        enrichments = new EnrichmentPairs();
        tuple = Mockito.mock(Tuple.class);
        collector = Mockito.mock(OutputCollector.class);
        argumentEmitCaptor = ArgumentCaptor.forClass(Values.class);

        when(tuple.getStringByField(eq(EnrichmentTuples.EVENT.toString()))).thenReturn(event);
        when(tuple.getValueByField(eq(EnrichmentTuples.ENRICHMENTS.toString()))).thenReturn(enrichments);
        when(tuple.getValueByField(eq(EnrichmentTuples.EXCEPTIONS.toString()))).thenReturn(exceptions);
        when(collector.emit(eq(tuple), argumentEmitCaptor.capture())).thenReturn(new ArrayList<>());

        StormEnrichmentAttributesDto attributes = new StormEnrichmentAttributesDto();
        attributes.setEnrichingErrorTopic(errorTopic);
        attributes.setEnrichingOutputTopic(outputTopic);
        mergerBolt = new EnrichmentMergerBolt(attributes);
        mergerBolt.prepare(null, null, collector);
    }

    @Test
    public void testEmptyExceptionsEmptyEnrichments() {
        mergerBolt.execute(tuple);
        Values values = argumentEmitCaptor.getValue();
        Assert.assertNotNull(values);
        Assert.assertEquals(1, values.size());
        Assert.assertTrue(values.get(0) instanceof KafkaBatchWriterMessages);
        KafkaBatchWriterMessages messages = (KafkaBatchWriterMessages)values.get(0);

        Assert.assertEquals(outputTopic, messages.get(0).getTopic());
        Assert.assertTrue(messages.get(0).getMessage().contains(enrichedEventPrefix.trim()));
    }

    @Test
    public void testNonEmptyExceptionsEmptyEnrichments() {
        exceptions.add("dummy1");
        exceptions.add("dummy2");
        mergerBolt.execute(tuple);
        Values values = argumentEmitCaptor.getValue();
        Assert.assertNotNull(values);
        Assert.assertEquals(1, values.size());
        Assert.assertTrue(values.get(0) instanceof KafkaBatchWriterMessages);
        KafkaBatchWriterMessages messages = (KafkaBatchWriterMessages)values.get(0);

        Assert.assertEquals(outputTopic, messages.get(0).getTopic());
        Assert.assertFalse(messages.get(0).getMessage().isEmpty());

        Assert.assertEquals(errorTopic, messages.get(1).getTopic());
        Assert.assertEquals("dummy1", messages.get(1).getMessage());

        Assert.assertEquals(errorTopic, messages.get(2).getTopic());
        Assert.assertEquals("dummy2", messages.get(2).getMessage());
    }

    @Test
    public void testEmptyExceptionsNonEmptyEnrichments() {
        exceptions.add("dummy1");
        exceptions.add("dummy2");
        enrichments.add(Pair.of("test", "enrichment"));
        mergerBolt.execute(tuple);

        Values values = argumentEmitCaptor.getValue();
        Assert.assertNotNull(values);
        Assert.assertEquals(1, values.size());
        Assert.assertTrue(values.get(0) instanceof KafkaBatchWriterMessages);
        KafkaBatchWriterMessages messages = (KafkaBatchWriterMessages)values.get(0);

        Assert.assertEquals(outputTopic, messages.get(0).getTopic());

        Assert.assertEquals(errorTopic, messages.get(1).getTopic());
        Assert.assertEquals("dummy1", messages.get(1).getMessage());

        Assert.assertEquals(errorTopic, messages.get(2).getTopic());
        Assert.assertEquals("dummy2", messages.get(2).getMessage());
    }

    @Test
    public void testExceptionsNonEmptyEnrichments() {
        enrichments.add(Pair.of("test", "enrichment"));
        mergerBolt.execute(tuple);

        Values values = argumentEmitCaptor.getValue();
        Assert.assertNotNull(values);
        Assert.assertEquals(1, values.size());
        Assert.assertTrue(values.get(0) instanceof KafkaBatchWriterMessages);
        KafkaBatchWriterMessages messages = (KafkaBatchWriterMessages)values.get(0);

        Assert.assertEquals(outputTopic, messages.get(0).getTopic());
        Assert.assertTrue(messages.get(0).getMessage().contains("\"test\":\"enrichment\""));
    }

    @Test
    public void testInvalidEvent() {
        when(tuple.getStringByField(eq(EnrichmentTuples.EVENT.toString()))).thenReturn("INVALID");
        mergerBolt.execute(tuple);

        Values values = argumentEmitCaptor.getValue();
        Assert.assertNotNull(values);
        Assert.assertEquals(1, values.size());
        Assert.assertTrue(values.get(0) instanceof KafkaBatchWriterMessages);
        KafkaBatchWriterMessages messages = (KafkaBatchWriterMessages)values.get(0);

        Assert.assertEquals(outputTopic, messages.get(0).getTopic());
        Assert.assertEquals("INVALID", messages.get(0).getMessage());

        Assert.assertTrue(messages.get(1).getMessage().contains("\"error_type\":\"enrichment_error\""));
        Assert.assertEquals(errorTopic, messages.get(1).getTopic());
    }
}
