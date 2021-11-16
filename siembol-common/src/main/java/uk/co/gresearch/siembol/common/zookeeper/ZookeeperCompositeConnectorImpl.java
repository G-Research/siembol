package uk.co.gresearch.siembol.common.zookeeper;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class ZookeeperCompositeConnectorImpl implements ZooKeeperCompositeConnector {
    private final List<ZooKeeperConnector> zooKeeperConnectors;

    public ZookeeperCompositeConnectorImpl(List<ZooKeeperConnector> zooKeeperConnectors) {
        this.zooKeeperConnectors = zooKeeperConnectors;
    }

    @Override
    public List<String> getData() {
        return zooKeeperConnectors.stream().map(x -> x.getData()).collect(Collectors.toList());
    }

    @Override
    public void setData(List<String> data) throws Exception {
        if (data == null || data.size() != zooKeeperConnectors.size()) {
            throw new IllegalArgumentException();
        }
        for (int i = 0; i < zooKeeperConnectors.size(); i++) {
            zooKeeperConnectors.get(i).setData(data.get(i));
        }
    }

    @Override
    public void addCacheListener(Runnable listener) {
        zooKeeperConnectors.forEach(x -> x.addCacheListener(listener));
    }

    @Override
    public void close() throws IOException {
        for (var connector : zooKeeperConnectors) {
            connector.close();
        }
    }

    @Override
    public void initialise() throws Exception {
        for (var connector : zooKeeperConnectors) {
            connector.initialise();
        }
    }
}
