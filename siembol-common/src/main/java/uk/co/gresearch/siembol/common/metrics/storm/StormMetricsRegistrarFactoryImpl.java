package uk.co.gresearch.siembol.common.metrics.storm;

import org.apache.storm.task.TopologyContext;
import uk.co.gresearch.siembol.common.metrics.SiembolMetricsRegistrar;

import java.io.Serializable;
/**
 * An object for creating a Siembol metrics registrar in Storm topology
 *
 * <p>This class implements StormMetricsRegistrarFactory and Serializable interfaces.
 * It is used for creating a Siembol metrics registrar in Storm topology.
 *
 * @author  Marian Novotny
 * @see StormMetricsRegistrarFactory
 *
 */
public class StormMetricsRegistrarFactoryImpl implements StormMetricsRegistrarFactory, Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * {@inheritDoc}
     */
    @Override
    public SiembolMetricsRegistrar createSiembolMetricsRegistrar(TopologyContext topologyContext) {
        return new StormMetricsRegistrar(topologyContext).cachedRegistrar();
    }
}
