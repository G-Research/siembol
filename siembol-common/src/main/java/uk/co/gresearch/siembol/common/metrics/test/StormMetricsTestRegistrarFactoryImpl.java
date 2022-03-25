package uk.co.gresearch.siembol.common.metrics.test;

import org.apache.storm.task.TopologyContext;
import uk.co.gresearch.siembol.common.metrics.SiembolMetricsRegistrar;
import uk.co.gresearch.siembol.common.metrics.storm.StormMetricsRegistrarFactory;

public class StormMetricsTestRegistrarFactoryImpl implements StormMetricsRegistrarFactory {
    private final SiembolMetricsTestRegistrar metricsRegistrar;
    private final SiembolMetricsRegistrar cachedRegistrar;

    public StormMetricsTestRegistrarFactoryImpl() {
        metricsRegistrar = new SiembolMetricsTestRegistrar();
        cachedRegistrar = metricsRegistrar.cachedRegistrar();
    }

    @Override
    public SiembolMetricsRegistrar createSiembolMetricsRegistrar(TopologyContext topologyContext) {
        return cachedRegistrar;
    }

    public int getCounterValue(String name) {
        return metricsRegistrar.getCounterValue(name);
    }
}
