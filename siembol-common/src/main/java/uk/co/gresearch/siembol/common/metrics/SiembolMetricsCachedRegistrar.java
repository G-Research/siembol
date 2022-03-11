package uk.co.gresearch.siembol.common.metrics;

import java.util.HashMap;
import java.util.Map;

public class SiembolMetricsCachedRegistrar implements SiembolMetricsRegistrar {
    private final Map<String, SiembolCounter> countersCache = new HashMap<>();
    private final Map<String, SiembolGauge> gaugeCache = new HashMap<>();
    private final SiembolMetricsRegistrar factory;

    public SiembolMetricsCachedRegistrar(SiembolMetricsRegistrar factory) {
        this.factory = factory;
    }

    @Override
    public SiembolCounter registerCounter(String name) {
        if (countersCache.containsKey(name)) {
            return countersCache.get(name);
        }
        var counter = factory.registerCounter(name);
        countersCache.put(name, counter);
        return counter;
    }

    @Override
    public SiembolGauge registerGauge(String name) {
        if (gaugeCache.containsKey(name)) {
            return gaugeCache.get(name);
        }
        var gauge = factory.registerGauge(name);
        gaugeCache.put(name, gauge);
        return gauge;

    }
}
