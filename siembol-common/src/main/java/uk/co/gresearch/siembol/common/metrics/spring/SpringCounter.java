package uk.co.gresearch.siembol.common.metrics.spring;

import io.micrometer.core.instrument.Counter;
import uk.co.gresearch.siembol.common.metrics.SiembolCounter;
/**
 * An object for representing a counter in Spring Boot
 *
 * <p>This class implements SiembolCounter interface.
 * It implements a counter which is used in Siembol Spring Boot projects.
 *
 * @author  Marian Novotny
 * @see SiembolCounter
 *
 */
public class SpringCounter implements SiembolCounter {
    private final Counter counter;

    public SpringCounter(Counter counter) {
        this.counter = counter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void increment() {
        counter.increment();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void increment(int value) {
        counter.increment(value);
    }
}
