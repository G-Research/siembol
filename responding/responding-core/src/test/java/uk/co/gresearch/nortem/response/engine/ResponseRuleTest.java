package uk.co.gresearch.nortem.response.engine;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import uk.co.gresearch.nortem.response.common.*;

import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;
import static uk.co.gresearch.nortem.response.common.ResponseEvaluationResult.FILTERED;
import static uk.co.gresearch.nortem.response.common.ResponseEvaluationResult.MATCH;
import static uk.co.gresearch.nortem.response.common.ResponseEvaluationResult.NO_MATCH;


public class ResponseRuleTest {
    private final String ruleName = "Test_rule";
    private final int ruleVerion = 1;
    private final String fullRuleName = "Test_rule_v1";

    private final String metchesMetricName = MetricNames.RULE_MATCHES.getNameWithSuffix(fullRuleName);
    private final String errorMetricName = MetricNames.RULE_ERROR_MATCHES.getNameWithSuffix(fullRuleName);
    private final String filteredMetricName = MetricNames.RULE_FILTERS.getNameWithSuffix(fullRuleName);

    private ResponseAlert alert;
    Evaluable evaluator;
    Evaluable evaluatorNext;
    ResponseRule.Builder builder;
    ResponseRule rule;
    TestMetricFactory metricFactory;
    RespondingResult evaluatorResult;
    RespondingResultAttributes resultAttributes;
    RespondingResult evaluatorNextResult;
    RespondingResultAttributes resultNextAttributes;

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

        resultNextAttributes = new RespondingResultAttributes();
        resultNextAttributes.setAlert(alert);
        evaluatorNextResult = new RespondingResult(RespondingResult.StatusCode.OK, resultNextAttributes);
        evaluatorNext = Mockito.mock(Evaluable.class);
        when(evaluatorNext.evaluate(alert)).thenReturn(evaluatorNextResult);

