package uk.co.gresearch.siembol.alerts.correlationengine;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;

public class AlertCounterTest {
    private final int threshold = 1000;
    private AlertCounterMetadata counterMetadata;
    private AlertCounter alertCounter;

    private List<String> fieldNames;

    @Before
    public void setUp() {
        fieldNames = Arrays.asList("constant", "variable");
        counterMetadata = new AlertCounterMetadata("dummy",
                threshold, 1000, EnumSet.noneOf(AlertCounterMetadata.Flags.class));
        alertCounter = new AlertCounter(counterMetadata);
    }

    @Test
    public void reachingMaxSize() {
        for (long i = 1; i <= counterMetadata.getThreshold(); i++) {
            alertCounter.update(new AlertContext(i, new Object[]{"secret", i}));
            Assert.assertEquals(1, alertCounter.getOldest().longValue());
            Assert.assertEquals(i, alertCounter.getSize());
        }

        alertCounter.update(new AlertContext(8, new Object[]{"secret", 1001}));
        Assert.assertEquals(2, alertCounter.getOldest().longValue());
        Assert.assertEquals(counterMetadata.getThreshold(), alertCounter.getSize());
        var fieldsToSend = alertCounter.getCorrelatedAlerts(fieldNames);
        Assert.assertFalse(fieldsToSend.isEmpty());
        Assert.assertEquals(1000, fieldsToSend.size());
        var variableSet = new HashSet<Long>();
        for (var stringObjectMap : fieldsToSend) {
            Assert.assertEquals(2, stringObjectMap.size());
            Assert.assertTrue(stringObjectMap.get("constant") instanceof String);
            Assert.assertTrue(stringObjectMap.get("variable") instanceof Number);
            Assert.assertEquals("secret", stringObjectMap.get("constant"));
            variableSet.add(((Number) stringObjectMap.get("variable")).longValue());
        }
        Assert.assertEquals(1000, variableSet.size());
    }


    @Test
    public void cleaningCounterPartially() {
        for (long i = 1; i <= counterMetadata.getThreshold(); i++) {
            alertCounter.update(new AlertContext(i, new Object[]{"secret", i}));
        }

        Assert.assertEquals(1, alertCounter.getOldest().longValue());
        Assert.assertEquals(counterMetadata.getThreshold(), alertCounter.getSize());
        Assert.assertFalse(alertCounter.isEmpty());

        alertCounter.clean(50);
        Assert.assertEquals(50, alertCounter.getOldest().longValue());
        Assert.assertEquals(counterMetadata.getThreshold() - 49, alertCounter.getSize());
    }

    @Test
    public void cleaningCounterComplete() {
        alertCounter.update(new AlertContext(1, new Object[]{"secret", 1}));
        Assert.assertEquals(1, alertCounter.getOldest().longValue());
        Assert.assertEquals(1, alertCounter.getSize());
        Assert.assertFalse(alertCounter.isEmpty());

        alertCounter.clean(2);
        Assert.assertNull(alertCounter.getOldest());
        Assert.assertEquals(0, alertCounter.getSize());
        Assert.assertTrue(alertCounter.isEmpty());
    }

    @Test
    public void cleaningCounterVeryOld() {
        alertCounter.update(new AlertContext(1, new Object[]{"secret", 1}));
        alertCounter.clean(1002);
        Assert.assertNull(alertCounter.getOldest());
        Assert.assertEquals(0, alertCounter.getSize());
        Assert.assertTrue(alertCounter.isEmpty());
    }

    @Test
    public void getFieldsToSendWithNullFields() {
        alertCounter.update(new AlertContext(1, new Object[]{"secret", 1}));
        alertCounter.update(new AlertContext(2, new Object[]{null, 2}));
        alertCounter.update(new AlertContext(3, new Object[]{null, null}));

        Assert.assertEquals(3, alertCounter.getSize());

        var fieldsToSend = alertCounter.getCorrelatedAlerts(fieldNames);
        Assert.assertEquals(3, fieldsToSend.size());

        for (var stringObjectMap : fieldsToSend) {
            if (stringObjectMap.get("variable") == null) {
                Assert.assertTrue(stringObjectMap.isEmpty());
                continue;
            }

            Assert.assertTrue(stringObjectMap.get("variable") instanceof Number);
            if (((Number) stringObjectMap.get("variable")).longValue() == 2) {
                Assert.assertEquals(1, stringObjectMap.size());
            } else {
                Assert.assertEquals(2, stringObjectMap.size());
                Assert.assertTrue(stringObjectMap.get("constant") instanceof String);
                Assert.assertTrue(stringObjectMap.get("variable") instanceof Number);
                Assert.assertEquals("secret", stringObjectMap.get("constant"));
                Assert.assertEquals(1, stringObjectMap.get("variable"));
            }
        }
    }
}
