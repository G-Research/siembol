package uk.co.gresearch.siembol.alerts.correlationengine;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import uk.co.gresearch.siembol.common.utils.TimeProvider;
import uk.co.gresearch.siembol.alerts.common.*;

import java.util.*;

import static org.mockito.Mockito.*;
import static org.mockito.internal.verification.VerificationModeFactory.times;
import static uk.co.gresearch.siembol.alerts.common.EvaluationResult.MATCH;
import static uk.co.gresearch.siembol.alerts.common.EvaluationResult.NO_MATCH;
import static uk.co.gresearch.siembol.alerts.common.AlertingResult.StatusCode.ERROR;
import static uk.co.gresearch.siembol.alerts.common.AlertingResult.StatusCode.OK;

public class CorrelationEngineTest {
    private TimeProvider timeProvider;
    private Map<String, Object> alert;
    private Map<String, Object> outEvent;

    private CorrelationRule rule1;
    private CorrelationRule rule2;
    private AlertingEngine engine;
    private AlertingResult resultRule2;
    private final Long currentTime = 1234L;

    @Before
    public void setUp() {
        outEvent = new HashMap<>();
        alert = new HashMap<>();
        String correlationKey = "1.2.3.4";
        alert.put(AlertingTags.CORRELATION_KEY_TAG_NAME.toString(), correlationKey);
        alert.put("detection_source", "siembol_alerts");
        List<Pair<String, String>> constants = Collections.singletonList(
                Pair.of("detection_source", "siembol_correlation_alerts"));
        List<Pair<String, Object>> protections = Collections.singletonList(
                Pair.of(AlertingFields.MAX_PER_HOUR_FIELD.getCorrelationAlertingName(), 1));
        rule1 = Mockito.mock(CorrelationRule.class);
        rule2 = Mockito.mock(CorrelationRule.class);
        timeProvider = Mockito.mock(TimeProvider.class);

        AlertingResult resultRule1 = AlertingResult.fromEvaluationResult(EvaluationResult.MATCH, alert);
        resultRule2 = AlertingResult.fromEvaluationResult(EvaluationResult.MATCH, new HashMap<>(alert));

        when(rule1.getAlertNames()).thenReturn(Arrays.asList("alert1", "alert2"));
        when(rule1.match(ArgumentMatchers.any())).thenReturn(resultRule1);


        when(rule2.getAlertNames()).thenReturn(Collections.singletonList("alert2"));
        when(rule2.match(ArgumentMatchers.any())).thenReturn(resultRule2);

        when(timeProvider.getCurrentTimeInMs()).thenReturn(currentTime);
        doNothing().when(rule1).clean(currentTime);
        doNothing().when(rule2).clean(currentTime);

        List<CorrelationRule> rules = Arrays.asList(rule1, rule2);

        engine = new CorrelationEngineImpl.Builder()
                .constants(constants)
                .protections(protections)
                .correlationRules(rules)
                .timeProvider(timeProvider)
                .build();
    }

    @Test
    public void testMatchOneRule() {
        alert.put(AlertingFields.RULE_NAME.getAlertingName(), "alert1");
        AlertingResult result = engine.evaluate(alert);
        Assert.assertEquals(OK, result.getStatusCode());
        Assert.assertEquals(MATCH, result.getAttributes().getEvaluationResult());
        outEvent = result.getAttributes().getOutputEvents().get(0);
        Assert.assertEquals(1, result.getAttributes().getOutputEvents().size());
        Assert.assertEquals(outEvent, result.getAttributes().getOutputEvents().get(0));
        Assert.assertEquals("siembol_correlation_alerts", outEvent.get("detection_source"));

        Assert.assertTrue(outEvent.containsKey(AlertingFields.MAX_PER_HOUR_FIELD.getCorrelationAlertingName()));
        Assert.assertEquals(1, outEvent.get(AlertingFields.MAX_PER_HOUR_FIELD.getCorrelationAlertingName()));

        Mockito.verify(rule1, times(1)).match(alert);
        Mockito.verify(rule2, times(0)).match(alert);
        Mockito.verify(timeProvider, times(1)).getCurrentTimeInMs();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testMatchOneOverwriteRuleProtection() {
        doAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            ((Map<String, Object>) args[0]).put(AlertingFields.MAX_PER_HOUR_FIELD.getCorrelationAlertingName(), 2);
            return null;
        }).when(rule1).addOutputFieldsToEvent(anyMap());

