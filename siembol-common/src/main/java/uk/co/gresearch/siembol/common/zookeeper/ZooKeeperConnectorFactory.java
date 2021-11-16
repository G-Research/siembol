package uk.co.gresearch.siembol.common.zookeeper;

import uk.co.gresearch.siembol.common.model.ZooKeeperAttributesDto;

public interface ZooKeeperConnectorFactory extends ZooKeeperGenericConnectorFactory<ZooKeeperConnector> {
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
