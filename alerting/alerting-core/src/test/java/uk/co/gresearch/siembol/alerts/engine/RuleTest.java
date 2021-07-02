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
    private String name = "test_rule";
    private Integer version = 1;
    private Map<String, Object> event = new HashMap<>();
    private List<Pair<String, String>> constants;
    private List<Pair<String, Object>> protections;
    private BasicMatcher matcher;
    private Rule rule;

    @Before
    public void setUp() {
        constants = Arrays.asList(Pair.of("detection_source", "alerts"));
        protections = Arrays.asList(Pair.of(AlertingFields.MAX_PER_HOUR_FIELD.toString(), Integer.valueOf(1)));
        matcher = Mockito.mock(BasicMatcher.class);
        when(matcher.match(ArgumentMatchers.<Map<String, Object>>any())).thenReturn(EvaluationResult.MATCH);
    }

    @Test
    public void testGoodMetadata() {
        rule = Rule.builder()
                .matchers(Arrays.asList(matcher))
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


        Assert.assertEquals(Integer.valueOf(1), event.get(AlertingFields.MAX_PER_HOUR_FIELD.toString()));
        Assert.assertFalse(rule.canModifyEvent());
    }

    @Test
    public void testGoodMetadataVariableTag() {
        constants = new ArrayList<>(constants);
        constants.add(Pair.of("malicious_url", "http://${dummy_host}/${dummy_path}"));
        rule = Rule.builder()
                .matchers(Arrays.asList(matcher))
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


        Assert.assertEquals(Integer.valueOf(1), event.get(AlertingFields.MAX_PER_HOUR_FIELD.toString()));
        Assert.assertFalse(rule.canModifyEvent());
    }

    @Test
    public void testGoodCanModifyEvent() {
        when(matcher.canModifyEvent()).thenReturn(true);

        rule = Rule.builder()
                .matchers(Arrays.asList(matcher))
                .name(name)
                .version(version)
                .tags(constants)
                .protections(protections)
                .build();

        Assert.assertTrue(rule.canModifyEvent());
    }

    @Test
    public void testGoodMatch() {
        rule = Rule.builder()
                .matchers(Arrays.asList(matcher))
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
    public void testGoodNoMatch() {
        when(matcher.match(ArgumentMatchers.<Map<String, Object>>any())).thenReturn(EvaluationResult.NO_MATCH);
        rule = Rule.builder()
                .matchers(Arrays.asList(matcher))
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
    public void testThrowsException() throws RuntimeException {
        when(matcher.match(ArgumentMatchers.<Map<String, Object>>any())).thenThrow(new RuntimeException());
        rule = Rule.builder()
                .matchers(Arrays.asList(matcher))
                .name(name)
                .version(version)
                .tags(constants)
                .protections(protections)
                .build();

        rule.match(event);
    }

    @Test(expected = IllegalArgumentException.class)
    public void missingName()  {
        rule = Rule.builder()
                .matchers(Arrays.asList(matcher))
                .version(version)
                .tags(constants)
                .protections(protections)
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void missingVersion()  {
        Rule.builder()
                .matchers(Arrays.asList(matcher))
                .name(name)
                .tags(constants)
                .protections(protections)
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void missingMatchers()  {
        Rule.builder()
                .name(name)
                .version(version)
                .tags(constants)
                .protections(protections)
                .build();
    }
}
