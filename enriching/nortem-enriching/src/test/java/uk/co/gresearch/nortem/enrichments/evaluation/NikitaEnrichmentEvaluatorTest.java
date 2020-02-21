package uk.co.gresearch.nortem.enrichments.evaluation;


import org.adrianwalker.multilinestring.Multiline;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import uk.co.gresearch.nortem.enrichments.common.EnrichmentCommand;

import uk.co.gresearch.nortem.enrichments.common.EnrichmentResult;
import uk.co.gresearch.nortem.nikita.common.EvaluationResult;
import uk.co.gresearch.nortem.nikita.common.NikitaAttributes;
import uk.co.gresearch.nortem.nikita.common.NikitaEngine;
import uk.co.gresearch.nortem.nikita.common.NikitaResult;

import java.util.*;

import static org.mockito.Mockito.when;
import static uk.co.gresearch.nortem.enrichments.common.EnrichmentResult.StatusCode.ERROR;
import static uk.co.gresearch.nortem.enrichments.common.EnrichmentResult.StatusCode.OK;


public class NikitaEnrichmentEvaluatorTest {
    /**
     * {
     *   "timestamp" : 1,
     *   "dummy_bool" : true,
     *   "dummy_str" : "test",
     *   "a" : "conflict"
     * }
     **/
    @Multiline
    public static String simpleEvent;

    private NikitaAttributes nikitaAttributes;
    private NikitaResult nikitaResult;
    private NikitaEngine engine;
    private NikitaEnrichmentEvaluator evaluator;
    private List<Map<String, Object>> outputEevents;
    private EnrichmentCommand command;
    private ArrayList<Pair<String, String>> enrichments;
    private ArrayList<Pair<String, String>> tags;

    @Before
    public void setUp() {
        nikitaAttributes = new NikitaAttributes();
        nikitaResult = new NikitaResult(NikitaResult.StatusCode.OK, nikitaAttributes);
        engine = Mockito.mock(NikitaEngine.class);
        when(engine.evaluate(simpleEvent)).thenReturn(nikitaResult);
        evaluator = new NikitaEnrichmentEvaluator.Builder().nikitaEngine(engine).build();
        outputEevents = new ArrayList<>();

        command = new EnrichmentCommand();
        tags = new ArrayList<>();
        enrichments = new ArrayList<>();
        tags.add(Pair.of("a", "b"));
        tags.add(Pair.of("c", "d"));
        enrichments.add(Pair.of("e", "f"));
        enrichments.add(Pair.of("g", "h"));
        command.setKey("dummy_key");
        command.setTableName("dummy_table");
        command.setEnrichmentFields(enrichments);
        command.setTags(tags);

        outputEevents.add(new HashMap<>());
        outputEevents.get(0).put(EnrichmentFields.ENRICHMENT_COMMAND.toString(), command);
    }

    @Test
    public void testEvaluateNoMatch() {
        nikitaAttributes.setEvaluationResult(EvaluationResult.NO_MATCH);
        EnrichmentResult result = evaluator.evaluate(simpleEvent);
        Assert.assertEquals(OK, result.getStatusCode());
        Assert.assertNull(result.getAttributes().getEnrichmentCommands());
    }

    @Test
    public void testEvaluateError() {
        nikitaResult = new NikitaResult(NikitaResult.StatusCode.ERROR, nikitaAttributes);
        nikitaAttributes.setException("dummy");
        when(engine.evaluate(simpleEvent)).thenReturn(nikitaResult);
        EnrichmentResult result = evaluator.evaluate(simpleEvent);
        Assert.assertEquals(ERROR, result.getStatusCode());
        Assert.assertEquals("dummy", result.getAttributes().getMessage());
    }

