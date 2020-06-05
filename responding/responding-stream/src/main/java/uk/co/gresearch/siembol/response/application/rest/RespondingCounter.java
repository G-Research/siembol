package uk.co.gresearch.siembol.response.application.rest;

import io.micrometer.core.instrument.Counter;
import uk.co.gresearch.siembol.response.common.MetricCounter;

public class RespondingCounter implements MetricCounter {

    private final Counter counter;

    public RespondingCounter(Counter counter) {
        this.counter = counter;
    }

    @Override
    public void increment() {
        counter.increment();
    }
}
