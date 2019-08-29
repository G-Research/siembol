package uk.co.gresearch.nortem.nikita.engine;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import uk.co.gresearch.nortem.nikita.common.EvaluationResult;
import uk.co.gresearch.nortem.nikita.common.NikitaFields;
import uk.co.gresearch.nortem.nikita.common.NikitaResult;

import java.util.*;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

public class RuleTest {
    private String name = "test_rule";
    private Integer version = 1;
    private String field = "test_field";
    private String sourceType = "test_source";
    private Map<String, Object> event;
    private List<Pair<String, String>> constants;
    private List<Pair<String, Object>> protections;
    private RuleMatcher matcher;
    private Rule rule;

    @Before
    public void setUp() {
        constants = Arrays.asList(Pair.of("detection_source", "nikita"));
        protections = Arrays.asList(Pair.of(NikitaFields.MAX_PER_HOUR_FIELD.toString(), Integer.valueOf(1)));
        matcher = Mockito.mock(RuleMatcher.class);
        when(matcher.match(any())).thenReturn(EvaluationResult.MATCH);
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
        Assert.assertEquals("nikita", event.get("detection_source"));


        Assert.assertEquals(Integer.valueOf(1), event.get(NikitaFields.MAX_PER_HOUR_FIELD.toString()));
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
        event.put("dummy_host", "nikita.com");
        event.put("dummy_path", "about");
        rule.addOutputFieldsToEvent(event);

        Assert.assertEquals(name, ruleName);
        Assert.assertEquals(name + "_v1", fullRuleName);
        Assert.assertEquals(5, event.size());
        Assert.assertEquals("nikita", event.get("detection_source"));
        Assert.assertEquals("http://nikita.com/about", event.get("malicious_url"));


        Assert.assertEquals(Integer.valueOf(1), event.get(NikitaFields.MAX_PER_HOUR_FIELD.toString()));
        Assert.assertFalse(rule.canModifyEvent());
    }

    @Test
    public void testGoodCanModifyEvent() {
        when(matcher.CanModifyEvent()).thenReturn(true);

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

        NikitaResult ret = rule.match(event);
        Assert.assertEquals(NikitaResult.StatusCode.OK, ret.getStatusCode());
        Assert.assertEquals(EvaluationResult.MATCH, ret.getAttributes().getEvaluationResult());
    }

    @Test
    public void testGoodNoMatch() {
        when(matcher.match(any())).thenReturn(EvaluationResult.NO_MATCH);
        rule = Rule.builder()
                .matchers(Arrays.asList(matcher))
                .name(name)
                .version(version)
                .tags(constants)
                .protections(protections)
                .build();

        NikitaResult ret = rule.match(event);
        Assert.assertEquals(NikitaResult.StatusCode.OK, ret.getStatusCode());
        Assert.assertEquals(EvaluationResult.NO_MATCH, ret.getAttributes().getEvaluationResult());
    }

    @Test(expected = RuntimeException.class)
    public void testThrowsException() throws RuntimeException {
        when(matcher.match(any())).thenThrow(new RuntimeException());
        rule = Rule.builder()
                .matchers(Arrays.asList(matcher))
                .name(name)
                .version(version)
                .tags(constants)
                .protections(protections)
                .build();

        NikitaResult ret = rule.match(event);
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
        rule = Rule.builder()
                .matchers(Arrays.asList(matcher))
                .name(name)
                .tags(constants)
                .protections(protections)
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void missingMatchers()  {
        rule = Rule.builder()
                .name(name)
                .version(version)
                .tags(constants)
                .protections(protections)
                .build();
    }

}
