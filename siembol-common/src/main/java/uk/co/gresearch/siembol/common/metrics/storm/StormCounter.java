package uk.co.gresearch.siembol.common.metrics.storm;

import com.codahale.metrics.Counter;
import uk.co.gresearch.siembol.common.metrics.SiembolCounter;
/**
 * An object for representing a counter in Storm
 *
 * <p>This class implements SiembolCounter interface is for representing a counter used in Siembol Storm topologies.
 *
 * @author  Marian Novotny
 * @see SiembolCounter
 *
 */
public class StormCounter implements SiembolCounter {
    private final Counter stormCounter;

    public StormCounter(Counter stormCounter) {
        this.stormCounter = stormCounter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void increment() {
        stormCounter.inc();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void increment(int value) {
        stormCounter.inc(value);
    }
}
