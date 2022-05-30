package uk.co.gresearch.siembol.spark;

import org.apache.commons.lang3.SerializationUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import uk.co.gresearch.siembol.alerts.common.AlertingAttributes;
import uk.co.gresearch.siembol.alerts.common.AlertingResult;
import uk.co.gresearch.siembol.common.model.AlertingSparkTestingResultDto;

import java.util.*;

public class SparkResultTest {
    private AlertingAttributes attributes;
    private AlertingResult alertingResult;
    private AlertingSparkTestingResultDto alertingSparkTestResult;
    private final int maxResult = 100;
    private Map<String, Object> event;

    @Before
    public void setup() {
        event = new HashMap<>();
        attributes = new AlertingAttributes();
        alertingResult = new AlertingResult(AlertingResult.StatusCode.OK, attributes);
    }

    @Test
    public void wrongStatusCodeTest() {
        alertingResult = new AlertingResult(AlertingResult.StatusCode.ERROR, attributes);
        alertingSparkTestResult = new AlertingSparkResult(alertingResult, maxResult).toAlertingSparkTestingResult();

        Assert.assertEquals(1, alertingSparkTestResult.getExceptionsTotal());
        Assert.assertEquals(0, alertingSparkTestResult.getMatchesTotal());
        Assert.assertTrue(alertingSparkTestResult.getMatches().isEmpty());
        Assert.assertTrue(alertingSparkTestResult.getExceptions().isEmpty());
    }


    @Test
    public void singleEventTest() {
        event.put("test", "true");
        attributes.setOutputEvents(List.of(event));
        var sparkResult = new AlertingSparkResult(alertingResult, maxResult);
        alertingSparkTestResult = sparkResult.toAlertingSparkTestingResult();
        Assert.assertEquals(0, alertingSparkTestResult.getExceptionsTotal());
        Assert.assertEquals(1, alertingSparkTestResult.getMatchesTotal());
        Assert.assertTrue(alertingSparkTestResult.getExceptions().isEmpty());
        Assert.assertEquals(1, alertingSparkTestResult.getMatches().size());
        Assert.assertEquals("true", alertingSparkTestResult.getMatches().get(0).get("test"));
        Assert.assertNotNull(sparkResult.toString());
    }


    @Test
    public void maxResultEventTest() {
        event.put("test", "true");
        ArrayList<Map<String, Object>> events = new ArrayList<>();
        for (int i = 0; i <= maxResult; i++) {
            events.add(event);
        }

        attributes.setOutputEvents(events);
        alertingSparkTestResult = new AlertingSparkResult(alertingResult, maxResult).toAlertingSparkTestingResult();
        Assert.assertEquals(0, alertingSparkTestResult.getExceptionsTotal());
        Assert.assertEquals(maxResult + 1, alertingSparkTestResult.getMatchesTotal());
        Assert.assertTrue(alertingSparkTestResult.getExceptions().isEmpty());
        Assert.assertEquals(maxResult, alertingSparkTestResult.getMatches().size());
        Assert.assertEquals("true", alertingSparkTestResult.getMatches().get(0).get("test"));
    }


    @Test
    public void singleExceptionTest() {
        event.put("test", "true");
        attributes.setExceptionEvents(List.of(event));
        var sparkResult = new AlertingSparkResult(alertingResult, maxResult);
        alertingSparkTestResult = sparkResult.toAlertingSparkTestingResult();
        Assert.assertEquals(1, alertingSparkTestResult.getExceptionsTotal());
        Assert.assertEquals(0, alertingSparkTestResult.getMatchesTotal());
        Assert.assertTrue(alertingSparkTestResult.getMatches().isEmpty());
        Assert.assertEquals(1, alertingSparkTestResult.getExceptions().size());
        Assert.assertEquals("true", alertingSparkTestResult.getExceptions().get(0).get("test"));
        Assert.assertNotNull(sparkResult.toString());
    }

