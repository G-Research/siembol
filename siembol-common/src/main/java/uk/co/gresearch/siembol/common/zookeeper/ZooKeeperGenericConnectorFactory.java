package uk.co.gresearch.siembol.common.zookeeper;

import uk.co.gresearch.siembol.common.model.ZooKeeperAttributesDto;
/**
 * An object for creating a ZooKeeper connector
 *
 * <p>This interface is used for creating a ZooKeeper connector from ZooKeeper attributes.
 *
 * @author  Marian Novotny
 * @see ZooKeeperAttributesDto
 *
 */
public interface ZooKeeperGenericConnectorFactory<T> {
    /**
     * Creates a ZooKeeper connector
     * @param attributes ZooKeeper attributes
     * @return a ZooKeeper connector instance
     * @throws Exception on error
     */
    T createZookeeperConnector(ZooKeeperAttributesDto attributes) throws Exception;
}
