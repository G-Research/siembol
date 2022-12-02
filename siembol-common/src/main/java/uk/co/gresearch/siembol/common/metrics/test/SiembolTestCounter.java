package uk.co.gresearch.siembol.common.metrics.test;

import uk.co.gresearch.siembol.common.metrics.SiembolCounter;

import java.util.concurrent.atomic.AtomicInteger;
/**
 * An object for representing a counter in testing
 *
 * <p>This class implements SiembolCounter interface is for representing a counter used in Siembol unit tests.
 *
 * @author  Marian Novotny
 * @see SiembolCounter
 *
 */
public class SiembolTestCounter implements SiembolCounter {
    private final AtomicInteger counter = new AtomicInteger(0);
    /**
     * {@inheritDoc}
     */
    @Override
    public void increment() {
        counter.incrementAndGet();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void increment(int value) {
        counter.addAndGet(value);
    }

    public int getValue() {
        return counter.get();
    }
}
