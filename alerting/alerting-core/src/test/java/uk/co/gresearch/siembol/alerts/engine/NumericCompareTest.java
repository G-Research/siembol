package uk.co.gresearch.siembol.alerts.engine;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import uk.co.gresearch.siembol.alerts.common.EvaluationResult;
import uk.co.gresearch.siembol.alerts.model.NumericCompareTypeDto;

import java.util.HashMap;
import java.util.Map;

public class NumericCompareTest {
    private final String field = "test_field";
    Map<String, Object> event;
    NumericCompareMatcher matcher;
    private String variableField;
    private String variableExpression;

    @Before
    public void setUp() {
        event = new HashMap<>();
        variableField = "compare_field";
        variableExpression = "${compare_field}";
        event.put(variableField, 1);
    }

    @Test
    public void compareEqualConstantIntegers() {
        event.put(field, 1L);
        matcher = NumericCompareMatcher.builder()
                .expression("1")
                .comparator(NumericCompareTypeDto.EQUAL.getComparator())
                .fieldName(field)
                .build();
        Assert.assertFalse(matcher.canModifyEvent());
        Assert.assertFalse(matcher.isNegated());
        var result = matcher.match(event);
        Assert.assertEquals(EvaluationResult.MATCH, result);
    }

    @Test
    public void compareNotEqualConstantIntegers() {
        event.put(field, 1L);
        matcher = NumericCompareMatcher.builder()
                .expression("2")
                .comparator(NumericCompareTypeDto.EQUAL.getComparator())
                .fieldName(field)
                .build();
        Assert.assertFalse(matcher.canModifyEvent());
        Assert.assertFalse(matcher.isNegated());
        var result = matcher.match(event);
        Assert.assertEquals(EvaluationResult.NO_MATCH, result);
    }

    @Test
    public void compareLesserConstantIntegers() {
        event.put(field, 1L);
        matcher = NumericCompareMatcher.builder()
                .expression("2")
                .comparator(NumericCompareTypeDto.LESSER.getComparator())
                .fieldName(field)
                .build();
        Assert.assertFalse(matcher.canModifyEvent());
        Assert.assertFalse(matcher.isNegated());
        var result = matcher.match(event);
        Assert.assertEquals(EvaluationResult.MATCH, result);
    }

    @Test
    public void compareLesserEqualConstantIntegers() {
        event.put(field, 1L);
        matcher = NumericCompareMatcher.builder()
                .expression("1")
                .comparator(NumericCompareTypeDto.LESSER_EQUAL.getComparator())
                .fieldName(field)
                .build();
        Assert.assertFalse(matcher.canModifyEvent());
        Assert.assertFalse(matcher.isNegated());
        var result = matcher.match(event);
        Assert.assertEquals(EvaluationResult.MATCH, result);
    }

    @Test
    public void compareGreaterConstantIntegers() {
        event.put(field, 2L);
        matcher = NumericCompareMatcher.builder()
                .expression("1")
                .comparator(NumericCompareTypeDto.GREATER.getComparator())
                .fieldName(field)
                .build();
        Assert.assertFalse(matcher.canModifyEvent());
        Assert.assertFalse(matcher.isNegated());
        var result = matcher.match(event);
        Assert.assertEquals(EvaluationResult.MATCH, result);
    }

    @Test
    public void compareGreaterEqualsConstantIntegers() {
        event.put(field, 2L);
        matcher = NumericCompareMatcher.builder()
                .expression("1")
                .comparator(NumericCompareTypeDto.GREATER_EQUAL.getComparator())
                .fieldName(field)
                .build();
        Assert.assertFalse(matcher.canModifyEvent());
        Assert.assertFalse(matcher.isNegated());
        var result = matcher.match(event);
        Assert.assertEquals(EvaluationResult.MATCH, result);
    }

    @Test
    public void compareEqualConstantStringField() {
        event.put(field, "1");
        matcher = NumericCompareMatcher.builder()
                .expression("1")
                .comparator(NumericCompareTypeDto.EQUAL.getComparator())
                .fieldName(field)
                .build();
        Assert.assertFalse(matcher.canModifyEvent());
        Assert.assertFalse(matcher.isNegated());
        var result = matcher.match(event);
        Assert.assertEquals(EvaluationResult.MATCH, result);
    }

    @Test
    public void compareEqualVariableExpressionIntegers() {
        event.put(field, 1L);
        matcher = NumericCompareMatcher.builder()
                .expression(variableExpression)
                .comparator(NumericCompareTypeDto.EQUAL.getComparator())
                .fieldName(field)
                .build();
        Assert.assertFalse(matcher.canModifyEvent());
        Assert.assertFalse(matcher.isNegated());
        var result = matcher.match(event);
        Assert.assertEquals(EvaluationResult.MATCH, result);
    }

