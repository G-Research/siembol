package uk.co.gresearch.siembol.common.metrics.test;

import uk.co.gresearch.siembol.common.metrics.SiembolCounter;
import uk.co.gresearch.siembol.common.metrics.SiembolGauge;
import uk.co.gresearch.siembol.common.metrics.SiembolMetricsRegistrar;

import java.util.HashMap;
import java.util.Map;
/**
 * An object for registering metrics in unit tests
 *
 * <p>This class implements SiembolMetricsRegistrar interface and it is using in unit tests.
 *
 * @author  Marian Novotny
 * @see SiembolMetricsRegistrar
 *
 */
public class SiembolMetricsTestRegistrar implements SiembolMetricsRegistrar {
    private final Map<String, SiembolTestCounter> countersMap = new HashMap<>();
    private final Map<String, SiembolGauge> gaugesMap = new HashMap<>();
    private static final String METRIC_ALREADY_EXISTS_MSG = "Metric %s already exists";
    private static final String METRIC_DOES_NOT_EXIST_MSG = "Metric %s does not exist";

    /**
     * {@inheritDoc}
     */
    @Override
    public SiembolCounter registerCounter(String name) {
        if (countersMap.containsKey(name)) {
            throw new IllegalArgumentException(String.format(METRIC_ALREADY_EXISTS_MSG, name));
        }
        var counter = new SiembolTestCounter();
        countersMap.put(name, counter);
        return counter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SiembolGauge registerGauge(String name) {
        if (countersMap.containsKey(name)) {
            throw new IllegalArgumentException(String.format(METRIC_ALREADY_EXISTS_MSG, name));
        }
        var gauge = new SiembolGauge();
        gaugesMap.put(name, gauge);
        return gauge;
    }

    public int getCounterValue(String name) {
        if (!countersMap.containsKey(name)) {
            throw new IllegalArgumentException(String.format(METRIC_DOES_NOT_EXIST_MSG, name));
        }
        return countersMap.get(name).getValue();
    }

    public double getGaugeValue(String name) {
        if (!gaugesMap.containsKey(name)) {
            throw new IllegalArgumentException(String.format(METRIC_DOES_NOT_EXIST_MSG, name));
        }
        return gaugesMap.get(name).getValue();
    }
}
