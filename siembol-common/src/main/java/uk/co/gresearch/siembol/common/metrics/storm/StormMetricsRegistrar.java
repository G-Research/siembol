package uk.co.gresearch.siembol.common.metrics.storm;

import org.apache.storm.task.TopologyContext;
import uk.co.gresearch.siembol.common.metrics.*;

public class StormMetricsRegistrar implements SiembolMetricsRegistrar {
    private final TopologyContext topologyContext;

    public StormMetricsRegistrar(TopologyContext topologyContext) {
        this.topologyContext = topologyContext;
    }

    @Override
    public SiembolCounter registerCounter(String name) {
        return new StormCounter(topologyContext.registerCounter(name));
    }

    @Override
    public SiembolGauge registerGauge(String name) {
        final var gauge = new SiembolGauge();
        topologyContext.registerGauge(name, () -> gauge.getValue());
        return gauge;
    }
}
