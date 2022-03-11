package uk.co.gresearch.siembol.common.metrics.spring;

import io.micrometer.core.instrument.Counter;
import uk.co.gresearch.siembol.common.metrics.SiembolCounter;

public class SpringCounter implements SiembolCounter {
    private final Counter counter;

    public SpringCounter(Counter counter) {
        this.counter = counter;
    }

    @Override
    public void increment() {
        counter.increment();
    }

    @Override
    public void increment(int value) {
        counter.increment(value);
    }
}