    @Test
    public void compareLesserVariableExpressionIntegers() {
        event.put(field, -1L);
        matcher = NumericCompareMatcher.builder()
                .expression(variableExpression)
                .comparator(NumericCompareTypeDto.LESSER.getComparator())
                .fieldName(field)
                .build();
        Assert.assertFalse(matcher.canModifyEvent());
        Assert.assertFalse(matcher.isNegated());
        var result = matcher.match(event);
        Assert.assertEquals(EvaluationResult.MATCH, result);
    }

    @Test
    public void compareLesserEqualVariableExpressionIntegers() {
        event.put(field, 1L);
        matcher = NumericCompareMatcher.builder()
                .expression(variableExpression)
                .comparator(NumericCompareTypeDto.LESSER_EQUAL.getComparator())
                .fieldName(field)
                .build();
        Assert.assertFalse(matcher.canModifyEvent());
        Assert.assertFalse(matcher.isNegated());
        var result = matcher.match(event);
        Assert.assertEquals(EvaluationResult.MATCH, result);
    }

    @Test
    public void compareGreaterVariableExpressionIntegers() {
        event.put(field, 2L);
        matcher = NumericCompareMatcher.builder()
                .expression(variableExpression)
                .comparator(NumericCompareTypeDto.GREATER.getComparator())
                .fieldName(field)
                .build();
        Assert.assertFalse(matcher.canModifyEvent());
        Assert.assertFalse(matcher.isNegated());
        var result = matcher.match(event);
        Assert.assertEquals(EvaluationResult.MATCH, result);
    }

    @Test
    public void compareGreaterEqualsVariableExpressionIntegers() {
        event.put(field, 2L);
        matcher = NumericCompareMatcher.builder()
                .expression(variableExpression)
                .comparator(NumericCompareTypeDto.GREATER_EQUAL.getComparator())
                .fieldName(field)
                .build();
        Assert.assertFalse(matcher.canModifyEvent());
        Assert.assertFalse(matcher.isNegated());
        var result = matcher.match(event);
        Assert.assertEquals(EvaluationResult.MATCH, result);
    }

    @Test
    public void compareEqualVariableStringField() {
        event.put(field, "1");
        matcher = NumericCompareMatcher.builder()
                .expression(variableExpression)
                .comparator(NumericCompareTypeDto.EQUAL.getComparator())
                .fieldName(field)
                .build();
        Assert.assertFalse(matcher.canModifyEvent());
        Assert.assertFalse(matcher.isNegated());
        var result = matcher.match(event);
        Assert.assertEquals(EvaluationResult.MATCH, result);
    }

    @Test
    public void noFieldVariableField() {
        event.put(field, "1");
        event.remove(variableField);
        matcher = NumericCompareMatcher.builder()
                .expression(variableExpression)
                .comparator(NumericCompareTypeDto.EQUAL.getComparator())
                .fieldName(field)
                .build();
        Assert.assertFalse(matcher.canModifyEvent());
        Assert.assertFalse(matcher.isNegated());
        var result = matcher.match(event);
        Assert.assertEquals(EvaluationResult.NO_MATCH, result);
    }

    @Test
    public void MissingFieldNoMatch() {
        matcher = NumericCompareMatcher.builder()
                .expression("1")
                .comparator(NumericCompareTypeDto.EQUAL.getComparator())
                .fieldName(field)
                .build();
        Assert.assertFalse(matcher.canModifyEvent());
        Assert.assertFalse(matcher.isNegated());
        var result = matcher.match(event);
        Assert.assertEquals(EvaluationResult.NO_MATCH, result);
    }

    @Test
    public void booleanFieldNoMatch() {
        event.put(field, true);
        matcher = NumericCompareMatcher.builder()
                .expression("1")
                .comparator(NumericCompareTypeDto.EQUAL.getComparator())
                .fieldName(field)
                .build();
        Assert.assertFalse(matcher.canModifyEvent());
        Assert.assertFalse(matcher.isNegated());
        var result = matcher.match(event);
        Assert.assertEquals(EvaluationResult.NO_MATCH, result);
    }

    @Test
    public void stringFieldNoMatch() {
        event.put(field, "not a number");
        matcher = NumericCompareMatcher.builder()
                .expression("1")
                .comparator(NumericCompareTypeDto.EQUAL.getComparator())
                .fieldName(field)
                .build();
        Assert.assertFalse(matcher.canModifyEvent());
        Assert.assertFalse(matcher.isNegated());
        var result = matcher.match(event);
        Assert.assertEquals(EvaluationResult.NO_MATCH, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void missingArguments() {
        matcher = NumericCompareMatcher.builder()
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void missingComparatorArguments() {
        matcher = NumericCompareMatcher.builder()
                .expression("1")
                .fieldName(field)
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidNumericFormat() {
        matcher = NumericCompareMatcher.builder()
                .expression("INVALID")
                .comparator(NumericCompareTypeDto.EQUAL.getComparator())
                .fieldName(field)
                .build();
    }
}
