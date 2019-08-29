package uk.co.gresearch.nortem.nikita.protection;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import uk.co.gresearch.nortem.common.utils.TimeProvider;

import static org.mockito.Mockito.when;

public class SimpleCounterTest {
    TimeProvider provider;
    SimpleCounter counter;

    @Before
    public void setUp() {
        provider = Mockito.mock(TimeProvider.class);
        counter = new SimpleCounter(provider);
        when(provider.getDays()).thenReturn(1);
        when(provider.getHour()).thenReturn(1);
    }

    @Test
    public void testNoChange() {
        counter.updateAndIncrement();
        Assert.assertEquals(1, counter.getDailyMatches());
        Assert.assertEquals(1, counter.getHourlyMatches());
    }

    @Test
    public void testDayChange() {
        counter.updateAndIncrement();
        counter.updateAndIncrement();
        Assert.assertEquals(2, counter.getDailyMatches());
        Assert.assertEquals(2, counter.getHourlyMatches());
        when(provider.getDays()).thenReturn(2);
        counter.updateAndIncrement();
        Assert.assertEquals(1, counter.getDailyMatches());
        Assert.assertEquals(1, counter.getHourlyMatches());
    }

    @Test
    public void testHourChange() {
        counter.updateAndIncrement();
        counter.updateAndIncrement();
        Assert.assertEquals(2, counter.getDailyMatches());
        Assert.assertEquals(2, counter.getHourlyMatches());
        when(provider.getHour()).thenReturn(2);
        counter.updateAndIncrement();
        Assert.assertEquals(3, counter.getDailyMatches());
        Assert.assertEquals(1, counter.getHourlyMatches());
    }
}
