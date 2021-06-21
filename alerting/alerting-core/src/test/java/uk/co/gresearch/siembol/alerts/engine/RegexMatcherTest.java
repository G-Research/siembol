package uk.co.gresearch.siembol.alerts.engine;

import org.adrianwalker.multilinestring.Multiline;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import uk.co.gresearch.siembol.alerts.common.EvaluationResult;

import java.util.Map;
import java.util.HashMap;

public class RegexMatcherTest {
    private String field = "test_field";
    Map<String, Object> event;
    RegexMatcher matcher;

    /**
     * Threat Level=(?<vof_threat_level>\d) Category=(?<vof_threat_cat>\S+) Type=(?<vof_threat_type>.*?)
     **/
    @Multiline
    public static String goodVofDetail;

    /**
     * Threat Level=1 Category=UNKNOWN Type=a
     *bc
     **/
    @Multiline
    public static String vofDetailInstance;

    @Before
    public void setUp() {
        event = new HashMap<>();
    }

    @Test
    public void testGoodWithVariables() {
        matcher = RegexMatcher.builder()
                .pattern(goodVofDetail)
                .fieldName(field)
                .build();

        event.put(field, vofDetailInstance);
        EvaluationResult rest = matcher.match(event);

        Assert.assertEquals(EvaluationResult.MATCH, rest);
        Assert.assertTrue(matcher.canModifyEvent());
        Assert.assertEquals(4, event.size());
        Assert.assertEquals("1", event.get("vof_threat_level"));
        Assert.assertEquals("UNKNOWN", event.get("vof_threat_cat"));
        Assert.assertEquals("a\nbc", event.get("vof_threat_type"));
    }

    @Test
    public void testGoodWithVariablesNoMatch() {
        matcher = RegexMatcher.builder()
                .pattern(goodVofDetail)
                .fieldName(field)
                .build();

        event.put(field, "Threat Level=1 WRONG");
        EvaluationResult rest = matcher.match(event);

        Assert.assertEquals(EvaluationResult.NO_MATCH, rest);
        Assert.assertTrue(matcher.canModifyEvent());
    }

    @Test
    public void testGoodWithVariablesNoField() {
        matcher = RegexMatcher.builder()
                .pattern(goodVofDetail)
                .fieldName(field)
                .build();

        EvaluationResult rest = matcher.match(event);
        Assert.assertEquals(EvaluationResult.NO_MATCH, rest);
        Assert.assertEquals(matcher.canModifyEvent(), true);
    }

    @Test
    public void testGoodWithVariablesNegated() {
        matcher = RegexMatcher.builder()
                .pattern(goodVofDetail)
                .fieldName(field)
                .isNegated(true)
                .build();

        event.put(field, vofDetailInstance);
        EvaluationResult rest = matcher.match(event);

        Assert.assertEquals(EvaluationResult.NO_MATCH, rest);
        Assert.assertTrue(matcher.canModifyEvent());
    }

    @Test
    public void testGoodNoVariables() {
        matcher = RegexMatcher.builder()
                .pattern("test")
                .fieldName(field)
                .build();

        event.put(field, "test");
        EvaluationResult rest = matcher.match(event);

        Assert.assertEquals(EvaluationResult.MATCH, rest);
        Assert.assertFalse(matcher.canModifyEvent());
    }

    @Test
    public void testGoodNoVariablesNoMatch() {
        matcher = RegexMatcher.builder()
                .pattern("test")
                .fieldName(field)
                .build();

        event.put(field, "WRONG");
        EvaluationResult rest = matcher.match(event);

        Assert.assertEquals(EvaluationResult.NO_MATCH, rest);
        Assert.assertFalse(matcher.canModifyEvent());
    }

    @Test
    public void testGoodNoVariablesNoMatchNegated() {
        matcher = RegexMatcher.builder()
                .pattern("test")
                .fieldName(field)
                .isNegated(true)
                .build();

        event.put(field, "WRONG");
        EvaluationResult rest = matcher.match(event);

        Assert.assertEquals(EvaluationResult.MATCH, rest);
        Assert.assertFalse(matcher.canModifyEvent());
    }

    @Test
    public void testMissingFieldNoMatchNegated() {
        matcher = RegexMatcher.builder()
                .pattern("test")
                .fieldName(field)
                .isNegated(true)
                .build();

        event.put(field, "WRONG");
        EvaluationResult rest = matcher.match(event);

        Assert.assertEquals(EvaluationResult.MATCH, rest);
        Assert.assertFalse(matcher.canModifyEvent());
    }

    @Test
    public void testGoodNoVariablesNegated() {
        matcher = RegexMatcher.builder()
                .pattern("test")
                .fieldName(field)
                .isNegated(true)
                .build();

        EvaluationResult rest = matcher.match(event);
        Assert.assertEquals(EvaluationResult.MATCH, rest);
    }

    @Test(expected = IllegalArgumentException.class)
    public void missingArguments() {
        matcher = RegexMatcher.builder()
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void wrongPattern() {
        matcher = RegexMatcher.builder()
                .pattern("[INVALID regex")
                .fieldName(field)
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void wrongVariableNamesInPattern() {
        matcher = RegexMatcher.builder()
                .pattern("(?<a>\\d)(?<a>\\d)")
                .fieldName(field)
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void missingFieldName() {
        matcher = RegexMatcher.builder()
                .pattern("valid")
                .build();
    }

}
