package uk.co.gresearch.siembol.alerts.engine;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import uk.co.gresearch.siembol.alerts.common.EvaluationResult;
import uk.co.gresearch.siembol.alerts.common.AlertingEngine;
import uk.co.gresearch.siembol.alerts.common.AlertingFields;
import uk.co.gresearch.siembol.alerts.common.AlertingResult;

import java.util.*;

import static org.mockito.Mockito.when;

public class AlertingEngineImplTest {
    private final String knownSourceType = """
            {   "source_type" : "test_source",
                "dummy_field" : "true"
            }
            """;

    private final String sourceType = "test_source";
    private List<Pair<String, String>> constants;
    private List<Pair<String, Object>> protections;
    private List<Pair<String, Rule>> rules;
    private Rule rule1;
    private Rule rule2;
    private AlertingEngine engine;
    private AlertingResult resultRule1;
    private AlertingResult resultRule2;

    @Before
    public void setUp() {
        constants = List.of(Pair.of("detection_source", "siembol_alerts"));
        protections = List.of(Pair.of(AlertingFields.MAX_PER_HOUR_FIELD.getAlertingName(), 1));
        rule1 = Mockito.mock(Rule.class);
        rule2 = Mockito.mock(Rule.class);
        resultRule1 = AlertingResult.fromEvaluationResult(EvaluationResult.MATCH, new HashMap<>());
        resultRule2 = AlertingResult.fromEvaluationResult(EvaluationResult.MATCH, new HashMap<>());

        when(rule1.getRuleName()).thenReturn("rule1");
        when(rule1.getFullRuleName()).thenReturn("rule1_v1");

        when(rule1.match(ArgumentMatchers.any())).thenReturn(resultRule1);
        when(rule2.getRuleName()).thenReturn("rule2");
        when(rule2.getFullRuleName()).thenReturn("rule2_v1");
        when(rule2.match(ArgumentMatchers.any())).thenReturn(resultRule2);

        rules = Arrays.asList(Pair.of(sourceType, rule1),
                Pair.of("*", rule2));

        engine = new AlertingEngineImpl.Builder()
                .constants(constants)
                .protections(protections)
                .rules(rules)
                .sourceField("source_type")
                .wildcardSource("*")
                .build();
    }

    @Test
    public void testMatchSourceAndAll() {
        AlertingResult ret = engine.evaluate(knownSourceType);
        Assert.assertEquals(AlertingResult.StatusCode.OK, ret.getStatusCode());
        Assert.assertEquals(EvaluationResult.MATCH, ret.getAttributes().getEvaluationResult());
        Assert.assertEquals(2, ret.getAttributes().getOutputEvents().size());
        Assert.assertEquals("rule1",
                ret.getAttributes().getOutputEvents().get(0).get("siembol_alerts_rule_name"));
        Assert.assertEquals("rule1_v1",
                ret.getAttributes().getOutputEvents().get(0).get("siembol_alerts_full_rule_name"));
        Assert.assertEquals("siembol_alerts",
                ret.getAttributes().getOutputEvents().get(0).get("detection_source"));
        Assert.assertEquals(1,
                ret.getAttributes().getOutputEvents().get(0).get("siembol_alerts_max_per_hour"));
        Assert.assertEquals("rule2",
                ret.getAttributes().getOutputEvents().get(1).get("siembol_alerts_rule_name"));
        Assert.assertEquals("rule2_v1",
                ret.getAttributes().getOutputEvents().get(1).get("siembol_alerts_full_rule_name"));
        Assert.assertEquals("siembol_alerts",
                ret.getAttributes().getOutputEvents().get(1).get("detection_source"));
        Assert.assertEquals(1,
                ret.getAttributes().getOutputEvents().get(1).get("siembol_alerts_max_per_hour"));
    }