        alert.put(AlertingFields.RULE_NAME.getAlertingName(), "alert1");
        AlertingResult result = engine.evaluate(alert);
        Assert.assertEquals(OK, result.getStatusCode());
        Assert.assertEquals(MATCH, result.getAttributes().getEvaluationResult());
        outEvent = result.getAttributes().getOutputEvents().get(0);
        Assert.assertEquals(1, result.getAttributes().getOutputEvents().size());
        Assert.assertEquals(outEvent, result.getAttributes().getOutputEvents().get(0));
        Assert.assertEquals("siembol_correlation_alerts", outEvent.get("detection_source"));

        Assert.assertTrue(outEvent.containsKey(AlertingFields.MAX_PER_HOUR_FIELD.getCorrelationAlertingName()));
        Assert.assertEquals(2, outEvent.get(AlertingFields.MAX_PER_HOUR_FIELD.getCorrelationAlertingName()));

        Mockito.verify(rule1, times(1)).match(alert);
        Mockito.verify(rule2, times(0)).match(alert);
        Mockito.verify(timeProvider, times(1)).getCurrentTimeInMs();
    }

    @Test
    public void testMatchBothRules() {
        alert.put(AlertingFields.RULE_NAME.getAlertingName(), "alert2");
        AlertingResult result = engine.evaluate(alert);
        Assert.assertEquals(OK, result.getStatusCode());
        Assert.assertEquals(MATCH, result.getAttributes().getEvaluationResult());
        Assert.assertEquals(2, result.getAttributes().getOutputEvents().size());
        Assert.assertNotNull(result.getAttributes().getOutputEvents().get(0));
        Assert.assertNotNull(result.getAttributes().getOutputEvents().get(1));
        Mockito.verify(rule1, times(1)).match(alert);
        Mockito.verify(rule2, times(1)).match(alert);
        Mockito.verify(timeProvider, times(1)).getCurrentTimeInMs();
    }

    @Test
    public void testMatchFirstRule() {
        when(rule2.match(ArgumentMatchers.any())).thenReturn(AlertingResult.fromEvaluationResult(NO_MATCH, outEvent));
        alert.put(AlertingFields.RULE_NAME.getAlertingName(), "alert2");
        AlertingResult result = engine.evaluate(alert);
        Assert.assertEquals(OK, result.getStatusCode());
        Assert.assertEquals(MATCH, result.getAttributes().getEvaluationResult());
        Assert.assertEquals(1, result.getAttributes().getOutputEvents().size());
        Assert.assertNotNull(result.getAttributes().getOutputEvents().get(0));
        Mockito.verify(rule1, times(1)).match(alert);
        Mockito.verify(rule2, times(1)).match(alert);
        Mockito.verify(timeProvider, times(1)).getCurrentTimeInMs();
    }

    @Test
    public void testMatchFirstAndExceptionSecond() {
        resultRule2 = new AlertingResult(ERROR, resultRule2.getAttributes());
        when(rule2.match(ArgumentMatchers.any())).thenReturn(resultRule2);

        alert.put(AlertingFields.RULE_NAME.getAlertingName(), "alert2");
        AlertingResult result = engine.evaluate(alert);
        Assert.assertEquals(OK, result.getStatusCode());
        Assert.assertEquals(MATCH, result.getAttributes().getEvaluationResult());
        Assert.assertEquals(1, result.getAttributes().getOutputEvents().size());
        Assert.assertNotNull(result.getAttributes().getOutputEvents().get(0));
        Assert.assertEquals(1, result.getAttributes().getExceptionEvents().size());
        Assert.assertNotNull(result.getAttributes().getExceptionEvents().get(0));

        Mockito.verify(rule1, times(1)).match(alert);
        Mockito.verify(rule2, times(1)).match(alert);
        Mockito.verify(timeProvider, times(1)).getCurrentTimeInMs();
    }

    @Test
    public void clean() {
        engine.clean();
        Mockito.verify(rule1, times(1)).clean(currentTime);
        Mockito.verify(rule2, times(1)).clean(currentTime);
        Mockito.verify(timeProvider, times(1)).getCurrentTimeInMs();
    }
}
