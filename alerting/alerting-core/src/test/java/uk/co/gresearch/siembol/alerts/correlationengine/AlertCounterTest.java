package uk.co.gresearch.siembol.alerts.correlationengine;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.EnumSet;

public class AlertCounterTest {
    private int threshold = 1000;
    private AlertCounterMetadata counterMetadata;
    private AlertCounter alertCounter;

    @Before
    public void setUp() {
        counterMetadata = new AlertCounterMetadata("dummy",
                threshold, 1000, EnumSet.noneOf(AlertCounterMetadata.Flags.class));
        alertCounter = new AlertCounter(counterMetadata);
    }

    @Test
    public void reachingMaxSize() {
        for (long i = 1; i <= counterMetadata.getThreshold(); i++) {
            alertCounter.update(i);
            Assert.assertEquals(1, alertCounter.getOldest().longValue());
            Assert.assertEquals(i, alertCounter.getSize());
        }
        alertCounter.update(8);
        Assert.assertEquals(2, alertCounter.getOldest().longValue());
        Assert.assertEquals(counterMetadata.getThreshold(), alertCounter.getSize());
    }

    @Test
    public void cleaningCounterPartially() {
        for (long i = 1; i <= counterMetadata.getThreshold(); i++) {
            alertCounter.update(i);
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
        alertCounter.update(1);
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
        alertCounter.update(1);
        alertCounter.clean(1002);
        Assert.assertNull(alertCounter.getOldest());
        Assert.assertEquals(0, alertCounter.getSize());
        Assert.assertTrue(alertCounter.isEmpty());
    }

}
