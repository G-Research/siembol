package uk.co.gresearch.siembol.alerts.engine;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import uk.co.gresearch.siembol.alerts.common.EvaluationResult;

import java.util.HashMap;
import java.util.Map;

public class ContainsMatcherTest {
    private String field = "test_field";
    private Map<String, Object> event;
    private ContainsMatcher matcher;
    private final String pattern = "secret";
    private final String patternWithVariable = "secret ${project}";

    @Before
    public void setUp() {
        event = new HashMap<>();
        event.put("project", "siembol");
    }

    @Test
    public void testGoodMatchPatternInsideString() {
        matcher = ContainsMatcher.builder()
                .data(pattern)
                .fieldName(field)
                .build();

        event.put(field, "aasecretbbb");
        EvaluationResult rest = matcher.match(event);

        Assert.assertEquals(EvaluationResult.MATCH, rest);
        Assert.assertFalse(matcher.canModifyEvent());
    }

    @Test
    public void testNoMatchNullField() {
        matcher = ContainsMatcher.builder()
                .data(pattern)
                .fieldName(field)
                .build();

        event.put(field, null);
        EvaluationResult rest = matcher.match(event);

        Assert.assertEquals(EvaluationResult.NO_MATCH, rest);
        Assert.assertFalse(matcher.canModifyEvent());
    }

    @Test
    public void testGoodMatchPatternInsideStringStartsWith() {
        matcher = ContainsMatcher.builder()
                .data(pattern)
                .isStartsWith(true)
                .fieldName(field)
                .build();

        event.put(field, "aasecretbbb");
        EvaluationResult rest = matcher.match(event);

        Assert.assertEquals(EvaluationResult.NO_MATCH, rest);
        Assert.assertFalse(matcher.canModifyEvent());
    }

    @Test
    public void testGoodMatchPatternInsideStringEndsWith() {
        matcher = ContainsMatcher.builder()
                .data(pattern)
                .isEndsWith(true)
                .fieldName(field)
                .build();

        event.put(field, "aasecretbbb");
        EvaluationResult rest = matcher.match(event);

        Assert.assertEquals(EvaluationResult.NO_MATCH, rest);
        Assert.assertFalse(matcher.canModifyEvent());
    }

    @Test
    public void testGoodMatchPatternInsideStartsStringEndsWith() {
        matcher = ContainsMatcher.builder()
                .data(pattern)
                .isEndsWith(true)
                .isStartsWith(true)
                .fieldName(field)
                .build();

        event.put(field, "aasecretbbb");
        EvaluationResult rest = matcher.match(event);

        Assert.assertEquals(EvaluationResult.NO_MATCH, rest);
        Assert.assertFalse(matcher.canModifyEvent());
    }

    @Test
    public void testGoodMatchPatternInsideStringCaseSensitive() {
        matcher = ContainsMatcher.builder()
                .data(pattern)
                .isCaseInsensitiveCompare(false)
                .fieldName(field)
                .build();

        event.put(field, "aaSecretbbb");
        EvaluationResult rest = matcher.match(event);

        Assert.assertEquals(EvaluationResult.NO_MATCH, rest);
        Assert.assertFalse(matcher.canModifyEvent());
    }

    @Test
    public void testGoodMatchPatternInsideStringCaseInSensitive() {
        matcher = ContainsMatcher.builder()
                .data(pattern)
                .isCaseInsensitiveCompare(true)
                .fieldName(field)
                .build();

        event.put(field, "aaSecretbbb");
        EvaluationResult rest = matcher.match(event);

        Assert.assertEquals(EvaluationResult.MATCH, rest);
        Assert.assertFalse(matcher.canModifyEvent());
    }

    @Test
    public void testGoodMatchPatternInsideStringWithVariable() {
        matcher = ContainsMatcher.builder()
                .data(patternWithVariable)
                .fieldName(field)
                .build();

        event.put(field, "aasecret siembolbbb");
        EvaluationResult rest = matcher.match(event);

        Assert.assertEquals(EvaluationResult.MATCH, rest);
        Assert.assertFalse(matcher.canModifyEvent());
    }

