package uk.co.gresearch.siembol.alerts.common;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;
import static uk.co.gresearch.siembol.alerts.common.AlertingEngineType.SIEMBOL_ALERTS;
import static uk.co.gresearch.siembol.alerts.common.AlertingResult.StatusCode.ERROR;
import static uk.co.gresearch.siembol.alerts.common.AlertingResult.StatusCode.OK;

public class CompositeAlertingEngineTest {
    private CompositeAlertingEngine compositeEngine;
    private AlertingEngine engine1;
    private AlertingEngine engine2;
    private AlertingResult resultEngine1;
    private AlertingResult resultEngine2;
    private final Map<String, Object> event = new HashMap<>();

    @Before
    public void setUp() {
        engine1 = Mockito.mock(AlertingEngine.class);
        engine2 = Mockito.mock(AlertingEngine.class);
        compositeEngine = new CompositeAlertingEngine(Arrays.asList(engine1, engine2));
        resultEngine1 = new AlertingResult(OK, new AlertingAttributes());
        resultEngine2 = new AlertingResult(OK, new AlertingAttributes());
        when(engine1.evaluate(event)).thenReturn(resultEngine1);
        when(engine2.evaluate(event)).thenReturn(resultEngine2);
        when(engine1.getAlertingEngineType()).thenReturn(SIEMBOL_ALERTS);
    }

    @Test
    public void testOkNoMatch() {
        AlertingResult result = compositeEngine.evaluate(event);
        Assert.assertEquals(OK, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes());
        Assert.assertEquals(EvaluationResult.NO_MATCH, result.getAttributes().getEvaluationResult());
        Assert.assertNull(result.getAttributes().getOutputEvents());
        Assert.assertNull(result.getAttributes().getExceptionEvents());
    }

    @Test
    public void testErrorFirstMatch() {
        when(engine1.evaluate(event)).thenReturn(AlertingResult.fromException(new IllegalStateException()));
        AlertingResult result = compositeEngine.evaluate(event);
        Assert.assertEquals(ERROR, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes());
        Assert.assertNull(result.getAttributes().getOutputEvents());
        Assert.assertNull(result.getAttributes().getExceptionEvents());
    }

    @Test
    public void testErrorSecondMatch() {
        when(engine2.evaluate(event)).thenReturn(AlertingResult.fromException(new IllegalStateException()));
        AlertingResult result = compositeEngine.evaluate(event);
        Assert.assertEquals(ERROR, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes());
        Assert.assertNull(result.getAttributes().getOutputEvents());
        Assert.assertNull(result.getAttributes().getExceptionEvents());
    }

    @Test
    public void testOkFirstMatch() {
        resultEngine1.getAttributes().setOutputEvents(Arrays.asList(new HashMap<>(), new HashMap<>()));
        AlertingResult result = compositeEngine.evaluate(event);
        Assert.assertEquals(OK, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes());
        Assert.assertEquals(EvaluationResult.MATCH, result.getAttributes().getEvaluationResult());
        Assert.assertNotNull(result.getAttributes().getOutputEvents());
        Assert.assertEquals(2, result.getAttributes().getOutputEvents().size());
        Assert.assertNull(result.getAttributes().getExceptionEvents());
    }

    @Test
    public void testOkSecondMatch() {
        resultEngine2.getAttributes().setOutputEvents(Arrays.asList(new HashMap<>(), new HashMap<>()));
        AlertingResult result = compositeEngine.evaluate(event);
        Assert.assertEquals(OK, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes());
        Assert.assertEquals(EvaluationResult.MATCH, result.getAttributes().getEvaluationResult());
        Assert.assertNotNull(result.getAttributes().getOutputEvents());
        Assert.assertEquals(2, result.getAttributes().getOutputEvents().size());
        Assert.assertNull(result.getAttributes().getExceptionEvents());
    }

    @Test
    public void testOkBothMatch() {
        resultEngine1.getAttributes().setOutputEvents(Arrays.asList(new HashMap<>(), new HashMap<>()));
        resultEngine2.getAttributes().setOutputEvents(Collections.singletonList(new HashMap<>()));
        AlertingResult result = compositeEngine.evaluate(event);
        Assert.assertEquals(OK, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes());
        Assert.assertEquals(EvaluationResult.MATCH, result.getAttributes().getEvaluationResult());
        Assert.assertNotNull(result.getAttributes().getOutputEvents());
        Assert.assertEquals(3, result.getAttributes().getOutputEvents().size());
        Assert.assertNull(result.getAttributes().getExceptionEvents());
    }

    @Test
    public void testOkMatchWithExceptions() {
        resultEngine1.getAttributes().setExceptionEvents(Arrays.asList(new HashMap<>(), new HashMap<>()));
        resultEngine2.getAttributes().setOutputEvents(Collections.singletonList(new HashMap<>()));
        AlertingResult result = compositeEngine.evaluate(event);
        Assert.assertEquals(OK, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes());
        Assert.assertEquals(EvaluationResult.MATCH, result.getAttributes().getEvaluationResult());
        Assert.assertNotNull(result.getAttributes().getOutputEvents());
        Assert.assertEquals(1, result.getAttributes().getOutputEvents().size());
        Assert.assertNotNull(result.getAttributes().getExceptionEvents());
        Assert.assertEquals(2, result.getAttributes().getExceptionEvents().size());
    }

    @Test
    public void testGetEngineType() {
        Assert.assertEquals(SIEMBOL_ALERTS, compositeEngine.getAlertingEngineType());
        verify(engine1, times(1)).getAlertingEngineType();
    }
}
