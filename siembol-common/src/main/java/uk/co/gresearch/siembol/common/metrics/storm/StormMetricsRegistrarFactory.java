package uk.co.gresearch.siembol.common.metrics.storm;

import org.apache.storm.task.TopologyContext;
import uk.co.gresearch.siembol.common.metrics.SiembolMetricsRegistrar;
/**
 * An object for creating a Siembol metrics registrar in Storm topology
 *
 * <p>This interface for creating a Siembol metrics registrar in Storm topology.
 *
 * @author  Marian Novotny
 *
 */
public interface StormMetricsRegistrarFactory {
    /**
     * Creates Siembol metrics registrar
     * @param topologyContext a Storm topology context provided in a Bolt prepare method
     * @return created Siembol metrics registrar
     */
    SiembolMetricsRegistrar createSiembolMetricsRegistrar(TopologyContext topologyContext);
}
