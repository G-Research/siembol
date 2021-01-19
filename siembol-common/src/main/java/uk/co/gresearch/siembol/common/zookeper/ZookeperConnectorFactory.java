package uk.co.gresearch.siembol.common.zookeper;

import uk.co.gresearch.siembol.common.model.ZookeperAttributesDto;

import java.io.Serializable;

public interface ZookeperConnectorFactory extends Serializable {
    default ZookeperConnector createZookeperConnector(ZookeperAttributesDto attributes) throws Exception {
        return new ZookeperConnectorImpl.Builder()
                .zkServer(attributes.getZkUrl())
                .path(attributes.getZkPath())
                .baseSleepTimeMs(attributes.getZkBaseSleepMs())
                .maxRetries(attributes.getZkMaxRetries())
                .build();

    }
}
