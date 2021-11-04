package uk.co.gresearch.siembol.enrichments.evaluation;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import uk.co.gresearch.siembol.enrichments.common.EnrichmentCommand;

import uk.co.gresearch.siembol.enrichments.common.EnrichmentResult;
import uk.co.gresearch.siembol.alerts.common.EvaluationResult;
import uk.co.gresearch.siembol.alerts.common.AlertingAttributes;
import uk.co.gresearch.siembol.alerts.common.AlertingEngine;
import uk.co.gresearch.siembol.alerts.common.AlertingResult;
import uk.co.gresearch.siembol.alerts.common.AlertingResult.StatusCode;

import java.util.*;

import static org.mockito.Mockito.when;
import static uk.co.gresearch.siembol.enrichments.common.EnrichmentResult.StatusCode.ERROR;
import static uk.co.gresearch.siembol.enrichments.common.EnrichmentResult.StatusCode.OK;

public class AlertingEnrichmentEvaluatorTest {
    private final String simpleEvent = """
            {
              "timestamp" : 1,
              "dummy_bool" : true,
              "dummy_str" : "test",
              "a" : "conflict"
            }
            """;

    private AlertingAttributes alertingAttributes;
    private AlertingResult alertingResult;
    private AlertingEngine engine;
    private AlertingEnrichmentEvaluator evaluator;
    private List<Map<String, Object>> outputEevents;
    private EnrichmentCommand command;
    private ArrayList<Pair<String, String>> enrichments;
    private ArrayList<Pair<String, String>> tags;

    @Before
    public void setUp() {
        alertingAttributes = new AlertingAttributes();
        alertingResult = new AlertingResult(StatusCode.OK, alertingAttributes);
        engine = Mockito.mock(AlertingEngine.class);
        when(engine.evaluate(simpleEvent)).thenReturn(alertingResult);
        evaluator = new AlertingEnrichmentEvaluator.Builder().AlertingEngine(engine).build();
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
        alertingAttributes.setEvaluationResult(EvaluationResult.NO_MATCH);
        EnrichmentResult result = evaluator.evaluate(simpleEvent);
        Assert.assertEquals(OK, result.getStatusCode());
        Assert.assertNull(result.getAttributes().getEnrichmentCommands());
    }

    @Test
    public void testEvaluateError() {
        alertingResult = new AlertingResult(StatusCode.ERROR, alertingAttributes);
        alertingAttributes.setException("dummy");
        when(engine.evaluate(simpleEvent)).thenReturn(alertingResult);
        EnrichmentResult result = evaluator.evaluate(simpleEvent);
        Assert.assertEquals(ERROR, result.getStatusCode());
        Assert.assertEquals("dummy", result.getAttributes().getMessage());
    }

    @Test
    public void testEvaluateMatch() {
        alertingAttributes.setOutputEvents(outputEevents);
        alertingAttributes.setEvaluationResult(EvaluationResult.MATCH);
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

        alertingAttributes.setOutputEvents(outputEevents);
        alertingAttributes.setEvaluationResult(EvaluationResult.MATCH);
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
        alertingAttributes.setOutputEvents(outputEevents);
        alertingAttributes.setEvaluationResult(EvaluationResult.MATCH);
        EnrichmentResult result = evaluator.evaluate(simpleEvent);
        Assert.assertEquals(ERROR, result.getStatusCode());
        Assert.assertTrue(result.getAttributes().getMessage().contains("Missing enrichment command"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void buildEmptyRules() {
        evaluator = new AlertingEnrichmentEvaluator.Builder().build();

    }
}
