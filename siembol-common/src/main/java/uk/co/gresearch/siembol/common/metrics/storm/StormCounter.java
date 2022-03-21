package uk.co.gresearch.siembol.common.metrics.storm;

import com.codahale.metrics.Counter;
import uk.co.gresearch.siembol.common.metrics.SiembolCounter;

public class StormCounter implements SiembolCounter {
    private final Counter stormCounter;

    public StormCounter(Counter stormCounter) {
        this.stormCounter = stormCounter;
    }

    @Override
    public void increment() {
        stormCounter.inc();
    }

    @Override
    public void increment(int value) {
        stormCounter.inc(value);
    }
}
