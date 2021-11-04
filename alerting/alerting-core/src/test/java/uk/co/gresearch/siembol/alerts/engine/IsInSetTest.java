package uk.co.gresearch.siembol.alerts.engine;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import uk.co.gresearch.siembol.alerts.common.EvaluationResult;

import java.util.HashMap;
import java.util.Map;

public class IsInSetTest {
    private String field = "test_field";
    private Map<String, Object> event;
    private IsInSetMatcher matcher;

    private final String strings = """
            metron
            alerts
            response
            stoRm
            123""";

    private final String variableStrings = """
            ${variable_field1}
            aa ${variable.field2} ${variable:field1}
            http://${host}/${path}""";

    private final String mixedVariablesConstants = """
            alerts
            ${variable:field1}
            aa ${variable.field2} b
            Metron
            """;

    @Before
    public void setUp() {
        event = new HashMap<>();
    }

    @Test
    public void testGoodMatchConstants() {
        matcher = IsInSetMatcher.builder()
                .data(strings)
                .fieldName(field)
                .build();

        event.put(field, "metron");
        EvaluationResult rest = matcher.match(event);

        Assert.assertEquals(EvaluationResult.MATCH, rest);
        Assert.assertFalse(matcher.canModifyEvent());
    }

    @Test
    public void testGoodNoMatchCaseSensitive() {
        matcher = IsInSetMatcher.builder()
                .data(strings)
                .isCaseInsensitiveCompare(false)
                .fieldName(field)
                .build();

        event.put(field, "StorM");
        EvaluationResult rest = matcher.match(event);

        Assert.assertEquals(EvaluationResult.NO_MATCH, rest);
        Assert.assertFalse(matcher.canModifyEvent());
    }

    @Test
    public void testGoodMatchCaseInsensitive() {
        matcher = IsInSetMatcher.builder()
                .data(strings)
                .isCaseInsensitiveCompare(true)
                .fieldName(field)
                .build();

        event.put(field, "StorM");
        EvaluationResult rest = matcher.match(event);

        Assert.assertEquals(EvaluationResult.MATCH, rest);
        Assert.assertFalse(matcher.canModifyEvent());
    }

    @Test
    public void testGoodMatchVariables() {
        matcher = IsInSetMatcher.builder()
                .data(variableStrings)
                .fieldName(field)
                .build();

        event.put(field, "metron");
        event.put("variable_field1", "metron");
        EvaluationResult rest = matcher.match(event);

        Assert.assertEquals(EvaluationResult.MATCH, rest);
        Assert.assertFalse(matcher.canModifyEvent());
    }

    @Test
    public void testGoodMatchVariablesCaseInsensitive() {
        matcher = IsInSetMatcher.builder()
                .data(variableStrings)
                .isCaseInsensitiveCompare(true)
                .fieldName(field)
                .build();

        event.put(field, "meTrON");
        event.put("variable_field1", "metrOn");
        EvaluationResult rest = matcher.match(event);

        Assert.assertEquals(EvaluationResult.MATCH, rest);
        Assert.assertFalse(matcher.canModifyEvent());
    }

    @Test
    public void testGoodNoMatchVariablesCaseInsensitive() {
        matcher = IsInSetMatcher.builder()
                .data(variableStrings)
                .isCaseInsensitiveCompare(false)
                .fieldName(field)
                .build();

        event.put(field, "meTrON");
        event.put("variable_field1", "metrOn");
        EvaluationResult rest = matcher.match(event);

        Assert.assertEquals(EvaluationResult.NO_MATCH, rest);
        Assert.assertFalse(matcher.canModifyEvent());
    }

    @Test
    public void testGoodMatchVariables2() {
        matcher = IsInSetMatcher.builder()
                .data(variableStrings)
                .fieldName(field)
                .build();

        event.put(field, "aa alerts metron");
        event.put("variable:field1", "metron");
        event.put("variable.field2", "alerts");
        EvaluationResult rest = matcher.match(event);

        Assert.assertEquals(EvaluationResult.MATCH, rest);
        Assert.assertFalse(matcher.canModifyEvent());
    }

    @Test
    public void testGoodMatchMixed() {
        matcher = IsInSetMatcher.builder()
                .data(mixedVariablesConstants)
                .fieldName(field)
                .build();

        event.put(field, "Metron");
        EvaluationResult rest = matcher.match(event);

        Assert.assertEquals(EvaluationResult.MATCH, rest);
        Assert.assertFalse(matcher.canModifyEvent());
    }

    @Test
    public void testGoodMatchMixedVariable() {
        matcher = IsInSetMatcher.builder()
                .data(mixedVariablesConstants)
                .fieldName(field)
                .build();

        event.put(field, "aa metron b");
        event.put("variable.field2", "metron");
        EvaluationResult rest = matcher.match(event);

        Assert.assertEquals(EvaluationResult.MATCH, rest);
        Assert.assertFalse(matcher.canModifyEvent());
    }

    @Test
    public void testGoodInteger() {
        matcher = IsInSetMatcher.builder()
                .data(strings)
                .fieldName(field)
                .build();

        event.put(field, 123);
        EvaluationResult rest = matcher.match(event);

        Assert.assertEquals(EvaluationResult.MATCH, rest);
        Assert.assertFalse(matcher.canModifyEvent());
    }

    @Test
    public void testGoodNegated() {
        matcher = IsInSetMatcher.builder()
                .data(strings)
                .fieldName(field)
                .isNegated(true)
                .build();

        event.put(field, "metron");
        EvaluationResult rest = matcher.match(event);

        Assert.assertEquals(EvaluationResult.NO_MATCH, rest);
        Assert.assertFalse(matcher.canModifyEvent());
    }

    @Test
    public void testGoodNoMatch() {
        matcher = IsInSetMatcher.builder()
                .data(strings)
                .fieldName(field)
                .build();

        event.put(field, "spark");
        EvaluationResult rest = matcher.match(event);

        Assert.assertEquals(EvaluationResult.NO_MATCH, rest);
        Assert.assertFalse(matcher.canModifyEvent());
    }

    @Test
    public void testGoodNoMatchVariables() {
        matcher = IsInSetMatcher.builder()
                .data(strings)
                .fieldName(field)
                .build();

        event.put(field, "spark");
        EvaluationResult rest = matcher.match(event);

        Assert.assertEquals(EvaluationResult.NO_MATCH, rest);
        Assert.assertFalse(matcher.canModifyEvent());
    }

    @Test
    public void testGoodNoMatchMixed() {
        matcher = IsInSetMatcher.builder()
                .data(strings)
                .fieldName(field)
                .build();

        event.put(field, "spark");
        EvaluationResult rest = matcher.match(event);

        Assert.assertEquals(EvaluationResult.NO_MATCH, rest);
        Assert.assertFalse(matcher.canModifyEvent());
    }

    @Test(expected = IllegalArgumentException.class)
    public void missingData() {
        matcher = IsInSetMatcher.builder()
                .fieldName(field)
                .build();
    }

    @Test
    public void wrongVariableData() {
        matcher = IsInSetMatcher.builder()
                .data("a${b\na${b c}")
                .fieldName(field)
                .build();

        event.put(field, "a${b");
        EvaluationResult ret = matcher.match(event);
        Assert.assertEquals(EvaluationResult.MATCH, ret);

        event.put(field, "a${b c}");
        ret = matcher.match(event);
        Assert.assertEquals(EvaluationResult.MATCH, ret);
    }
}
