package uk.co.gresearch.siembol.common.metrics;

import java.util.concurrent.atomic.AtomicInteger;

public class SiembolGauge {
    private final AtomicInteger value = new AtomicInteger(0);

    public void setValue(int value) {
        this.value.set(value);
    }

    public int getValue() {
        return value.intValue();
    }
}