    @Test
    public void testNoMatchPatternWithVariableMissingSubstitutionVariable() {
        matcher = ContainsMatcher.builder()
                .data(patternWithVariable)
                .fieldName(field)
                .build();

        event.remove("project");
        event.put(field, "aasecret siembolbbb");
        EvaluationResult rest = matcher.match(event);

        Assert.assertEquals(EvaluationResult.NO_MATCH, rest);
        Assert.assertFalse(matcher.canModifyEvent());
    }

    @Test
    public void testGoodMatchPatternInsideStringStartsWithWithVariable() {
        matcher = ContainsMatcher.builder()
                .data(patternWithVariable)
                .isStartsWith(true)
                .fieldName(field)
                .build();

        event.put(field, "aasecret siembolbbb");
        EvaluationResult rest = matcher.match(event);

        Assert.assertEquals(EvaluationResult.NO_MATCH, rest);
        Assert.assertFalse(matcher.canModifyEvent());
    }

    @Test
    public void testGoodMatchPatternInsideStringEndsWithWithVariable() {
        matcher = ContainsMatcher.builder()
                .data(patternWithVariable)
                .isEndsWith(true)
                .fieldName(field)
                .build();

        event.put(field, "aasecret siembolbbb");
        EvaluationResult rest = matcher.match(event);

        Assert.assertEquals(EvaluationResult.NO_MATCH, rest);
        Assert.assertFalse(matcher.canModifyEvent());
    }

    @Test
    public void testGoodMatchPatternInsideStartsStringEndsWithWithVariable() {
        matcher = ContainsMatcher.builder()
                .data(patternWithVariable)
                .isEndsWith(true)
                .isStartsWith(true)
                .fieldName(field)
                .build();

        event.put(field, "aasecret siembolbbb");
        EvaluationResult rest = matcher.match(event);

        Assert.assertEquals(EvaluationResult.NO_MATCH, rest);
        Assert.assertFalse(matcher.canModifyEvent());
    }

    @Test
    public void testGoodMatchPatternInsideStringCaseSensitiveWithVariable() {
        matcher = ContainsMatcher.builder()
                .data(patternWithVariable)
                .isCaseInsensitiveCompare(false)
                .fieldName(field)
                .build();

        event.put("project", "Siembol");
        event.put(field, "aasecret Siembolbbb");
        EvaluationResult rest = matcher.match(event);

        Assert.assertEquals(EvaluationResult.MATCH, rest);
        Assert.assertFalse(matcher.canModifyEvent());
    }

    @Test
    public void testGoodNoMatchPatternInsideStringCaseSensitiveWithVariable() {
        matcher = ContainsMatcher.builder()
                .data(patternWithVariable)
                .isCaseInsensitiveCompare(false)
                .fieldName(field)
                .build();

        event.put(field, "aasecret Siembolbbb");
        EvaluationResult rest = matcher.match(event);

        Assert.assertEquals(EvaluationResult.NO_MATCH, rest);
        Assert.assertFalse(matcher.canModifyEvent());
    }

    @Test
    public void testGoodMatchPatternInsideStringCaseInSensitiveWithVariable() {
        matcher = ContainsMatcher.builder()
                .data(patternWithVariable)
                .isCaseInsensitiveCompare(true)
                .fieldName(field)
                .build();

        event.put(field, "aaSecret Siembolbbb");
        EvaluationResult rest = matcher.match(event);

        Assert.assertEquals(EvaluationResult.MATCH, rest);
        Assert.assertFalse(matcher.canModifyEvent());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMissingPattern() {
        ContainsMatcher.builder()
                .fieldName(field)
                .build();

    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullPattern() {
        ContainsMatcher.builder()
                .data(null)
                .fieldName(field)
                .build();
    }
}
