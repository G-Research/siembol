package uk.co.gresearch.siembol.common.metrics;

import java.util.HashMap;
import java.util.Map;
/**
 * An object for registering metrics with caching
 *
 * <p>This class implements SiembolMetricsRegistrar interface and it is using a map for caching of metrics.
 * This class is not thread safe and the caller needs to consider it.
 *
 * @author  Marian Novotny
 * @see SiembolMetricsRegistrar
 *
 */
public class SiembolMetricsCachedRegistrar implements SiembolMetricsRegistrar {
    private final Map<String, SiembolCounter> countersCache = new HashMap<>();
    private final Map<String, SiembolGauge> gaugeCache = new HashMap<>();
    private final SiembolMetricsRegistrar registrar;

    public SiembolMetricsCachedRegistrar(SiembolMetricsRegistrar registrar) {
        this.registrar = registrar;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SiembolCounter registerCounter(String name) {
        if (countersCache.containsKey(name)) {
            return countersCache.get(name);
        }
        var counter = registrar.registerCounter(name);
        countersCache.put(name, counter);
        return counter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SiembolGauge registerGauge(String name) {
        if (gaugeCache.containsKey(name)) {
            return gaugeCache.get(name);
        }
        var gauge = registrar.registerGauge(name);
        gaugeCache.put(name, gauge);
        return gauge;

    }
}
