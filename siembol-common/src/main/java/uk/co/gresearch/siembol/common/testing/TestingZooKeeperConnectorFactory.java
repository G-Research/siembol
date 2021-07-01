package uk.co.gresearch.siembol.common.testing;

import org.apache.curator.framework.recipes.cache.NodeCacheListener;
import uk.co.gresearch.siembol.common.model.ZooKeeperAttributesDto;
import uk.co.gresearch.siembol.common.zookeeper.ZooKeeperConnector;
import uk.co.gresearch.siembol.common.zookeeper.ZooKeeperConnectorFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TestingZooKeeperConnectorFactory implements ZooKeeperConnectorFactory {
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Map<String, String> cache = new HashMap<>();

    public ZooKeeperConnector createZookeeperConnector(ZooKeeperAttributesDto attributes) {
        return new TestingZooKeeperConnector(attributes.getZkPath());
    }

    public void setData(String path, String data) {
        cache.put(path, data);
    }

    public class TestingZooKeeperConnector implements ZooKeeperConnector {
        private final String path;
        private final List<NodeCacheListener> callBacks = new ArrayList<>();

        public TestingZooKeeperConnector(String path) {
            this.path = path;
        }

        @Override
        public String getData() {
            return cache.getOrDefault(path, "{}");
        }

        @Override
        public void setData(String data) throws Exception {
            cache.put(path, data);
            for (NodeCacheListener callBack: callBacks) {
                executorService.submit(() -> {
                    try {
                        callBack.nodeChanged();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        }

        @Override
        public void addCacheListener(NodeCacheListener listener) {
            callBacks.add(listener);
        }

        @Override
        public void close() throws IOException {
        }
    }
}
