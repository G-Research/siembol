package uk.co.gresearch.siembol.common.metrics.storm;

import org.apache.storm.task.TopologyContext;
import uk.co.gresearch.siembol.common.metrics.SiembolMetricsRegistrar;

import java.io.Serializable;

public class StormMetricsRegistrarFactoryImpl implements StormMetricsRegistrarFactory, Serializable {
    private static final long serialVersionUID = 1L;
    @Override
    public SiembolMetricsRegistrar createSiembolMetricsRegistrar(TopologyContext topologyContext) {
        return new StormMetricsRegistrar(topologyContext).cachedRegistrar();
    }
}
