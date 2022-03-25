package uk.co.gresearch.siembol.common.metrics.storm;

import org.apache.storm.task.TopologyContext;
import uk.co.gresearch.siembol.common.metrics.SiembolMetricsRegistrar;

public interface StormMetricsRegistrarFactory {
    SiembolMetricsRegistrar createSiembolMetricsRegistrar(TopologyContext topologyContext);
}
