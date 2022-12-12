package uk.co.gresearch.siembol.common.metrics;
/**
 * An object for inactive registering metrics
 *
 * <p>This class implements SiembolMetricsRegistrar interface, and it is used for
 * registering dummy metrics implementations.
 * It should be used in scenarios with disabled collecting metrics.
 *
 * @author  Marian Novotny
 * @see SiembolMetricsRegistrar
 *
 */
public class InactiveSiembolMetricsRegistrar implements SiembolMetricsRegistrar {
    @Override
    public SiembolCounter registerCounter(String name) {
        return new SiembolCounter() {
            @Override
            public void increment() {}

            @Override
            public void increment(int value) {}
        };
    }

    @Override
    public SiembolGauge registerGauge(String name) {
        return new SiembolGauge();
    }
}
