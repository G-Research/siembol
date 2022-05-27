package uk.co.gresearch.siembol.common.metrics.spring;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import uk.co.gresearch.siembol.common.metrics.SiembolCounter;
import uk.co.gresearch.siembol.common.metrics.SiembolGauge;
import uk.co.gresearch.siembol.common.metrics.SiembolMetricsRegistrar;

public class SpringMetricsRegistrar implements SiembolMetricsRegistrar {
    private final MeterRegistry registry;

    public SpringMetricsRegistrar(MeterRegistry registry) {
        this.registry = registry;
    }

    @Override
    public SiembolCounter registerCounter(String name) {
        return new SpringCounter(Counter.builder(name).register(registry));
    }

    @Override
    public SiembolGauge registerGauge(String name) {
        var gauge = new SiembolGauge();
        Gauge
                .builder(name, gauge::getValue)
                .register(registry);

        return gauge;
    }
}
