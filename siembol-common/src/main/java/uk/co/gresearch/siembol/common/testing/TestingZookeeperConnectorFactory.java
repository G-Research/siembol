package uk.co.gresearch.siembol.common.testing;

import org.apache.curator.framework.recipes.cache.NodeCacheListener;
import uk.co.gresearch.siembol.common.model.ZookeeperAttributesDto;
import uk.co.gresearch.siembol.common.zookeper.ZookeeperConnector;
import uk.co.gresearch.siembol.common.zookeper.ZookeeperConnectorFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TestingZookeeperConnectorFactory implements ZookeeperConnectorFactory {
    private static final long serialVersionUID = 1L;

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Map<String, String> cache = new HashMap<>();

    public ZookeeperConnector createZookeeperConnector(ZookeeperAttributesDto attributes) {
        return new TestingZookeeperConnector(attributes.getZkPath());
    }

    public void setData(String path, String data) {
        cache.put(path, data);
    }

    public class TestingZookeeperConnector implements ZookeeperConnector {
        private final String path;
        private final List<NodeCacheListener> callBacks = new ArrayList<>();

        public TestingZookeeperConnector(String path) {
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
