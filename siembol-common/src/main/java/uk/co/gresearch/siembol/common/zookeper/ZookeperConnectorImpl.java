package uk.co.gresearch.siembol.common.zookeper;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.NodeCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;

import java.io.IOException;

import static java.nio.charset.StandardCharsets.UTF_8;

public class ZookeperConnectorImpl implements ZookeperConnector {
    private final CuratorFramework client;
    private final NodeCache cache;

    ZookeperConnectorImpl(Builder builder) {
        this.client = builder.client;
        this.cache = builder.cache;
    }

    public String getData() {
        ChildData childData =  cache.getCurrentData();
        return new String(childData.getData(), UTF_8);
    }

    public void addCacheListener(NodeCacheListener listener) {
        cache.getListenable().addListener(listener);
    }

    @Override
    public void close() throws IOException {
        client.close();
        cache.close();
    }

    public static class Builder {
        private String zkServer;
        private String path;
        private Integer baseSleepTimeMs = 1000;
        private Integer maxRetries = 3;
        private NodeCache cache;
        private CuratorFramework client;

        public Builder path(String path) {
            this.path = path;
            return this;
        }

        public Builder zkServer(String zkServer) {
            this.zkServer = zkServer;
            return this;
        }

        public Builder maxRetries(Integer maxRetries) {
            this.maxRetries = maxRetries;
            return this;
        }

        public Builder baseSleepTimeMs(Integer baseSleepTimeMs) {
            this.baseSleepTimeMs = baseSleepTimeMs;
            return this;
        }

        public ZookeperConnectorImpl build() throws Exception {
            if (zkServer == null
                    || path == null
                    || baseSleepTimeMs == null
                    || maxRetries == null) {
                throw new IllegalArgumentException("Missing required parameters to initialise zookeper connector");
            }
            client = CuratorFrameworkFactory.newClient(zkServer,
                   new ExponentialBackoffRetry(baseSleepTimeMs, maxRetries));
            client.start();

            cache = new NodeCache(client, path);
            cache.start(true);

            return new ZookeperConnectorImpl(this);
        }
    }
}
