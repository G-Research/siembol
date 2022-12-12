package uk.co.gresearch.siembol.common.metrics.storm;

import org.apache.storm.task.TopologyContext;
import uk.co.gresearch.siembol.common.metrics.*;
/**
 * An object for registering metrics in Storm
 *
 * <p>This class implements SiembolMetricsRegistrar interface, and it is used in Siembol Storm topologies.
 *
 * @author  Marian Novotny
 * @see SiembolMetricsRegistrar
 *
 */
public class StormMetricsRegistrar implements SiembolMetricsRegistrar {
    private final TopologyContext topologyContext;

    public StormMetricsRegistrar(TopologyContext topologyContext) {
        this.topologyContext = topologyContext;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SiembolCounter registerCounter(String name) {
        return new StormCounter(topologyContext.registerCounter(name));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SiembolGauge registerGauge(String name) {
        final var gauge = new SiembolGauge();
        topologyContext.registerGauge(name, () -> gauge.getValue());
        return gauge;
    }
}
