package uk.co.gresearch.siembol.common.zookeeper;

import uk.co.gresearch.siembol.common.model.ZooKeeperAttributesDto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
/**
 * An object for creating a ZooKeeper composite connector
 *
 * <p>This class implements ZooKeeperCompositeConnectorFactory and Serializable interfaces.
 * It is used for creating a ZooKeeper composite connectors from ZooKeeper attributes.
 * Node paths are delimited by new line in the attributes.
 *
 * @author  Marian Novotny
 * @see ZooKeeperAttributesDto
 * @see ZooKeeperCompositeConnector
 *
 */
public class ZooKeeperCompositeConnectorFactoryImpl implements ZooKeeperCompositeConnectorFactory, Serializable {
    private static final long serialVersionUID = 1L;
    private static final String PATH_DELIMITER = "\n";

    /**
     * {@inheritDoc}
     */
    @Override
    public ZooKeeperCompositeConnector createZookeeperConnector(ZooKeeperAttributesDto attributes)
            throws Exception {
        List<ZooKeeperConnector> zooKeeperConnectors = new ArrayList<>();
        final String[] paths = attributes.getZkPath().split(PATH_DELIMITER);
        for (String path : paths) {
            zooKeeperConnectors.add(new ZooKeeperConnectorImpl.Builder()
                    .zkServer(attributes.getZkUrl())
                    .path(path)
                    .baseSleepTimeMs(attributes.getZkBaseSleepMs())
                    .maxRetries(attributes.getZkMaxRetries())
                    .build());
        }

        var compositeConnector = new ZookeeperCompositeConnectorImpl(zooKeeperConnectors);
        compositeConnector.initialise();
        return compositeConnector;
    }
}
