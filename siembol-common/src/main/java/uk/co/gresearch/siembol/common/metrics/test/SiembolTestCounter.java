package uk.co.gresearch.siembol.common.metrics.test;

import uk.co.gresearch.siembol.common.metrics.SiembolCounter;

import java.util.concurrent.atomic.AtomicInteger;

public class SiembolTestCounter implements SiembolCounter {
    private final AtomicInteger counter = new AtomicInteger(0);
    @Override
    public void increment() {
        counter.incrementAndGet();
    }

    @Override
    public void increment(int value) {
        counter.addAndGet(value);
    }

    public int getValue() {
        return counter.get();
    }
}
