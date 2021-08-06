package uk.co.gresearch.siembol.common.zookeeper;

import uk.co.gresearch.siembol.common.model.ZooKeeperAttributesDto;

public interface ZooKeeperGenericConnectorFactory<T> {
    T createZookeeperConnector(ZooKeeperAttributesDto attributes) throws Exception;
}
