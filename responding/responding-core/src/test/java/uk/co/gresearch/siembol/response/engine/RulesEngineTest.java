package uk.co.gresearch.siembol.response.engine;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import uk.co.gresearch.siembol.common.metrics.SiembolMetrics;
import uk.co.gresearch.siembol.common.metrics.test.SiembolMetricsTestRegistrar;
import uk.co.gresearch.siembol.response.common.*;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;
import static uk.co.gresearch.siembol.response.common.ResponseEvaluationResult.FILTERED;
import static uk.co.gresearch.siembol.response.common.ResponseEvaluationResult.MATCH;
import static uk.co.gresearch.siembol.response.common.ResponseEvaluationResult.NO_MATCH;

public class RulesEngineTest {

    private ResponseAlert alert;
    private Evaluable rule;
    private Evaluable ruleNext;
    private List<Evaluable> rules;
    private RulesEngine.Builder builder;
    private SiembolMetricsTestRegistrar metricsTestRegistrar;
    private RespondingResult ruleResult;
    private RespondingResultAttributes resultAttributes;
    private RespondingResultAttributes metadataAttributes;
    private RespondingResult ruleNextResult;
    private RespondingResultAttributes resultNextAttributes;
    private RulesEngine engine;

    @Before
    public void setUp() {
        alert = new ResponseAlert();
        alert.put("field1", "VALUE1");
        alert.put("field2", "VALUE2");

        resultAttributes = new RespondingResultAttributes();
        resultAttributes.setAlert(alert);
        ruleResult = new RespondingResult(RespondingResult.StatusCode.OK, resultAttributes);
        rule = Mockito.mock(Evaluable.class);
        when(rule.evaluate(alert)).thenReturn(ruleResult);

        resultNextAttributes = new RespondingResultAttributes();
        resultNextAttributes.setAlert(alert);
        ruleNextResult = new RespondingResult(RespondingResult.StatusCode.OK, resultNextAttributes);
        ruleNext = Mockito.mock(Evaluable.class);
        when(ruleNext.evaluate(alert)).thenReturn(ruleNextResult);
        rules = Arrays.asList(rule, ruleNext);

        metadataAttributes = new RespondingResultAttributes();
        metricsTestRegistrar = new SiembolMetricsTestRegistrar();
        builder = new RulesEngine.Builder()
                .metricsRegistrar(metricsTestRegistrar.cachedRegistrar())
                .metadata(metadataAttributes)
                .rules(rules);

        engine = builder.build();
    }

    @Test
    public void testFirstRuleMatch() {
        resultAttributes.setResult(MATCH);
        RespondingResult result = engine.evaluate(alert);
        Mockito.verify(rule, times(1)).evaluate(alert);
        Mockito.verify(ruleNext, times(0)).evaluate(alert);

        Assert.assertEquals(RespondingResult.StatusCode.OK, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes());
        Assert.assertEquals(MATCH, result.getAttributes().getResult());
        Assert.assertEquals(1, metricsTestRegistrar
                .getCounterValue(SiembolMetrics.RESPONSE_ENGINE_PROCESSED_ALERTS.getMetricName()));
        Assert.assertEquals(0, metricsTestRegistrar
                .getCounterValue(SiembolMetrics.RESPONSE_ENGINE_ERRORS.getMetricName()));
        Assert.assertEquals(0, metricsTestRegistrar
                .getCounterValue(SiembolMetrics.RESPONSE_ENGINE_FILTERED_ALERTS.getMetricName()));
        Assert.assertEquals(0, metricsTestRegistrar
                .getCounterValue(SiembolMetrics.RESPONSE_ENGINE_NO_MATCHES.getMetricName()));
        Assert.assertEquals(RespondingResult.StatusCode.OK, engine.getRulesMetadata().getStatusCode());
        Assert.assertEquals(metadataAttributes, engine.getRulesMetadata().getAttributes());
    }