    @Test
    public void maxResultExceptionTest() {
        event.put("test", "true");
        ArrayList<Map<String, Object>> events = new ArrayList<>();
        for (int i = 0; i <= maxResult; i++) {
            events.add(event);
        }

        attributes.setExceptionEvents(events);
        alertingSparkTestResult = new AlertingSparkResult(alertingResult, maxResult).toAlertingSparkTestingResult();
        Assert.assertEquals(0, alertingSparkTestResult.getMatchesTotal());
        Assert.assertEquals(maxResult + 1, alertingSparkTestResult.getExceptionsTotal());
        Assert.assertTrue(alertingSparkTestResult.getMatches().isEmpty());
        Assert.assertEquals(maxResult, alertingSparkTestResult.getExceptions().size());
        Assert.assertEquals("true", alertingSparkTestResult.getExceptions().get(0).get("test"));
    }

    @Test
    public void mergeTest() {
        event.put("test", "true");
        ArrayList<Map<String, Object>> events = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            events.add(event);
        }

        attributes.setOutputEvents(events);
        attributes.setExceptionEvents(events);
        var sparkResult = new AlertingSparkResult(alertingResult, maxResult);
        alertingSparkTestResult = sparkResult.toAlertingSparkTestingResult();

        Assert.assertEquals(10, alertingSparkTestResult.getMatchesTotal());
        Assert.assertEquals(10, alertingSparkTestResult.getExceptionsTotal());
        Assert.assertEquals(10, alertingSparkTestResult.getMatches().size());
        Assert.assertEquals(10, alertingSparkTestResult.getExceptions().size());

        event.put("test", "false");
        ArrayList<Map<String, Object>> eventsOther = new ArrayList<>();
        for (int i = 0; i < maxResult + 1; i++) {
            eventsOther.add(event);
        }

        AlertingAttributes attributesOther = new AlertingAttributes();
        attributesOther.setOutputEvents(eventsOther);
        attributesOther.setExceptionEvents(eventsOther);
        var sparkResultOther = new AlertingSparkResult(
                new AlertingResult(AlertingResult.StatusCode.OK, attributesOther), maxResult);
        var alertingSparkTestResultOther = sparkResultOther
                .toAlertingSparkTestingResult();

        Assert.assertEquals(maxResult + 1, alertingSparkTestResultOther.getMatchesTotal());
        Assert.assertEquals(maxResult + 1, alertingSparkTestResultOther.getExceptionsTotal());
        Assert.assertEquals(maxResult, alertingSparkTestResultOther.getMatches().size());
        Assert.assertEquals(maxResult, alertingSparkTestResultOther.getExceptions().size());

        alertingSparkTestResult = sparkResult.merge(sparkResultOther).toAlertingSparkTestingResult();
        Assert.assertEquals(10 + maxResult + 1, alertingSparkTestResult.getMatchesTotal());
        Assert.assertEquals(10 + maxResult + 1, alertingSparkTestResult.getExceptionsTotal());
        Assert.assertEquals(maxResult, alertingSparkTestResult.getMatches().size());
        Assert.assertEquals(maxResult, alertingSparkTestResult.getExceptions().size());
    }


    @Test
    public void serializableTest() {
        event.put("test", "true");
        attributes.setOutputEvents(List.of(event));
        var sparkResult = new AlertingSparkResult(alertingResult, maxResult);

        byte[] blob = SerializationUtils.serialize(sparkResult);
        Assert.assertTrue(blob.length > 0);
        AlertingSparkTestingResultDto cloneTestingResult = SerializationUtils
                .clone(sparkResult).toAlertingSparkTestingResult();

        Assert.assertEquals(0, cloneTestingResult.getExceptionsTotal());
        Assert.assertEquals(1, cloneTestingResult.getMatchesTotal());
        Assert.assertTrue(cloneTestingResult.getExceptions().isEmpty());
        Assert.assertEquals(1, cloneTestingResult.getMatches().size());
        Assert.assertEquals("true", cloneTestingResult.getMatches().get(0).get("test"));
    }

}