    @Test
    public void testMatchSourceTwoRules() {
        rules = Arrays.asList(Pair.of(sourceType, rule1),
                Pair.of(sourceType, rule2));

        engine = new AlertingEngineImpl.Builder()
                .constants(constants)
                .protections(protections)
                .rules(rules)
                .sourceField("source_type")
                .wildcardSource("*")
                .build();

        AlertingResult ret = engine.evaluate(knownSourceType);
        Assert.assertEquals(AlertingResult.StatusCode.OK, ret.getStatusCode());
        Assert.assertEquals(EvaluationResult.MATCH, ret.getAttributes().getEvaluationResult());
        Assert.assertEquals(2, ret.getAttributes().getOutputEvents().size());
        Assert.assertEquals("rule1",
                ret.getAttributes().getOutputEvents().get(0).get("siembol_alerts_rule_name"));
        Assert.assertEquals("rule1_v1",
                ret.getAttributes().getOutputEvents().get(0).get("siembol_alerts_full_rule_name"));
        Assert.assertEquals("siembol_alerts",
                ret.getAttributes().getOutputEvents().get(0).get("detection_source"));
        Assert.assertEquals(1,
                ret.getAttributes().getOutputEvents().get(0).get("siembol_alerts_max_per_hour"));
        Assert.assertEquals("rule2",
                ret.getAttributes().getOutputEvents().get(1).get("siembol_alerts_rule_name"));
        Assert.assertEquals("rule2_v1",
                ret.getAttributes().getOutputEvents().get(1).get("siembol_alerts_full_rule_name"));
        Assert.assertEquals("siembol_alerts",
                ret.getAttributes().getOutputEvents().get(1).get("detection_source"));
        Assert.assertEquals(1,
                ret.getAttributes().getOutputEvents().get(1).get("siembol_alerts_max_per_hour"));
    }

    @Test
    public void testMatchWildcardOnly() {
        resultRule1.getAttributes().setEvaluationResult(EvaluationResult.NO_MATCH);
        AlertingResult ret = engine.evaluate(knownSourceType);
        Assert.assertEquals(AlertingResult.StatusCode.OK, ret.getStatusCode());
        Assert.assertEquals(EvaluationResult.MATCH, ret.getAttributes().getEvaluationResult());
        Assert.assertEquals(1, ret.getAttributes().getOutputEvents().size());
        Assert.assertEquals("rule2",
                ret.getAttributes().getOutputEvents().get(0).get("siembol_alerts_rule_name"));
        Assert.assertEquals("rule2_v1",
                ret.getAttributes().getOutputEvents().get(0).get("siembol_alerts_full_rule_name"));
        Assert.assertEquals("siembol_alerts",
                ret.getAttributes().getOutputEvents().get(0).get("detection_source"));
        Assert.assertEquals(1,
                ret.getAttributes().getOutputEvents().get(0).get("siembol_alerts_max_per_hour"));
    }

    @Test
    public void testMatchAndException() {
        when(rule1.match(ArgumentMatchers.<Map<String, Object>>any())).thenThrow(new RuntimeException());
        AlertingResult ret = engine.evaluate(knownSourceType);
        Assert.assertEquals(AlertingResult.StatusCode.OK, ret.getStatusCode());
        Assert.assertEquals(EvaluationResult.MATCH, ret.getAttributes().getEvaluationResult());
        Assert.assertEquals(1, ret.getAttributes().getOutputEvents().size());
        Assert.assertEquals("rule2",
                ret.getAttributes().getOutputEvents().get(0).get("siembol_alerts_rule_name"));
        Assert.assertEquals("rule2_v1",
                ret.getAttributes().getOutputEvents().get(0).get("siembol_alerts_full_rule_name"));
        Assert.assertEquals("siembol_alerts",
                ret.getAttributes().getOutputEvents().get(0).get("detection_source"));
        Assert.assertEquals(1,
                ret.getAttributes().getOutputEvents().get(0).get("siembol_alerts_max_per_hour"));

        Assert.assertEquals(1, ret.getAttributes().getExceptionEvents().size());
        Assert.assertEquals("rule1",
                ret.getAttributes().getExceptionEvents().get(0).get("siembol_alerts_rule_name"));
        Assert.assertEquals("rule1_v1",
                ret.getAttributes().getExceptionEvents().get(0).get("siembol_alerts_full_rule_name"));
        Assert.assertTrue(ret.getAttributes().getExceptionEvents().get(0)
                .get(AlertingFields.EXCEPTION.getAlertingName()).toString().contains("java.lang.RuntimeException"));
    }

    @Test
    public void testWrongJsonEvent() {
        AlertingResult ret = engine.evaluate("INVALID JSON");
        Assert.assertEquals(AlertingResult.StatusCode.ERROR, ret.getStatusCode());
        Assert.assertTrue(ret.getAttributes().getException().contains("json"));
    }

    @Test
    public void testNoSourceType() {
        AlertingResult ret = engine.evaluate("{\"a\": \"b\"}");
        Assert.assertEquals(AlertingResult.StatusCode.OK, ret.getStatusCode());
        Assert.assertEquals(EvaluationResult.NO_MATCH, ret.getAttributes().getEvaluationResult());
    }

    @Test(expected = IllegalArgumentException.class)
    public void missingRules() {
        engine = new AlertingEngineImpl.Builder()
                .constants(constants)
                .protections(protections)
                .sourceField("source_type")
                .wildcardSource("*")
                .build();
    }
}
