package uk.co.gresearch.siembol.common.zookeper;

import java.io.Serializable;

public interface ZookeperConnectorFactory extends Serializable {
    default ZookeperConnector createZookeperConnector(ZookeperAttributes attributes) throws Exception {
        return new ZookeperConnectorImpl.Builder()
                .zkServer(attributes.getZkUrl())
                .path(attributes.getZkPath())
                .baseSleepTimeMs(attributes.getZkBaseSleepMs())
                .maxRetries(attributes.getZkMaxRetries())
                .build();

    }
}
