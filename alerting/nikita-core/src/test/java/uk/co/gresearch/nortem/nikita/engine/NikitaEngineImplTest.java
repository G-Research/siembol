package uk.co.gresearch.nortem.nikita.engine;

import org.adrianwalker.multilinestring.Multiline;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import uk.co.gresearch.nortem.nikita.common.EvaluationResult;
import uk.co.gresearch.nortem.nikita.common.NikitaEngine;
import uk.co.gresearch.nortem.nikita.common.NikitaFields;
import uk.co.gresearch.nortem.nikita.common.NikitaResult;

import java.util.*;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

public class NikitaEngineImplTest {
    /**
     *{"source:type" : "test_source", "dummy_field" : "true"}
     */
    @Multiline
    public static String knownSourceType;

    private String field = "test_field";
    private String sourceType = "test_source";
    private Map<String, Object> event;
    private List<Pair<String, String>> constants;
    private List<Pair<String, Object>> protections;
    private List<Pair<String, Rule>> rules;
    private Rule rule1;
    private Rule rule2;
    private NikitaEngine engine;
    private NikitaResult resultRule1;
    private NikitaResult resultRule2;

    @Before
    public void setUp() {
        constants = Arrays.asList(Pair.of("detection_source", "nikita"));
        protections = Arrays.asList(Pair.of(NikitaFields.MAX_PER_HOUR_FIELD.getNikitaName(), Integer.valueOf(1)));
        rule1 = Mockito.mock(Rule.class);
        rule2 = Mockito.mock(Rule.class);
        resultRule1 = NikitaResult.fromEvaluationResult(EvaluationResult.MATCH, new HashMap<>());
        resultRule2 = NikitaResult.fromEvaluationResult(EvaluationResult.MATCH, new HashMap<>());

        when(rule1.getRuleName()).thenReturn("rule1");
        when(rule1.getFullRuleName()).thenReturn("rule1_v1");

        when(rule1.match(any())).thenReturn(resultRule1);
        when(rule2.getRuleName()).thenReturn("rule2");
        when(rule2.getFullRuleName()).thenReturn("rule2_v1");
        when(rule2.match(any())).thenReturn(resultRule2);

        rules = Arrays.asList(Pair.of(sourceType, rule1),
                Pair.of("*", rule2));

        engine = new NikitaEngineImpl.Builder()
                .constants(constants)
                .protections(protections)
                .rules(rules)
                .sourceField("source:type")
                .wildcardSource("*")
                .build();
    }

    @Test
    public void testMatchSourceAndAll() {
        NikitaResult ret = engine.evaluate(knownSourceType);
        Assert.assertEquals(NikitaResult.StatusCode.OK, ret.getStatusCode());
        Assert.assertEquals(EvaluationResult.MATCH, ret.getAttributes().getEvaluationResult());
        Assert.assertEquals(2, ret.getAttributes().getOutputEvents().size());
        Assert.assertEquals("rule1", ret.getAttributes().getOutputEvents().get(0).get("nikita:rule_name"));
        Assert.assertEquals("rule1_v1",
                ret.getAttributes().getOutputEvents().get(0).get("nikita:full_rule_name"));
        Assert.assertEquals("nikita", ret.getAttributes().getOutputEvents().get(0).get("detection_source"));
        Assert.assertEquals(1, ret.getAttributes().getOutputEvents().get(0).get("nikita:max_per_hour"));
        Assert.assertEquals("rule2", ret.getAttributes().getOutputEvents().get(1).get("nikita:rule_name"));
        Assert.assertEquals("rule2_v1",
                ret.getAttributes().getOutputEvents().get(1).get("nikita:full_rule_name"));
        Assert.assertEquals("nikita", ret.getAttributes().getOutputEvents().get(1).get("detection_source"));
        Assert.assertEquals(1, ret.getAttributes().getOutputEvents().get(1).get("nikita:max_per_hour"));
    }

