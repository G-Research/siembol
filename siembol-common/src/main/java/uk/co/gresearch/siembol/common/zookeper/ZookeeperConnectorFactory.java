package uk.co.gresearch.siembol.common.zookeper;

import uk.co.gresearch.siembol.common.model.ZookeeperAttributesDto;

public interface ZookeeperConnectorFactory {
    default ZookeeperConnector createZookeeperConnector(ZookeeperAttributesDto attributes) throws Exception {
        return new ZookeeperConnectorImpl.Builder()
                .zkServer(attributes.getZkUrl())
                .path(attributes.getZkPath())
                .baseSleepTimeMs(attributes.getZkBaseSleepMs())
                .maxRetries(attributes.getZkMaxRetries())
                .build();

    }
}