        metricFactory = new TestMetricFactory();
        builder = new ResponseRule.Builder()
                .metricFactory(metricFactory)
                .ruleName(ruleName)
                .ruleVersion(ruleVerion)
                .addEvaluator(evaluator);
        rule = builder.build();
    }

    @Test
    public void testOneEvaluatorNoMatch() {
        resultAttributes.setResult(NO_MATCH);
        RespondingResult result = rule.evaluate(alert);
        Mockito.verify(evaluator, times(1)).evaluate(alert);
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
        resultAttributes.setResult(MATCH);
        resultNextAttributes.setResult(NO_MATCH);

        builder.addEvaluator(evaluatorNext);
        rule = builder.build();

        RespondingResult result = rule.evaluate(alert);
        Mockito.verify(evaluator, times(1)).evaluate(alert);
        Mockito.verify(evaluatorNext, times(1)).evaluate(alert);

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
        resultAttributes.setResult(MATCH);
        RespondingResult result = rule.evaluate(alert);
        Mockito.verify(evaluator, times(1)).evaluate(alert);
        Assert.assertEquals(RespondingResult.StatusCode.OK, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes());
        Assert.assertEquals(MATCH, result.getAttributes().getResult());
        Assert.assertEquals(3, result.getAttributes().getAlert().size());
        Assert.assertEquals(fullRuleName, alert.get(ResponseFields.RULE_NAME.toString()));
        Assert.assertEquals(1, metricFactory.getCounter(metchesMetricName).getValue());
        Assert.assertEquals(0, metricFactory.getCounter(errorMetricName).getValue());
        Assert.assertEquals(0, metricFactory.getCounter(filteredMetricName).getValue());
    }

    @Test
    public void testTwoEvaluatorsMatch() {
        resultAttributes.setResult(MATCH);
        resultNextAttributes.setResult(MATCH);

        builder.addEvaluator(evaluatorNext);
        rule = builder.build();

        RespondingResult result = rule.evaluate(alert);
        Mockito.verify(evaluator, times(1)).evaluate(alert);
        Mockito.verify(evaluatorNext, times(1)).evaluate(alert);

        Assert.assertEquals(RespondingResult.StatusCode.OK, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes());
        Assert.assertEquals(MATCH, result.getAttributes().getResult());
        Assert.assertEquals(3, result.getAttributes().getAlert().size());
        Assert.assertEquals(fullRuleName, alert.get(ResponseFields.RULE_NAME.toString()));
        Assert.assertEquals(1, metricFactory.getCounter(metchesMetricName).getValue());
        Assert.assertEquals(0, metricFactory.getCounter(errorMetricName).getValue());
        Assert.assertEquals(0, metricFactory.getCounter(filteredMetricName).getValue());
    }

    @Test
    public void testOneEvaluatorFiltered() {
        resultAttributes.setResult(ResponseEvaluationResult.FILTERED);
        RespondingResult result = rule.evaluate(alert);
        Mockito.verify(evaluator, times(1)).evaluate(alert);
        Assert.assertEquals(RespondingResult.StatusCode.OK, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes());
        Assert.assertEquals(FILTERED, result.getAttributes().getResult());
        Assert.assertEquals(2, result.getAttributes().getAlert().size());
        Assert.assertEquals(0, metricFactory.getCounter(metchesMetricName).getValue());
        Assert.assertEquals(0, metricFactory.getCounter(errorMetricName).getValue());
        Assert.assertEquals(1, metricFactory.getCounter(filteredMetricName).getValue());
    }

    @Test
    public void testTwoEvaluatorsFiltered() {
        resultAttributes.setResult(MATCH);
        resultNextAttributes.setResult(FILTERED);

        builder.addEvaluator(evaluatorNext);
        rule = builder.build();

        RespondingResult result = rule.evaluate(alert);
        Mockito.verify(evaluator, times(1)).evaluate(alert);
        Mockito.verify(evaluatorNext, times(1)).evaluate(alert);

        Assert.assertEquals(RespondingResult.StatusCode.OK, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes());
        Assert.assertEquals(FILTERED, result.getAttributes().getResult());
        Assert.assertEquals(2, result.getAttributes().getAlert().size());
        Assert.assertEquals(0, metricFactory.getCounter(metchesMetricName).getValue());
        Assert.assertEquals(0, metricFactory.getCounter(errorMetricName).getValue());
        Assert.assertEquals(1, metricFactory.getCounter(filteredMetricName).getValue());
    }

    @Test
    public void testOneEvaluatorError() {
        when(evaluator.evaluate(alert)).thenReturn(RespondingResult.fromException(new IllegalStateException()));
        RespondingResult result = rule.evaluate(alert);
        Mockito.verify(evaluator, times(1)).evaluate(alert);
        Assert.assertEquals(RespondingResult.StatusCode.ERROR, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes());
        Assert.assertNotNull(result.getAttributes().getMessage());
        Assert.assertEquals(0, metricFactory.getCounter(metchesMetricName).getValue());
        Assert.assertEquals(1, metricFactory.getCounter(errorMetricName).getValue());
        Assert.assertEquals(0, metricFactory.getCounter(filteredMetricName).getValue());
    }

    @Test
    public void testTwoEvaluatorsError() {
        resultAttributes.setResult(MATCH);
        when(evaluatorNext.evaluate(alert)).thenReturn(RespondingResult.fromException(new IllegalStateException()));
        builder.addEvaluator(evaluatorNext);
        rule = builder.build();

        RespondingResult result = rule.evaluate(alert);
        Mockito.verify(evaluator, times(1)).evaluate(alert);
        Mockito.verify(evaluatorNext, times(1)).evaluate(alert);

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
        RespondingResult result = rule.evaluate(alert);
        Mockito.verify(evaluator, times(1)).evaluate(alert);
        Assert.assertEquals(RespondingResult.StatusCode.ERROR, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes());
        Assert.assertNotNull(result.getAttributes().getMessage());
        Assert.assertEquals(0, metricFactory.getCounter(metchesMetricName).getValue());
        Assert.assertEquals(1, metricFactory.getCounter(errorMetricName).getValue());
        Assert.assertEquals(0, metricFactory.getCounter(filteredMetricName).getValue());
    }

    @Test
    public void testTwoEvaluatorsException() {
        resultAttributes.setResult(MATCH);
        when(evaluatorNext.evaluate(alert)).thenThrow(new IllegalStateException());
        builder.addEvaluator(evaluatorNext);
        rule = builder.build();

        RespondingResult result = rule.evaluate(alert);
        Mockito.verify(evaluator, times(1)).evaluate(alert);
        Mockito.verify(evaluatorNext, times(1)).evaluate(alert);

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