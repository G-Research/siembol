package uk.co.gresearch.siembol.response.engine;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import uk.co.gresearch.siembol.response.common.*;
import uk.co.gresearch.siembol.response.evaluators.fixed.FixedResultEvaluator;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;
import static uk.co.gresearch.siembol.response.common.ResponseEvaluationResult.FILTERED;
import static uk.co.gresearch.siembol.response.common.ResponseEvaluationResult.MATCH;
import static uk.co.gresearch.siembol.response.common.ResponseEvaluationResult.NO_MATCH;


public class ResponseRuleTest {
    private final String ruleName = "Test_rule";
    private final int ruleVerion = 1;
    private final String fullRuleName = "Test_rule_v1";

    private final String metchesMetricName = MetricNames.RULE_MATCHES.getNameWithSuffix(ruleName);
    private final String errorMetricName = MetricNames.RULE_ERROR_MATCHES.getNameWithSuffix(ruleName);
    private final String filteredMetricName = MetricNames.RULE_FILTERS.getNameWithSuffix(ruleName);

    private ResponseAlert alert;
    Evaluable evaluator;
    Evaluable evaluatorNext;
    ResponseRule.Builder builder;
    ResponseRule rule;
    TestMetricFactory metricFactory;
    RespondingResult evaluatorResult;
    RespondingResultAttributes resultAttributes;

    @Before
    public void setUp() {
        alert = new ResponseAlert();
        alert.put("field1", "VALUE1");
        alert.put("field2", "VALUE2");

        resultAttributes = new RespondingResultAttributes();
        resultAttributes.setAlert(alert);
        evaluatorResult = new RespondingResult(RespondingResult.StatusCode.OK, resultAttributes);
        evaluator = Mockito.mock(Evaluable.class);
        when(evaluator.evaluate(alert)).thenReturn(evaluatorResult);

        metricFactory = new TestMetricFactory();
        builder = new ResponseRule.Builder()
                .metricFactory(metricFactory)
                .ruleName(ruleName)
                .ruleVersion(ruleVerion);

    }

    @Test
    public void testOneEvaluatorNoMatch() {
        builder.addEvaluator(new FixedResultEvaluator(NO_MATCH));
        rule = builder.build();
        RespondingResult result = rule.evaluate(alert);

        Assert.assertEquals(RespondingResult.StatusCode.OK, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes());
        Assert.assertEquals(NO_MATCH, result.getAttributes().getResult());
        Assert.assertEquals(2, result.getAttributes().getAlert().size());
        Assert.assertEquals(0, metricFactory.getCounter(metchesMetricName).getValue());
        Assert.assertEquals(0, metricFactory.getCounter(errorMetricName).getValue());
        Assert.assertEquals(0, metricFactory.getCounter(filteredMetricName).getValue());
    }

    @Test
    public void testTwoEvaluatorsNoMatch() {
        builder.addEvaluator(new FixedResultEvaluator(MATCH));
        builder.addEvaluator(new FixedResultEvaluator(NO_MATCH));
        rule = builder.build();

        builder.addEvaluator(evaluatorNext);
        rule = builder.build();

        RespondingResult result = rule.evaluate(alert);

        Assert.assertEquals(RespondingResult.StatusCode.OK, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes());
        Assert.assertEquals(NO_MATCH, result.getAttributes().getResult());
        Assert.assertEquals(2, result.getAttributes().getAlert().size());
        Assert.assertEquals(0, metricFactory.getCounter(metchesMetricName).getValue());
        Assert.assertEquals(0, metricFactory.getCounter(errorMetricName).getValue());
        Assert.assertEquals(0, metricFactory.getCounter(filteredMetricName).getValue());
    }

    @Test
    public void testOneEvaluatorMatch() {
        builder.addEvaluator(new FixedResultEvaluator(MATCH));
        rule = builder.build();
        RespondingResult result = rule.evaluate(alert);
        Assert.assertEquals(RespondingResult.StatusCode.OK, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes());
        Assert.assertEquals(MATCH, result.getAttributes().getResult());
        Assert.assertEquals(4, result.getAttributes().getAlert().size());
        Assert.assertEquals(fullRuleName, result.getAttributes().getAlert()
                .get(ResponseFields.FULL_RULE_NAME.toString()));
        Assert.assertEquals(ruleName, result.getAttributes().getAlert().get(ResponseFields.RULE_NAME.toString()));

        Assert.assertEquals(1, metricFactory.getCounter(metchesMetricName).getValue());
        Assert.assertEquals(0, metricFactory.getCounter(errorMetricName).getValue());
        Assert.assertEquals(0, metricFactory.getCounter(filteredMetricName).getValue());
    }

    @Test
    public void testTwoEvaluatorsMatch() {
        builder.addEvaluator(new FixedResultEvaluator(MATCH));
        builder.addEvaluator(new FixedResultEvaluator(MATCH));
        rule = builder.build();
        RespondingResult result = rule.evaluate(alert);

        Assert.assertEquals(RespondingResult.StatusCode.OK, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes());
        Assert.assertEquals(MATCH, result.getAttributes().getResult());
        Assert.assertEquals(4, result.getAttributes().getAlert().size());
        Assert.assertEquals(fullRuleName, result.getAttributes().getAlert()
                .get(ResponseFields.FULL_RULE_NAME.toString()));
        Assert.assertEquals(ruleName, result.getAttributes().getAlert().get(ResponseFields.RULE_NAME.toString()));

        Assert.assertEquals(1, metricFactory.getCounter(metchesMetricName).getValue());
        Assert.assertEquals(0, metricFactory.getCounter(errorMetricName).getValue());
        Assert.assertEquals(0, metricFactory.getCounter(filteredMetricName).getValue());
    }

