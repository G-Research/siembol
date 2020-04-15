package uk.co.gresearch.siembol.spark;

import org.apache.commons.lang3.SerializationUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import uk.co.gresearch.siembol.alerts.common.AlertingAttributes;
import uk.co.gresearch.siembol.alerts.common.AlertingResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;

public class SparkResultTest {
    private AlertingAttributes attributes;
    private AlertingResult alertingResult;
    private AlertingSparkResult alertingSparkResult;
    private int maxResult = 100;
    private Map<String, Object> event;

    @Before
    public void setup() {
        event = new HashMap<>();
        attributes = new AlertingAttributes();
        alertingResult = new AlertingResult(AlertingResult.StatusCode.OK, attributes);
    }

    @Test
    public void wrongStatusCodeTest() throws Exception {
        alertingResult = new AlertingResult(AlertingResult.StatusCode.ERROR, attributes);
        alertingSparkResult = new AlertingSparkResult(alertingResult, maxResult);
        Assert.assertEquals(1, alertingSparkResult.getExceptionsTotal());
        Assert.assertEquals(0, alertingSparkResult.getMatchesTotal());
        Assert.assertTrue(alertingSparkResult.getMatches().isEmpty());
        Assert.assertEquals(1, alertingSparkResult.getExceptions().size());
        Assert.assertEquals("Status code: ERROR", alertingSparkResult.getExceptions().get(0));
    }

    @Test
    public void singleEventTest() throws Exception {
        event.put("test", "true");
        attributes.setOutputEvents(Arrays.asList(event));
        alertingSparkResult = new AlertingSparkResult(alertingResult, maxResult);
        Assert.assertEquals(0, alertingSparkResult.getExceptionsTotal());
        Assert.assertEquals(1, alertingSparkResult.getMatchesTotal());
        Assert.assertTrue(alertingSparkResult.getExceptions().isEmpty());
        Assert.assertEquals(1, alertingSparkResult.getMatches().size());
        Assert.assertEquals("{\"test\":\"true\"}", alertingSparkResult.getMatches().get(0));
    }

    @Test
    public void maxResultEventTest() throws Exception {
        event.put("test", "true");
        ArrayList<Map<String, Object>> events = new ArrayList<>();
        for(int i = 0; i <= maxResult; i++) {
            events.add(event);
        }

        attributes.setOutputEvents(events);
        alertingSparkResult = new AlertingSparkResult(alertingResult, maxResult);
        Assert.assertEquals(0, alertingSparkResult.getExceptionsTotal());
        Assert.assertEquals(maxResult + 1, alertingSparkResult.getMatchesTotal());
        Assert.assertTrue(alertingSparkResult.getExceptions().isEmpty());
        Assert.assertEquals(maxResult, alertingSparkResult.getMatches().size());
        Assert.assertEquals("{\"test\":\"true\"}", alertingSparkResult.getMatches().get(0));
    }

    @Test
    public void singleExceptionTest() throws Exception {
        event.put("test", "true");
        attributes.setExceptionEvents(Arrays.asList(event));
        alertingSparkResult = new AlertingSparkResult(alertingResult, maxResult);
        Assert.assertEquals(1, alertingSparkResult.getExceptionsTotal());
        Assert.assertEquals(0, alertingSparkResult.getMatchesTotal());
        Assert.assertTrue(alertingSparkResult.getMatches().isEmpty());
        Assert.assertEquals(1, alertingSparkResult.getExceptions().size());
        Assert.assertEquals("{\"test\":\"true\"}", alertingSparkResult.getExceptions().get(0));
    }

    @Test
    public void maxResultExceptionTest() throws Exception {
        event.put("test", "true");
        ArrayList<Map<String, Object>> events = new ArrayList<>();
        for(int i = 0; i <= maxResult; i++) {
            events.add(event);
        }

        attributes.setExceptionEvents(events);
        alertingSparkResult = new AlertingSparkResult(alertingResult, maxResult);
        Assert.assertEquals(0, alertingSparkResult.getMatchesTotal());
        Assert.assertEquals(maxResult + 1, alertingSparkResult.getExceptionsTotal());
        Assert.assertTrue(alertingSparkResult.getMatches().isEmpty());
        Assert.assertEquals(maxResult, alertingSparkResult.getExceptions().size());
        Assert.assertEquals("{\"test\":\"true\"}", alertingSparkResult.getExceptions().get(0));
    }

    @Test
    public void mergeTest() throws Exception {
        event.put("test", "true");
        ArrayList<Map<String, Object>> events = new ArrayList<>();
        for(int i = 0; i < 10; i++) {
            events.add(event);
        }

        attributes.setOutputEvents(events);
        attributes.setExceptionEvents(events);
        alertingSparkResult = new AlertingSparkResult(alertingResult, maxResult);

        Assert.assertEquals(10, alertingSparkResult.getMatchesTotal());
        Assert.assertEquals(10, alertingSparkResult.getExceptionsTotal());
        Assert.assertEquals(10, alertingSparkResult.getMatches().size());
        Assert.assertEquals(10, alertingSparkResult.getExceptions().size());

        event.put("test", "false");
        ArrayList<Map<String, Object>> eventsOther = new ArrayList<>();
        for(int i = 0; i < maxResult + 1; i++) {
            eventsOther.add(event);
        }

        AlertingAttributes attributesOther = new AlertingAttributes();
        attributesOther.setOutputEvents(eventsOther);
        attributesOther.setExceptionEvents(eventsOther);
        AlertingSparkResult alertingSparkResultOther = new AlertingSparkResult(
                new AlertingResult(AlertingResult.StatusCode.OK, attributesOther),maxResult);

        Assert.assertEquals(maxResult + 1, alertingSparkResultOther.getMatchesTotal());
        Assert.assertEquals(maxResult + 1, alertingSparkResultOther.getExceptionsTotal());
        Assert.assertEquals(maxResult, alertingSparkResultOther.getMatches().size());
        Assert.assertEquals(maxResult, alertingSparkResultOther.getExceptions().size());

        alertingSparkResult = alertingSparkResult.merge(alertingSparkResultOther);
        Assert.assertEquals(10 + maxResult + 1, alertingSparkResult.getMatchesTotal());
        Assert.assertEquals(10 + maxResult + 1, alertingSparkResult.getExceptionsTotal());
        Assert.assertEquals(maxResult, alertingSparkResult.getMatches().size());
        Assert.assertEquals(maxResult, alertingSparkResult.getExceptions().size());
    }

    @Test
    public void serializableTest() throws Exception {
        event.put("test", "true");
        attributes.setOutputEvents(Arrays.asList(event));
        alertingSparkResult = new AlertingSparkResult(alertingResult, maxResult);

        byte[] blob = SerializationUtils.serialize(alertingSparkResult);
        Assert.assertTrue(blob.length > 0);
        AlertingSparkResult clone = SerializationUtils.clone(alertingSparkResult);


        Assert.assertEquals(0, clone.getExceptionsTotal());
        Assert.assertEquals(1, clone.getMatchesTotal());
        Assert.assertTrue(clone.getExceptions().isEmpty());
        Assert.assertEquals(1, clone.getMatches().size());
        Assert.assertEquals("{\"test\":\"true\"}", clone.getMatches().get(0));
    }
}