    @Test
    public void testEvaluateMatch() {
        nikitaAttributes.setOutputEvents(outputEevents);
        nikitaAttributes.setEvaluationResult(EvaluationResult.MATCH);
        EnrichmentResult result = evaluator.evaluate(simpleEvent);
        Assert.assertEquals(OK, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes().getEnrichmentCommands());
        ArrayList<EnrichmentCommand> enrichmentCommands = result.getAttributes().getEnrichmentCommands();
        Assert.assertEquals(1, enrichmentCommands.size());
        Assert.assertEquals("dummy_key", enrichmentCommands.get(0).getKey());
        Assert.assertEquals("dummy_table", enrichmentCommands.get(0).getTableName());

        Assert.assertTrue(enrichmentCommands.get(0).getTags().contains(Pair.of("a", "b")));
        Assert.assertTrue(enrichmentCommands.get(0).getTags().contains(Pair.of("c", "d")));

        Assert.assertTrue(enrichmentCommands.get(0).getEnrichmentFields().contains(Pair.of("g", "h")));
        Assert.assertTrue(enrichmentCommands.get(0).getEnrichmentFields().contains(Pair.of("e", "f")));

        Assert.assertTrue(enrichmentCommands.get(0).getTableFields().contains("g"));
        Assert.assertTrue(enrichmentCommands.get(0).getTableFields().contains("e"));
    }

    @Test
    public void testEvaluateMatchMultipleCommands() {
        outputEevents.add(new HashMap<>());
        outputEevents.get(1).put(EnrichmentFields.ENRICHMENT_COMMAND.toString(), command);

        nikitaAttributes.setOutputEvents(outputEevents);
        nikitaAttributes.setEvaluationResult(EvaluationResult.MATCH);
        EnrichmentResult result = evaluator.evaluate(simpleEvent);
        Assert.assertEquals(OK, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes().getEnrichmentCommands());
        ArrayList<EnrichmentCommand> enrichmentCommands = result.getAttributes().getEnrichmentCommands();
        Assert.assertEquals(2, enrichmentCommands.size());
        Assert.assertEquals("dummy_key", enrichmentCommands.get(1).getKey());
        Assert.assertEquals("dummy_table", enrichmentCommands.get(1).getTableName());

        Assert.assertTrue(enrichmentCommands.get(1).getTags().contains(Pair.of("a", "b")));
        Assert.assertTrue(enrichmentCommands.get(1).getTags().contains(Pair.of("c", "d")));

        Assert.assertTrue(enrichmentCommands.get(1).getEnrichmentFields().contains(Pair.of("g", "h")));
        Assert.assertTrue(enrichmentCommands.get(1).getEnrichmentFields().contains(Pair.of("e", "f")));

        Assert.assertTrue(enrichmentCommands.get(1).getTableFields().contains("g"));
        Assert.assertTrue(enrichmentCommands.get(1).getTableFields().contains("e"));

        Assert.assertEquals("dummy_key", enrichmentCommands.get(1).getKey());
        Assert.assertEquals("dummy_table", enrichmentCommands.get(1).getTableName());

        Assert.assertTrue(enrichmentCommands.get(1).getTags().contains(Pair.of("a", "b")));
        Assert.assertTrue(enrichmentCommands.get(1).getTags().contains(Pair.of("c", "d")));

        Assert.assertTrue(enrichmentCommands.get(1).getEnrichmentFields().contains(Pair.of("g", "h")));
        Assert.assertTrue(enrichmentCommands.get(1).getEnrichmentFields().contains(Pair.of("e", "f")));

        Assert.assertTrue(enrichmentCommands.get(1).getTableFields().contains("g"));
        Assert.assertTrue(enrichmentCommands.get(1).getTableFields().contains("e"));
    }

    @Test
    public void testEvaluateMatchMissingCommand() {
        outputEevents.get(0).remove(EnrichmentFields.ENRICHMENT_COMMAND.toString());
        nikitaAttributes.setOutputEvents(outputEevents);
        nikitaAttributes.setEvaluationResult(EvaluationResult.MATCH);
        EnrichmentResult result = evaluator.evaluate(simpleEvent);
        Assert.assertEquals(ERROR, result.getStatusCode());
        Assert.assertTrue(result.getAttributes().getMessage().contains("Missing enrichment command"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void buildEmptyRules() {
        evaluator = new NikitaEnrichmentEvaluator.Builder().build();

    }
}