    @Test
    public void testOneEvaluatorFiltered() {
        builder.addEvaluator(new FixedResultEvaluator(FILTERED));
        rule = builder.build();
        RespondingResult result = rule.evaluate(alert);

        Assert.assertEquals(RespondingResult.StatusCode.OK, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes());
        Assert.assertEquals(FILTERED, result.getAttributes().getResult());
        Assert.assertEquals(4, result.getAttributes().getAlert().size());
        Assert.assertEquals(fullRuleName, result.getAttributes().getAlert()
                .get(ResponseFields.FULL_RULE_NAME.toString()));
        Assert.assertEquals(ruleName, result.getAttributes().getAlert().get(ResponseFields.RULE_NAME.toString()));

        Assert.assertEquals(0, metricFactory.getCounter(metchesMetricName).getValue());
        Assert.assertEquals(0, metricFactory.getCounter(errorMetricName).getValue());
        Assert.assertEquals(1, metricFactory.getCounter(filteredMetricName).getValue());
    }

    @Test
    public void testTwoEvaluatorsFiltered() {
        builder.addEvaluator(new FixedResultEvaluator(MATCH));
        builder.addEvaluator(new FixedResultEvaluator(FILTERED));
        rule = builder.build();
        RespondingResult result = rule.evaluate(alert);

        Assert.assertEquals(RespondingResult.StatusCode.OK, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes());
        Assert.assertEquals(FILTERED, result.getAttributes().getResult());
        Assert.assertEquals(4, result.getAttributes().getAlert().size());
        Assert.assertEquals(fullRuleName, result.getAttributes().getAlert()
                .get(ResponseFields.FULL_RULE_NAME.toString()));
        Assert.assertEquals(ruleName, result.getAttributes().getAlert().get(ResponseFields.RULE_NAME.toString()));
        Assert.assertEquals(0, metricFactory.getCounter(metchesMetricName).getValue());
        Assert.assertEquals(0, metricFactory.getCounter(errorMetricName).getValue());
        Assert.assertEquals(1, metricFactory.getCounter(filteredMetricName).getValue());
    }

    @Test
    public void testOneEvaluatorError() {
        when(evaluator.evaluate(alert)).thenReturn(RespondingResult.fromException(new IllegalStateException()));
        builder.addEvaluator(evaluator);
        rule = builder.build();
        RespondingResult result = rule.evaluate(alert);
        Mockito.verify(evaluator, times(1)).evaluate(any());
        Assert.assertEquals(RespondingResult.StatusCode.ERROR, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes());
        Assert.assertNotNull(result.getAttributes().getMessage());
        Assert.assertEquals(0, metricFactory.getCounter(metchesMetricName).getValue());
        Assert.assertEquals(1, metricFactory.getCounter(errorMetricName).getValue());
        Assert.assertEquals(0, metricFactory.getCounter(filteredMetricName).getValue());
    }

    @Test
    public void testTwoEvaluatorsError() {
        when(evaluator.evaluate(alert)).thenReturn(RespondingResult.fromException(new IllegalStateException()));
        builder.addEvaluator(new FixedResultEvaluator(MATCH));
        builder.addEvaluator(evaluator);
        rule = builder.build();
        RespondingResult result = rule.evaluate(alert);
        Mockito.verify(evaluator, times(1)).evaluate(any());

        Assert.assertEquals(RespondingResult.StatusCode.ERROR, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes());
        Assert.assertNotNull(result.getAttributes().getMessage());
        Assert.assertEquals(0, metricFactory.getCounter(metchesMetricName).getValue());
        Assert.assertEquals(1, metricFactory.getCounter(errorMetricName).getValue());
        Assert.assertEquals(0, metricFactory.getCounter(filteredMetricName).getValue());
    }


    @Test
    public void testOneEvaluatorException() {
        when(evaluator.evaluate(alert)).thenThrow(new IllegalStateException());
        builder.addEvaluator(evaluator);
        rule = builder.build();
        RespondingResult result = rule.evaluate(alert);
        Mockito.verify(evaluator, times(1)).evaluate(any());
        Assert.assertEquals(RespondingResult.StatusCode.ERROR, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes());
        Assert.assertNotNull(result.getAttributes().getMessage());
        Assert.assertEquals(0, metricFactory.getCounter(metchesMetricName).getValue());
        Assert.assertEquals(1, metricFactory.getCounter(errorMetricName).getValue());
        Assert.assertEquals(0, metricFactory.getCounter(filteredMetricName).getValue());
    }

    @Test
    public void testTwoEvaluatorsException() {
        when(evaluator.evaluate(alert)).thenThrow(new IllegalStateException());
        builder.addEvaluator(new FixedResultEvaluator(MATCH));
        builder.addEvaluator(evaluator);
        rule = builder.build();
        RespondingResult result = rule.evaluate(alert);
        Mockito.verify(evaluator, times(1)).evaluate(any());

        Assert.assertEquals(RespondingResult.StatusCode.ERROR, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes());
        Assert.assertNotNull(result.getAttributes().getMessage());
        Assert.assertEquals(0, metricFactory.getCounter(metchesMetricName).getValue());
        Assert.assertEquals(1, metricFactory.getCounter(errorMetricName).getValue());
        Assert.assertEquals(0, metricFactory.getCounter(filteredMetricName).getValue());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBuilderMissingAttributes() {
        new ResponseRule.Builder().build();
    }
}