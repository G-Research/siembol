package uk.co.gresearch.nortem.nikita.correlationengine;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import uk.co.gresearch.nortem.common.utils.TimeProvider;
import uk.co.gresearch.nortem.nikita.common.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;
import static uk.co.gresearch.nortem.nikita.common.EvaluationResult.MATCH;
import static uk.co.gresearch.nortem.nikita.common.EvaluationResult.NO_MATCH;
import static uk.co.gresearch.nortem.nikita.common.NikitaResult.StatusCode.ERROR;
import static uk.co.gresearch.nortem.nikita.common.NikitaResult.StatusCode.OK;

public class NikitaCorrelationEngineTest {
    private TimeProvider timeProvider;
    private Map<String, Object> alert;
    private Map<String, Object> outEvent;
    private List<Pair<String, String>> constants;
    private List<Pair<String, Object>> protections;

    private CorrelationRule rule1;
    private CorrelationRule rule2;
    private List<CorrelationRule> rules;
    private NikitaEngine engine;
    private NikitaResult resultRule1;
    private NikitaResult resultRule2;
    private Long currentTime = 1234L;
    private String correlationKey = "1.2.3.4";

    @Before
    public void setUp() {
        outEvent = new HashMap<>();
        alert = new HashMap<>();
        alert.put(NikitaTags.CORRELATION_KEY_TAG_NAME.toString(), correlationKey);
        constants = Arrays.asList(Pair.of("detection:source", "nikita_correlation"));
        protections = Arrays.asList(Pair.of(NikitaFields.MAX_PER_HOUR_FIELD.getNikitaCorrelationName(),
                Integer.valueOf(1)));
        rule1 = Mockito.mock(CorrelationRule.class);
        rule2 = Mockito.mock(CorrelationRule.class);
        timeProvider = Mockito.mock(TimeProvider.class);

        resultRule1 = NikitaResult.fromEvaluationResult(EvaluationResult.MATCH, outEvent);
        resultRule2 = NikitaResult.fromEvaluationResult(EvaluationResult.MATCH, outEvent);

        when(rule1.getAlertNames()).thenReturn(Arrays.asList("alert1", "alert2"));
        when(rule1.match(any())).thenReturn(resultRule1);


        when(rule2.getAlertNames()).thenReturn(Arrays.asList("alert2"));
        when(rule2.match(any())).thenReturn(resultRule2);

        when(timeProvider.getCurrentTimeInMs()).thenReturn(currentTime);
        doNothing().when(rule1).clean(currentTime);
        doNothing().when(rule2).clean(currentTime);

        rules = Arrays.asList(rule1, rule2);

        engine = new CorrelationEngineImpl.Builder()
                .constants(constants)
                .protections(protections)
                .correlationRules(rules)
                .timeProvider(timeProvider)
                .build();
    }

    @Test
    public void testMatchOneRule() {
        alert.put(NikitaFields.RULE_NAME.getNikitaName(), "alert1");
        NikitaResult result = engine.evaluate(alert);
        Assert.assertEquals(OK, result.getStatusCode());
        Assert.assertEquals(MATCH, result.getAttributes().getEvaluationResult());
        Assert.assertEquals(1, result.getAttributes().getOutputEvents().size());
        Assert.assertEquals(outEvent, result.getAttributes().getOutputEvents().get(0));
        Assert.assertTrue(outEvent.containsKey("detection:source"));
        Assert.assertEquals("nikita_correlation", outEvent.get("detection:source"));

        Assert.assertTrue(outEvent.containsKey(NikitaFields.MAX_PER_HOUR_FIELD.getNikitaCorrelationName()));
        Assert.assertEquals(1, outEvent.get(NikitaFields.MAX_PER_HOUR_FIELD.getNikitaCorrelationName()));

        Mockito.verify(rule1, times(1)).match(alert);
        Mockito.verify(rule2, times(0)).match(alert);
        Mockito.verify(timeProvider, times(1)).getCurrentTimeInMs();
    }

    @Test
    public void testMatchBothRules() {
        alert.put(NikitaFields.RULE_NAME.getNikitaName(), "alert2");
        NikitaResult result = engine.evaluate(alert);
        Assert.assertEquals(OK, result.getStatusCode());
        Assert.assertEquals(MATCH, result.getAttributes().getEvaluationResult());
        Assert.assertEquals(2, result.getAttributes().getOutputEvents().size());
        Assert.assertEquals(outEvent, result.getAttributes().getOutputEvents().get(0));
        Assert.assertEquals(outEvent, result.getAttributes().getOutputEvents().get(1));
        Mockito.verify(rule1, times(1)).match(alert);
        Mockito.verify(rule2, times(1)).match(alert);
        Mockito.verify(timeProvider, times(1)).getCurrentTimeInMs();
    }

    @Test
    public void testMatchFirstRule() {
        when(rule2.match(any())).thenReturn(NikitaResult.fromEvaluationResult(NO_MATCH, outEvent));
        alert.put(NikitaFields.RULE_NAME.getNikitaName(), "alert2");
        NikitaResult result = engine.evaluate(alert);
        Assert.assertEquals(OK, result.getStatusCode());
        Assert.assertEquals(MATCH, result.getAttributes().getEvaluationResult());
        Assert.assertEquals(1, result.getAttributes().getOutputEvents().size());
        Assert.assertEquals(outEvent, result.getAttributes().getOutputEvents().get(0));
        Mockito.verify(rule1, times(1)).match(alert);
        Mockito.verify(rule2, times(1)).match(alert);
        Mockito.verify(timeProvider, times(1)).getCurrentTimeInMs();
    }

    @Test
    public void testMatchFirstAndExceptionSecond() {
        resultRule2 = new NikitaResult(ERROR, resultRule2.getAttributes());
        when(rule2.match(any())).thenReturn(resultRule2);

        alert.put(NikitaFields.RULE_NAME.getNikitaName(), "alert2");
        NikitaResult result = engine.evaluate(alert);
        Assert.assertEquals(OK, result.getStatusCode());
        Assert.assertEquals(MATCH, result.getAttributes().getEvaluationResult());
        Assert.assertEquals(1, result.getAttributes().getOutputEvents().size());
        Assert.assertEquals(outEvent, result.getAttributes().getOutputEvents().get(0));
        Assert.assertEquals(1, result.getAttributes().getExceptionEvents().size());

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
