package uk.co.gresearch.siembol.common.zookeeper;

import uk.co.gresearch.siembol.common.model.ZooKeeperAttributesDto;

public interface ZooKeeperConnectorFactory {
    default ZooKeeperConnector createZookeeperConnector(ZooKeeperAttributesDto attributes) throws Exception {
        return new ZooKeeperConnectorImpl.Builder()
                .zkServer(attributes.getZkUrl())
                .path(attributes.getZkPath())
                .baseSleepTimeMs(attributes.getZkBaseSleepMs())
                .maxRetries(attributes.getZkMaxRetries())
                .build();

    }
}
