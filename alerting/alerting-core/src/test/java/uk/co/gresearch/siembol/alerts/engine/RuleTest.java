package uk.co.gresearch.siembol.alerts.engine;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import uk.co.gresearch.siembol.alerts.common.EvaluationResult;
import uk.co.gresearch.siembol.alerts.common.AlertingFields;
import uk.co.gresearch.siembol.alerts.common.AlertingResult;

import java.util.*;

import static org.mockito.Mockito.when;

public class RuleTest {
    private final String name = "test_rule";
    private final Integer version = 1;
    private final Map<String, Object> event = new HashMap<>();
    private List<Pair<String, String>> constants;
    private List<Pair<String, Object>> protections;
    private Matcher matcher;
    private Rule rule;

    @Before
    public void setUp() {
        constants = List.of(Pair.of("detection_source", "alerts"));
        protections = List.of(Pair.of(AlertingFields.MAX_PER_HOUR_FIELD.toString(), 1));
        matcher = Mockito.mock(Matcher.class);
        when(matcher.match(ArgumentMatchers.any())).thenReturn(EvaluationResult.MATCH);
    }

    @Test
    public void ruleWithMetadataOk() {
        rule = Rule.builder()
                .matchers(List.of(matcher))
                .name(name)
                .version(version)
                .tags(constants)
                .protections(protections)
                .build();

        String ruleName = rule.getRuleName();
        String fullRuleName = rule.getFullRuleName();
        Map<String, Object> event = new HashMap<>();
        rule.addOutputFieldsToEvent(event);

        Assert.assertEquals(name, ruleName);
        Assert.assertEquals(name + "_v1", fullRuleName);
        Assert.assertEquals(2, event.size());
        Assert.assertEquals("alerts", event.get("detection_source"));


        Assert.assertEquals(1, event.get(AlertingFields.MAX_PER_HOUR_FIELD.toString()));
        Assert.assertFalse(rule.canModifyEvent());
    }

    @Test
    public void ruleWithMetadataVariableTagOk() {
        constants = new ArrayList<>(constants);
        constants.add(Pair.of("malicious_url", "http://${dummy_host}/${dummy_path}"));
        rule = Rule.builder()
                .matchers(List.of(matcher))
                .name(name)
                .version(version)
                .tags(constants)
                .protections(protections)
                .build();

        String ruleName = rule.getRuleName();
        String fullRuleName = rule.getFullRuleName();
        Map<String, Object> event = new HashMap<>();
        event.put("dummy_host", "alerts.com");
        event.put("dummy_path", "about");
        rule.addOutputFieldsToEvent(event);

        Assert.assertEquals(name, ruleName);
        Assert.assertEquals(name + "_v1", fullRuleName);
        Assert.assertEquals(5, event.size());
        Assert.assertEquals("alerts", event.get("detection_source"));
        Assert.assertEquals("http://alerts.com/about", event.get("malicious_url"));


        Assert.assertEquals(1, event.get(AlertingFields.MAX_PER_HOUR_FIELD.toString()));
        Assert.assertFalse(rule.canModifyEvent());
    }

    @Test
    public void ruleCanModifyEventOk() {
        when(matcher.canModifyEvent()).thenReturn(true);

        rule = Rule.builder()
                .matchers(List.of(matcher))
                .name(name)
                .version(version)
                .tags(constants)
                .protections(protections)
                .build();

        Assert.assertTrue(rule.canModifyEvent());
    }

    @Test
    public void ruleMatchOk() {
        rule = Rule.builder()
                .matchers(List.of(matcher))
                .name(name)
                .version(version)
                .tags(constants)
                .protections(protections)
                .build();

        AlertingResult ret = rule.match(event);
        Assert.assertEquals(AlertingResult.StatusCode.OK, ret.getStatusCode());
        Assert.assertEquals(EvaluationResult.MATCH, ret.getAttributes().getEvaluationResult());
    }

    @Test
    public void ruleNoMatch() {
        when(matcher.match(ArgumentMatchers.any())).thenReturn(EvaluationResult.NO_MATCH);
        rule = Rule.builder()
                .matchers(List.of(matcher))
                .name(name)
                .version(version)
                .tags(constants)
                .protections(protections)
                .build();

        AlertingResult ret = rule.match(event);
        Assert.assertEquals(AlertingResult.StatusCode.OK, ret.getStatusCode());
        Assert.assertEquals(EvaluationResult.NO_MATCH, ret.getAttributes().getEvaluationResult());
    }

    @Test(expected = RuntimeException.class)
    public void matchThrowsException() throws RuntimeException {
        when(matcher.match(ArgumentMatchers.any())).thenThrow(new RuntimeException());
        rule = Rule.builder()
                .matchers(List.of(matcher))
                .name(name)
                .version(version)
                .tags(constants)
                .protections(protections)
                .build();

        rule.match(event);
    }

    @Test(expected = IllegalArgumentException.class)
    public void builderMissingName()  {
        rule = Rule.builder()
                .matchers(List.of(matcher))
                .version(version)
                .tags(constants)
                .protections(protections)
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void builderMissingVersion()  {
        Rule.builder()
                .matchers(List.of(matcher))
                .name(name)
                .tags(constants)
                .protections(protections)
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void builderMissingMatchers()  {
        Rule.builder()
                .name(name)
                .version(version)
                .tags(constants)
                .protections(protections)
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void builderOneNegatedMatcher()  {
        when(matcher.isNegated()).thenReturn(true);
        rule = Rule.builder()
                .matchers(List.of(matcher))
                .name(name)
                .version(version)
                .tags(constants)
                .protections(protections)
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void builderTwoNegatedMatchers()  {
        when(matcher.isNegated()).thenReturn(true);
        rule = Rule.builder()
                .matchers(List.of(matcher, matcher))
                .name(name)
                .version(version)
                .tags(constants)
                .protections(protections)
                .build();
    }

    @Test
    public void builderOneMatcherAndMultipleNegatedMatchers()  {
        var nonNegatedMatcher = Mockito.mock(Matcher.class);
        when(nonNegatedMatcher.isNegated()).thenReturn(false);
        when(matcher.isNegated()).thenReturn(true);
        rule = Rule.builder()
                .matchers(List.of(matcher, matcher, nonNegatedMatcher, matcher, matcher))
                .name(name)
                .version(version)
                .tags(constants)
                .protections(protections)
                .build();
        Assert.assertNotNull(rule);
    }
}
