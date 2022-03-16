package uk.co.gresearch.siembol.common.metrics.test;

import uk.co.gresearch.siembol.common.metrics.SiembolCounter;
import uk.co.gresearch.siembol.common.metrics.SiembolGauge;
import uk.co.gresearch.siembol.common.metrics.SiembolMetricsRegistrar;

import java.util.HashMap;
import java.util.Map;

public class SiembolMetricsTestRegistrar implements SiembolMetricsRegistrar {
    private final Map<String, SiembolTestCounter> countersMap = new HashMap<>();
    private final Map<String, SiembolGauge> gaugesMap = new HashMap<>();
    private static final String METRIC_ALREADY_EXISTS = "Metrics %s already exists with the name";
    @Override
    public SiembolCounter registerCounter(String name) {
        if (countersMap.containsKey(name)) {
            throw new IllegalArgumentException(String.format(METRIC_ALREADY_EXISTS, name));
        }
        var counter = new SiembolTestCounter();
        countersMap.put(name, counter);
        return counter;
    }

    @Override
    public SiembolGauge registerGauge(String name) {
        if (countersMap.containsKey(name)) {
            throw new IllegalArgumentException(String.format(METRIC_ALREADY_EXISTS, name));
        }
        var gauge = new SiembolGauge();
        gaugesMap.put(name, gauge);
        return gauge;
    }

    public int getCounterValue(String name) {
        if (!countersMap.containsKey(name)) {
            throw new IllegalArgumentException("Metrics with name %s does not exist");
        }
        return countersMap.get(name).getValue();
    }
}
