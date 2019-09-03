package uk.co.gresearch.nortem.spark;

import org.apache.commons.lang3.SerializationUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import uk.co.gresearch.nortem.nikita.common.NikitaAttributes;
import uk.co.gresearch.nortem.nikita.common.NikitaResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;

public class SparkResultTest {
    private NikitaAttributes attributes;
    private NikitaResult nikitaResult;
    private NikitaSparkResult nikitaSparkResult;
    private int maxResult = 100;
    private Map<String, Object> event;

    @Before
    public void setup() {
        event = new HashMap<>();
        attributes = new NikitaAttributes();
        nikitaResult = new NikitaResult(NikitaResult.StatusCode.OK, attributes);
    }

    @Test
    public void wrongStatusCodeTest() throws Exception {
        nikitaResult = new NikitaResult(NikitaResult.StatusCode.ERROR, attributes);
        nikitaSparkResult = new NikitaSparkResult(nikitaResult, maxResult);
        Assert.assertEquals(1, nikitaSparkResult.getExceptionsTotal());
        Assert.assertEquals(0, nikitaSparkResult.getMatchesTotal());
        Assert.assertTrue(nikitaSparkResult.getMatches().isEmpty());
        Assert.assertEquals(1, nikitaSparkResult.getExceptions().size());
        Assert.assertEquals("Status code: ERROR", nikitaSparkResult.getExceptions().get(0));
    }

    @Test
    public void singleEventTest() throws Exception {
        event.put("test", "true");
        attributes.setOutputEvents(Arrays.asList(event));
        nikitaSparkResult = new NikitaSparkResult(nikitaResult, maxResult);
        Assert.assertEquals(0, nikitaSparkResult.getExceptionsTotal());
        Assert.assertEquals(1, nikitaSparkResult.getMatchesTotal());
        Assert.assertTrue(nikitaSparkResult.getExceptions().isEmpty());
        Assert.assertEquals(1, nikitaSparkResult.getMatches().size());
        Assert.assertEquals("{\"test\":\"true\"}", nikitaSparkResult.getMatches().get(0));
    }

    @Test
    public void maxResultEventTest() throws Exception {
        event.put("test", "true");
        ArrayList<Map<String, Object>> events = new ArrayList<>();
        for(int i = 0; i <= maxResult; i++) {
            events.add(event);
        }

        attributes.setOutputEvents(events);
        nikitaSparkResult = new NikitaSparkResult(nikitaResult, maxResult);
        Assert.assertEquals(0, nikitaSparkResult.getExceptionsTotal());
        Assert.assertEquals(maxResult + 1, nikitaSparkResult.getMatchesTotal());
        Assert.assertTrue(nikitaSparkResult.getExceptions().isEmpty());
        Assert.assertEquals(maxResult, nikitaSparkResult.getMatches().size());
        Assert.assertEquals("{\"test\":\"true\"}", nikitaSparkResult.getMatches().get(0));
    }

    @Test
    public void singleExceptionTest() throws Exception {
        event.put("test", "true");
        attributes.setExceptionEvents(Arrays.asList(event));
        nikitaSparkResult = new NikitaSparkResult(nikitaResult, maxResult);
        Assert.assertEquals(1, nikitaSparkResult.getExceptionsTotal());
        Assert.assertEquals(0, nikitaSparkResult.getMatchesTotal());
        Assert.assertTrue(nikitaSparkResult.getMatches().isEmpty());
        Assert.assertEquals(1, nikitaSparkResult.getExceptions().size());
        Assert.assertEquals("{\"test\":\"true\"}", nikitaSparkResult.getExceptions().get(0));
    }

    @Test
    public void maxResultExceptionTest() throws Exception {
        event.put("test", "true");
        ArrayList<Map<String, Object>> events = new ArrayList<>();
        for(int i = 0; i <= maxResult; i++) {
            events.add(event);
        }

        attributes.setExceptionEvents(events);
        nikitaSparkResult = new NikitaSparkResult(nikitaResult, maxResult);
        Assert.assertEquals(0, nikitaSparkResult.getMatchesTotal());
        Assert.assertEquals(maxResult + 1, nikitaSparkResult.getExceptionsTotal());
        Assert.assertTrue(nikitaSparkResult.getMatches().isEmpty());
        Assert.assertEquals(maxResult, nikitaSparkResult.getExceptions().size());
        Assert.assertEquals("{\"test\":\"true\"}", nikitaSparkResult.getExceptions().get(0));
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
        nikitaSparkResult = new NikitaSparkResult(nikitaResult, maxResult);

        Assert.assertEquals(10, nikitaSparkResult.getMatchesTotal());
        Assert.assertEquals(10, nikitaSparkResult.getExceptionsTotal());
        Assert.assertEquals(10, nikitaSparkResult.getMatches().size());
        Assert.assertEquals(10, nikitaSparkResult.getExceptions().size());

        event.put("test", "false");
        ArrayList<Map<String, Object>> eventsOther = new ArrayList<>();
        for(int i = 0; i < maxResult + 1; i++) {
            eventsOther.add(event);
        }

        NikitaAttributes attributesOther = new NikitaAttributes();
        attributesOther.setOutputEvents(eventsOther);
        attributesOther.setExceptionEvents(eventsOther);
        NikitaSparkResult nikitaSparkResultOther = new NikitaSparkResult(
                new NikitaResult(NikitaResult.StatusCode.OK, attributesOther),maxResult);

        Assert.assertEquals(maxResult + 1, nikitaSparkResultOther.getMatchesTotal());
        Assert.assertEquals(maxResult + 1, nikitaSparkResultOther.getExceptionsTotal());
        Assert.assertEquals(maxResult, nikitaSparkResultOther.getMatches().size());
        Assert.assertEquals(maxResult, nikitaSparkResultOther.getExceptions().size());

        nikitaSparkResult = nikitaSparkResult.merge(nikitaSparkResultOther);
        Assert.assertEquals(10 + maxResult + 1, nikitaSparkResult.getMatchesTotal());
        Assert.assertEquals(10 + maxResult + 1, nikitaSparkResult.getExceptionsTotal());
        Assert.assertEquals(maxResult, nikitaSparkResult.getMatches().size());
        Assert.assertEquals(maxResult, nikitaSparkResult.getExceptions().size());
    }

    @Test
    public void serializableTest() throws Exception {
        event.put("test", "true");
        attributes.setOutputEvents(Arrays.asList(event));
        nikitaSparkResult = new NikitaSparkResult(nikitaResult, maxResult);

        byte[] blob = SerializationUtils.serialize(nikitaSparkResult);
        Assert.assertTrue(blob.length > 0);
        NikitaSparkResult clone = SerializationUtils.clone(nikitaSparkResult);


        Assert.assertEquals(0, clone.getExceptionsTotal());
        Assert.assertEquals(1, clone.getMatchesTotal());
        Assert.assertTrue(clone.getExceptions().isEmpty());
        Assert.assertEquals(1, clone.getMatches().size());
        Assert.assertEquals("{\"test\":\"true\"}", clone.getMatches().get(0));
    }
}
