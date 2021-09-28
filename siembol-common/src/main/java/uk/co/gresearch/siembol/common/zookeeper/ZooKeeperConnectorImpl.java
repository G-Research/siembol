package uk.co.gresearch.siembol.common.zookeeper;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.NodeCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.Optional;

import static java.nio.charset.StandardCharsets.UTF_8;

public class ZooKeeperConnectorImpl implements ZooKeeperConnector {
    private final CuratorFramework client;
    private final NodeCache cache;
    private final String path;

    ZooKeeperConnectorImpl(Builder builder) {
        this.client = builder.client;
        this.cache = builder.cache;
        this.path = builder.path;
    }

    public String getData() {
        ChildData childData =  cache.getCurrentData();
        return new String(childData.getData(), UTF_8);
    }

    @Override
    public void setData(String data) throws Exception {
        client.setData().forPath(this.path, data.getBytes(UTF_8));
    }

    @SuppressWarnings( "deprecation" )
    public void addCacheListener(NodeCacheListener listener) {
        cache.getListenable().addListener(listener);
    }

    @Override
    public void close() throws IOException {
        client.close();
        cache.close();
    }

    public static class Builder {
        private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
        private static final String WRONG_ATTRIBUTES_LOG_MSG = "Missing ZooKeeper connector attributes, zkServer: {}, " +
                "path: {}, baseSleepTimeMs: {}, maxRetries: {}";
        private static final String WRONG_ATTRIBUTES_EXCEPTION_MSG = "Missing required parameters to initialise " +
                "ZooKeeper connector";
        private static final String INIT_NON_EXISTING_LOG_MSG = "Initialising ZooKeeper node {} with the value {}";

        private String zkServer;
        private String path;
        private Integer baseSleepTimeMs = 1000;
        private Integer maxRetries = 3;
        private NodeCache cache;
        private CuratorFramework client;
        private Optional<String> initValue = Optional.empty();

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

        public Builder initValueIfNotExists(String initValue) {
            this.initValue = Optional.ofNullable(initValue);
            return this;
        }

        public ZooKeeperConnectorImpl build() throws Exception {
            if (zkServer == null
                    || path == null
                    || baseSleepTimeMs == null
                    || maxRetries == null) {
                LOG.error(WRONG_ATTRIBUTES_LOG_MSG, zkServer, path, baseSleepTimeMs, maxRetries);
                throw new IllegalArgumentException(WRONG_ATTRIBUTES_EXCEPTION_MSG);
            }
            client = CuratorFrameworkFactory.newClient(zkServer,
                    new ExponentialBackoffRetry(baseSleepTimeMs, maxRetries));
            client.start();

            cache = new NodeCache(client, path);
            cache.start(true);

            if (initValue.isPresent()
                    && (cache.getCurrentData() == null || cache.getCurrentData().getData() == null)) {
                LOG.warn(INIT_NON_EXISTING_LOG_MSG, path, initValue.get());
                client.setData().forPath(this.path, initValue.get().getBytes(UTF_8));
            }

            return new ZooKeeperConnectorImpl(this);
        }
    }
}