    @Test
    public void testSecondRuleMatch() {
        resultAttributes.setResult(NO_MATCH);
        resultNextAttributes.setResult(MATCH);
        RespondingResult result = engine.evaluate(alert);
        Mockito.verify(rule, times(1)).evaluate(alert);
        Mockito.verify(ruleNext, times(1)).evaluate(alert);

        Assert.assertEquals(RespondingResult.StatusCode.OK, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes());
        Assert.assertEquals(MATCH, result.getAttributes().getResult());
        Assert.assertEquals(1, metricsTestRegistrar
                .getCounterValue(SiembolMetrics.RESPONSE_ENGINE_PROCESSED_ALERTS.getMetricName()));
        Assert.assertEquals(0, metricsTestRegistrar
                .getCounterValue(SiembolMetrics.RESPONSE_ENGINE_ERRORS.getMetricName()));
        Assert.assertEquals(0, metricsTestRegistrar
                .getCounterValue(SiembolMetrics.RESPONSE_ENGINE_FILTERED_ALERTS.getMetricName()));
        Assert.assertEquals(0, metricsTestRegistrar
                .getCounterValue(SiembolMetrics.RESPONSE_ENGINE_NO_MATCHES.getMetricName()));
        Assert.assertEquals(RespondingResult.StatusCode.OK, engine.getRulesMetadata().getStatusCode());
        Assert.assertEquals(metadataAttributes, engine.getRulesMetadata().getAttributes());
    }

    @Test
    public void testFirstRuleFiltered() {
        resultAttributes.setResult(FILTERED);
        RespondingResult result = engine.evaluate(alert);
        Mockito.verify(rule, times(1)).evaluate(alert);
        Mockito.verify(ruleNext, times(0)).evaluate(alert);

        Assert.assertEquals(RespondingResult.StatusCode.OK, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes());
        Assert.assertEquals(FILTERED, result.getAttributes().getResult());
        Assert.assertEquals(1, metricsTestRegistrar
                .getCounterValue(SiembolMetrics.RESPONSE_ENGINE_PROCESSED_ALERTS.getMetricName()));
        Assert.assertEquals(0, metricsTestRegistrar
                .getCounterValue(SiembolMetrics.RESPONSE_ENGINE_ERRORS.getMetricName()));
        Assert.assertEquals(1, metricsTestRegistrar
                .getCounterValue(SiembolMetrics.RESPONSE_ENGINE_FILTERED_ALERTS.getMetricName()));
        Assert.assertEquals(0, metricsTestRegistrar
                .getCounterValue(SiembolMetrics.RESPONSE_ENGINE_NO_MATCHES.getMetricName()));
        Assert.assertEquals(RespondingResult.StatusCode.OK, engine.getRulesMetadata().getStatusCode());
        Assert.assertEquals(metadataAttributes, engine.getRulesMetadata().getAttributes());
    }

    @Test
    public void testSecondRuleFiltered() {
        resultAttributes.setResult(NO_MATCH);
        resultNextAttributes.setResult(FILTERED);
        RespondingResult result = engine.evaluate(alert);
        Mockito.verify(rule, times(1)).evaluate(alert);
        Mockito.verify(ruleNext, times(1)).evaluate(alert);

        Assert.assertEquals(RespondingResult.StatusCode.OK, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes());
        Assert.assertEquals(FILTERED, result.getAttributes().getResult());
        Assert.assertEquals(1, metricsTestRegistrar
                .getCounterValue(SiembolMetrics.RESPONSE_ENGINE_PROCESSED_ALERTS.getMetricName()));
        Assert.assertEquals(0, metricsTestRegistrar
                .getCounterValue(SiembolMetrics.RESPONSE_ENGINE_ERRORS.getMetricName()));
        Assert.assertEquals(1, metricsTestRegistrar
                .getCounterValue(SiembolMetrics.RESPONSE_ENGINE_FILTERED_ALERTS.getMetricName()));
        Assert.assertEquals(0, metricsTestRegistrar
                .getCounterValue(SiembolMetrics.RESPONSE_ENGINE_NO_MATCHES.getMetricName()));
        Assert.assertEquals(RespondingResult.StatusCode.OK, engine.getRulesMetadata().getStatusCode());
        Assert.assertEquals(metadataAttributes, engine.getRulesMetadata().getAttributes());
    }

