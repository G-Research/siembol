package uk.co.gresearch.siembol.common.metrics;

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
