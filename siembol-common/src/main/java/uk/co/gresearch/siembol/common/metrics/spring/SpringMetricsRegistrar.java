package uk.co.gresearch.siembol.common.metrics.spring;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import uk.co.gresearch.siembol.common.metrics.SiembolCounter;
import uk.co.gresearch.siembol.common.metrics.SiembolGauge;
import uk.co.gresearch.siembol.common.metrics.SiembolMetricsRegistrar;
/**
 * An object for registering metrics in Spring
 *
 * <p>This class implements SiembolMetricsRegistrar interface, and it is using in Siembol Spring Boot projects.
 *
 * @author  Marian Novotny
 * @see SiembolMetricsRegistrar
 *
 */
public class SpringMetricsRegistrar implements SiembolMetricsRegistrar {
    private final MeterRegistry registry;

    public SpringMetricsRegistrar(MeterRegistry registry) {
        this.registry = registry;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SiembolCounter registerCounter(String name) {
        return new SpringCounter(Counter.builder(name).register(registry));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SiembolGauge registerGauge(String name) {
        var gauge = new SiembolGauge();
        Gauge
                .builder(name, gauge::getValue)
                .register(registry);

        return gauge;
    }
}