    @Test
    public void testMatchSourceTwoRules() {
        rules = Arrays.asList(Pair.of(sourceType, rule1),
                Pair.of(sourceType, rule2));

        engine = new NikitaEngineImpl.Builder()
                .constants(constants)
                .protections(protections)
                .rules(rules)
                .sourceField("source:type")
                .wildcardSource("*")
                .build();

        NikitaResult ret = engine.evaluate(knownSourceType);
        Assert.assertEquals(NikitaResult.StatusCode.OK, ret.getStatusCode());
        Assert.assertEquals(EvaluationResult.MATCH, ret.getAttributes().getEvaluationResult());
        Assert.assertEquals(2, ret.getAttributes().getOutputEvents().size());
        Assert.assertEquals("rule1", ret.getAttributes().getOutputEvents().get(0).get("nikita:rule_name"));
        Assert.assertEquals("rule1_v1",
                ret.getAttributes().getOutputEvents().get(0).get("nikita:full_rule_name"));
        Assert.assertEquals("nikita", ret.getAttributes().getOutputEvents().get(0).get("detection_source"));
        Assert.assertEquals(1, ret.getAttributes().getOutputEvents().get(0).get("nikita:max_per_hour"));
        Assert.assertEquals("rule2", ret.getAttributes().getOutputEvents().get(1).get("nikita:rule_name"));
        Assert.assertEquals("rule2_v1",
                ret.getAttributes().getOutputEvents().get(1).get("nikita:full_rule_name"));
        Assert.assertEquals("nikita", ret.getAttributes().getOutputEvents().get(1).get("detection_source"));
        Assert.assertEquals(1, ret.getAttributes().getOutputEvents().get(1).get("nikita:max_per_hour"));
    }

    @Test
    public void testMatchWildcardOnly() {
        resultRule1.getAttributes().setEvaluationResult(EvaluationResult.NO_MATCH);
        NikitaResult ret = engine.evaluate(knownSourceType);
        Assert.assertEquals(NikitaResult.StatusCode.OK, ret.getStatusCode());
        Assert.assertEquals(EvaluationResult.MATCH, ret.getAttributes().getEvaluationResult());
        Assert.assertEquals(ret.getAttributes().getOutputEvents().size(), 1);
        Assert.assertEquals(ret.getAttributes().getOutputEvents().get(0).get("nikita:rule_name"),
                "rule2");
        Assert.assertEquals(ret.getAttributes().getOutputEvents().get(0).get("nikita:full_rule_name"),
                "rule2_v1");
        Assert.assertEquals(ret.getAttributes().getOutputEvents().get(0).get("detection_source"),
                "nikita");
        Assert.assertEquals(ret.getAttributes().getOutputEvents().get(0).get("nikita:max_per_hour"),
                1);
    }

    @Test
    public void testMatchAndException() {
        when(rule1.match(any())).thenThrow(new RuntimeException());
        NikitaResult ret = engine.evaluate(knownSourceType);
        Assert.assertEquals(NikitaResult.StatusCode.OK, ret.getStatusCode());
        Assert.assertEquals(EvaluationResult.MATCH, ret.getAttributes().getEvaluationResult());
        Assert.assertEquals(1, ret.getAttributes().getOutputEvents().size());
        Assert.assertEquals("rule2", ret.getAttributes().getOutputEvents().get(0).get("nikita:rule_name"));
        Assert.assertEquals("rule2_v1",
                ret.getAttributes().getOutputEvents().get(0).get("nikita:full_rule_name"));
        Assert.assertEquals("nikita", ret.getAttributes().getOutputEvents().get(0).get("detection_source"));
        Assert.assertEquals(1, ret.getAttributes().getOutputEvents().get(0).get("nikita:max_per_hour"));

        Assert.assertEquals(1, ret.getAttributes().getExceptionEvents().size());
        Assert.assertEquals("rule1", ret.getAttributes().getExceptionEvents().get(0).get("nikita:rule_name"));
        Assert.assertEquals("rule1_v1",
                ret.getAttributes().getExceptionEvents().get(0).get("nikita:full_rule_name"));
        Assert.assertTrue(ret.getAttributes().getExceptionEvents().get(0).get(NikitaFields.EXCEPTION.getNikitaName())
                        .toString().contains("java.lang.RuntimeException"));
    }

    @Test
    public void testWrongJsonEvent() {
        NikitaResult ret = engine.evaluate("INVALID JSON");
        Assert.assertEquals(NikitaResult.StatusCode.ERROR, ret.getStatusCode());
        Assert.assertTrue(ret.getAttributes().getException().contains("json"));
    }

    @Test
    public void testNoSourceType() {
        NikitaResult ret = engine.evaluate("{\"a\": \"b\"}");
        Assert.assertEquals(NikitaResult.StatusCode.OK, ret.getStatusCode());
        Assert.assertEquals(EvaluationResult.NO_MATCH, ret.getAttributes().getEvaluationResult());
    }

    @Test(expected = IllegalArgumentException.class)
    public void missingRules()  {
        engine = new NikitaEngineImpl.Builder()
                .constants(constants)
                .protections(protections)
                .sourceField("source:type")
                .wildcardSource("*")
                .build();
    }
}