    @Test
    public void testFirstRuleErrorMatch() {
        when(rule.evaluate(alert)).thenReturn(RespondingResult.fromException(new IllegalStateException()));
        RespondingResult result = engine.evaluate(alert);
        Mockito.verify(rule, times(1)).evaluate(alert);
        Mockito.verify(ruleNext, times(0)).evaluate(alert);

        Assert.assertEquals(RespondingResult.StatusCode.ERROR, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes());
        Assert.assertNotNull(result.getAttributes().getMessage());
        Assert.assertEquals(1, metricsTestRegistrar
                .getCounterValue(SiembolMetrics.RESPONSE_ENGINE_PROCESSED_ALERTS.getMetricName()));
        Assert.assertEquals(1, metricsTestRegistrar
                .getCounterValue(SiembolMetrics.RESPONSE_ENGINE_ERRORS.getMetricName()));
        Assert.assertEquals(0, metricsTestRegistrar
                .getCounterValue(SiembolMetrics.RESPONSE_ENGINE_FILTERED_ALERTS.getMetricName()));
        Assert.assertEquals(0, metricsTestRegistrar
                .getCounterValue(SiembolMetrics.RESPONSE_ENGINE_NO_MATCHES.getMetricName()));
        Assert.assertEquals(RespondingResult.StatusCode.OK, engine.getRulesMetadata().getStatusCode());
        Assert.assertEquals(metadataAttributes, engine.getRulesMetadata().getAttributes());
    }

    @Test
    public void testSecondRuleErrorMatch() {
        resultAttributes.setResult(NO_MATCH);
        when(ruleNext.evaluate(alert)).thenReturn(RespondingResult.fromException(new IllegalStateException()));
        RespondingResult result = engine.evaluate(alert);
        Mockito.verify(rule, times(1)).evaluate(alert);
        Mockito.verify(ruleNext, times(1)).evaluate(alert);

        Assert.assertEquals(RespondingResult.StatusCode.ERROR, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes());
        Assert.assertNotNull(result.getAttributes().getMessage());
        Assert.assertEquals(1, metricsTestRegistrar
                .getCounterValue(SiembolMetrics.RESPONSE_ENGINE_PROCESSED_ALERTS.getMetricName()));
        Assert.assertEquals(1, metricsTestRegistrar
                .getCounterValue(SiembolMetrics.RESPONSE_ENGINE_ERRORS.getMetricName()));
        Assert.assertEquals(0, metricsTestRegistrar
                .getCounterValue(SiembolMetrics.RESPONSE_ENGINE_FILTERED_ALERTS.getMetricName()));
        Assert.assertEquals(0, metricsTestRegistrar
                .getCounterValue(SiembolMetrics.RESPONSE_ENGINE_NO_MATCHES.getMetricName()));
        Assert.assertEquals(RespondingResult.StatusCode.OK, engine.getRulesMetadata().getStatusCode());
        Assert.assertEquals(metadataAttributes, engine.getRulesMetadata().getAttributes());
    }

    @Test
    public void testNoRuleMatch() {
        resultAttributes.setResult(NO_MATCH);
        resultNextAttributes.setResult(NO_MATCH);
        RespondingResult result = engine.evaluate(alert);
        Mockito.verify(rule, times(1)).evaluate(alert);
        Mockito.verify(ruleNext, times(1)).evaluate(alert);

        Assert.assertEquals(RespondingResult.StatusCode.OK, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes());
        Assert.assertEquals(NO_MATCH, result.getAttributes().getResult());
        Assert.assertNotNull(result.getAttributes().getMessage());
        Assert.assertEquals(1, metricsTestRegistrar
                .getCounterValue(SiembolMetrics.RESPONSE_ENGINE_PROCESSED_ALERTS.getMetricName()));
        Assert.assertEquals(1, metricsTestRegistrar
                .getCounterValue(SiembolMetrics.RESPONSE_ENGINE_NO_MATCHES.getMetricName()));
        Assert.assertEquals(0, metricsTestRegistrar
                .getCounterValue(SiembolMetrics.RESPONSE_ENGINE_ERRORS.getMetricName()));
        Assert.assertEquals(0, metricsTestRegistrar
                .getCounterValue(SiembolMetrics.RESPONSE_ENGINE_FILTERED_ALERTS.getMetricName()));
        Assert.assertEquals(RespondingResult.StatusCode.OK, engine.getRulesMetadata().getStatusCode());
        Assert.assertEquals(metadataAttributes, engine.getRulesMetadata().getAttributes());
    }
}
