package uk.co.gresearch.siembol.response.stream.rest;

import io.micrometer.core.instrument.Counter;
import uk.co.gresearch.siembol.response.common.MetricCounter;

public class ResponseCounter implements MetricCounter {

    private final Counter counter;

    public ResponseCounter(Counter counter) {
        this.counter = counter;
    }

    @Override
    public void increment() {
        counter.increment();
    }
}
