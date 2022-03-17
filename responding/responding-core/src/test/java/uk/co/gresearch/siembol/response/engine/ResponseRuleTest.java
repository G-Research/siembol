package uk.co.gresearch.siembol.response.engine;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import uk.co.gresearch.siembol.common.metrics.SiembolMetrics;
import uk.co.gresearch.siembol.common.metrics.test.SiembolMetricsTestRegistrar;
import uk.co.gresearch.siembol.response.common.*;
import uk.co.gresearch.siembol.response.evaluators.fixed.FixedResultEvaluator;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;
import static uk.co.gresearch.siembol.response.common.ResponseEvaluationResult.FILTERED;
import static uk.co.gresearch.siembol.response.common.ResponseEvaluationResult.MATCH;
import static uk.co.gresearch.siembol.response.common.ResponseEvaluationResult.NO_MATCH;


public class ResponseRuleTest {
    private final String ruleName = "Test_rule";
    private final int ruleVerion = 1;
    private final String fullRuleName = "Test_rule_v1";

    private final String metchesMetricName = SiembolMetrics.RESPONSE_RULE_MATCHED.getMetricName(ruleName);
    private final String errorMetricName = SiembolMetrics.RESPONSE_RULE_ERROR_MATCH.getMetricName(ruleName);
    private final String filteredMetricName = SiembolMetrics.RESPONSE_RULE_FILTERED.getMetricName(ruleName);

    private ResponseAlert alert;
    private Evaluable evaluator;
    private Evaluable evaluatorNext;
    private ResponseRule.Builder builder;
    private ResponseRule rule;
    private SiembolMetricsTestRegistrar metricsTestRegistrar;
    private RespondingResult evaluatorResult;
    private RespondingResultAttributes resultAttributes;

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

        metricsTestRegistrar = new SiembolMetricsTestRegistrar();
        builder = new ResponseRule.Builder()
                .metricsRegistrar(metricsTestRegistrar.cachedRegistrar())
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
        Assert.assertEquals(0, metricsTestRegistrar.getCounterValue(metchesMetricName));
        Assert.assertEquals(0, metricsTestRegistrar.getCounterValue(errorMetricName));
        Assert.assertEquals(0, metricsTestRegistrar.getCounterValue(filteredMetricName));
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
        Assert.assertEquals(0, metricsTestRegistrar.getCounterValue(metchesMetricName));
        Assert.assertEquals(0, metricsTestRegistrar.getCounterValue(errorMetricName));
        Assert.assertEquals(0, metricsTestRegistrar.getCounterValue(filteredMetricName));
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

        Assert.assertEquals(1, metricsTestRegistrar.getCounterValue(metchesMetricName));
        Assert.assertEquals(0, metricsTestRegistrar.getCounterValue(errorMetricName));
        Assert.assertEquals(0, metricsTestRegistrar.getCounterValue(filteredMetricName));
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

        Assert.assertEquals(1, metricsTestRegistrar.getCounterValue(metchesMetricName));
        Assert.assertEquals(0, metricsTestRegistrar.getCounterValue(errorMetricName));
        Assert.assertEquals(0, metricsTestRegistrar.getCounterValue(filteredMetricName));
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

        Assert.assertEquals(0, metricsTestRegistrar.getCounterValue(metchesMetricName));
        Assert.assertEquals(0, metricsTestRegistrar.getCounterValue(errorMetricName));
        Assert.assertEquals(1, metricsTestRegistrar.getCounterValue(filteredMetricName));
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
        Assert.assertEquals(0, metricsTestRegistrar.getCounterValue(metchesMetricName));
        Assert.assertEquals(0, metricsTestRegistrar.getCounterValue(errorMetricName));
        Assert.assertEquals(1, metricsTestRegistrar.getCounterValue(filteredMetricName));
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
        Assert.assertEquals(0, metricsTestRegistrar.getCounterValue(metchesMetricName));
        Assert.assertEquals(1, metricsTestRegistrar.getCounterValue(errorMetricName));
        Assert.assertEquals(0, metricsTestRegistrar.getCounterValue(filteredMetricName));
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
        Assert.assertEquals(0, metricsTestRegistrar.getCounterValue(metchesMetricName));
        Assert.assertEquals(1, metricsTestRegistrar.getCounterValue(errorMetricName));
        Assert.assertEquals(0, metricsTestRegistrar.getCounterValue(filteredMetricName));
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
        Assert.assertEquals(0, metricsTestRegistrar.getCounterValue(metchesMetricName));
        Assert.assertEquals(1, metricsTestRegistrar.getCounterValue(errorMetricName));
        Assert.assertEquals(0, metricsTestRegistrar.getCounterValue(filteredMetricName));
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
        Assert.assertEquals(0, metricsTestRegistrar.getCounterValue(metchesMetricName));
        Assert.assertEquals(1, metricsTestRegistrar.getCounterValue(errorMetricName));
        Assert.assertEquals(0, metricsTestRegistrar.getCounterValue(filteredMetricName));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBuilderMissingAttributes() {
        new ResponseRule.Builder().build();
    }
}