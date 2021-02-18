package uk.co.gresearch.siembol.common.zookeper;

import uk.co.gresearch.siembol.common.model.ZookeeperAttributesDto;

import java.io.Serializable;

public interface ZookeeperConnectorFactory extends Serializable {
    default ZookeeperConnector createZookeeperConnector(ZookeeperAttributesDto attributes) throws Exception {
        return new ZookeeperConnectorImpl.Builder()
                .zkServer(attributes.getZkUrl())
                .path(attributes.getZkPath())
                .baseSleepTimeMs(attributes.getZkBaseSleepMs())
                .maxRetries(attributes.getZkMaxRetries())
                .build();

    }
}
