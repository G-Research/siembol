package uk.co.gresearch.siembol.common.zookeeper;

import uk.co.gresearch.siembol.common.model.ZooKeeperAttributesDto;
/**
 * An object for creating a ZooKeeper connector
 *
 * <p>This interface extends ZooKeeperGenericConnectorFactory interface.
 * It is used for creating a ZooKeeper connector for a String cache from ZooKeeper attributes.
 *
 * @author  Marian Novotny
 * @see ZooKeeperAttributesDto
 *
 */
public interface ZooKeeperConnectorFactory extends ZooKeeperGenericConnectorFactory<ZooKeeperConnector> {
    /**
     * {@inheritDoc}
     */
    default ZooKeeperConnector createZookeeperConnector(ZooKeeperAttributesDto attributes) throws Exception {
        var connector = new ZooKeeperConnectorImpl.Builder()
                .zkServer(attributes.getZkUrl())
                .path(attributes.getZkPath())
                .baseSleepTimeMs(attributes.getZkBaseSleepMs())
                .maxRetries(attributes.getZkMaxRetries())
                .initValueIfNotExists(attributes.getInitValueIfNotExists())
                .build();
        connector.initialise();
        return connector;
    }
}
