package uk.co.gresearch.siembol.common.zookeeper;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.*;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.ZookeeperFactory;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.nio.charset.StandardCharsets.UTF_8;
/**
 * An object for watching and manipulating a ZooKeeper cache
 *
 * <p>This class implements ZooKeeperConnector interface.
 * It is used for watching and manipulating a ZooKeeper Utf-8 String cache.
 *
 * @author  Marian Novotny
 *
 */
public class ZooKeeperConnectorImpl implements ZooKeeperConnector {
    private static final int SLEEP_TIME_MS = 100;
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final String EMPTY_GET_DATA_MSG = "Trying to read form empty cache from zk path: %s";
    private static final String INIT_TIMEOUT_MSG = "Initialisation of zk path: %s exceeded timeout ";
    private static final String NON_JSON_DATA_MSG = "Data set in zk path: {} is not JSON";
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();

    private final CuratorFramework client;
    private final CuratorCache cache;
    private final String path;
    private final AtomicBoolean initialised;
    private final int initTimeout;


    ZooKeeperConnectorImpl(Builder builder) {
        this.client = builder.client;
        this.cache = builder.cache;
        this.path = builder.path;
        this.initialised = builder.initialised;
        this.initTimeout = builder.initTimeout;
    }

    /**
     * {@inheritDoc}
     */
    public String getData() {
        Optional<ChildData> childData = cache.get(path);
        if (childData.isPresent()) {
            return new String(childData.get().getData(), UTF_8);
        } else {
            throw new IllegalStateException(String.format(EMPTY_GET_DATA_MSG, path));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setData(String data) throws Exception {
        try {
            var json = JSON_MAPPER.readValue(data, JsonNode.class);
            client.setData().forPath(this.path, JSON_MAPPER.writeValueAsBytes(json));
        } catch (JsonParseException e) {
            LOG.warn(NON_JSON_DATA_MSG, this.path);
            client.setData().forPath(this.path, data.getBytes(UTF_8));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addCacheListener(Runnable listener) {
        cache.listenable().addListener((x, y, z) -> listener.run());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialise() throws Exception {
        int initTime = 0;
        while (!initialised.get()) {
            initTime += SLEEP_TIME_MS;
            if (initTime > initTimeout) {
                throw new IllegalStateException(String.format(INIT_TIMEOUT_MSG, path));
            }
            Thread.sleep(SLEEP_TIME_MS);
        }
    }

    /**
     * Closes the connector
     * @throws IOException on error
     */
    @Override
    public void close() throws IOException {
        cache.close();
        client.close();
    }

    /**
     * Builder for ZooKeeperConnectorImpl instance
     *
     * @author  Marian Novotny
     */
    public static class Builder {
        private static final String WRONG_ATTRIBUTES_LOG_MSG = "Missing ZooKeeper connector attributes, zkServer: {}, " +
                "path: {}, baseSleepTimeMs: {}, maxRetries: {}";
        private static final String WRONG_ATTRIBUTES_EXCEPTION_MSG = "Missing required parameters to initialise " +
                "ZooKeeper connector";
        private static final String INIT_NON_EXISTING_LOG_MSG = "Initialising ZooKeeper node {} with the value {}";

        private String zkServer;
        private String path;
        private Integer baseSleepTimeMs = 1000;
        private Integer maxRetries = 3;
        private CuratorCache cache;
        private CuratorFramework client;
        private Optional<String> initValue = Optional.empty();
        private final AtomicBoolean initialised = new AtomicBoolean(false);
        private int initTimeout = 3000;

        /**
         * Sets a ZooKeeper node path
         * @param path to a node cache
         * @return this builder
         */
        public Builder path(String path) {
            this.path = path;
            return this;
        }

        /**
         * Sets ZooKeeper server(s) url
         * @param zkServer url String
         * @return this builder
         */
        public Builder zkServer(String zkServer) {
            this.zkServer = zkServer;
            return this;
        }

        /**
         * Sets max retries for ZooKeeper client
         * @param maxRetries of a ZooKeeper client
         * @return this builder
         */
        public Builder maxRetries(Integer maxRetries) {
            this.maxRetries = maxRetries;
            return this;
        }

        /**
         * Sets base sleep time for an exponential backoff retry
         * @param baseSleepTimeMs base sleep time for an exponential backoff retry in milliseconds
         * @return this builder
         */
        public Builder baseSleepTimeMs(Integer baseSleepTimeMs) {
            this.baseSleepTimeMs = baseSleepTimeMs;
            return this;
        }

        /**
         * Sets an initial value of the node if the node does nto exist
         * @param initValue a string with an initial node value
         * @return this builder
         */
        public Builder initValueIfNotExists(String initValue) {
            this.initValue = Optional.ofNullable(initValue);
            return this;
        }

        /**
         * Sets the timeout for initialise method
         * @param initTimeout timeout in milliseconds
         * @return this builder
         */
        public Builder initTimeout(int initTimeout) {
            this.initTimeout = initTimeout;
            return this;
        }

        /**
         * Builds a ZooKeeper connector instance
         *
         * @return a ZooKeeper connector instance built from an internal state of the builder
         * @throws Exception on error
         */
        public ZooKeeperConnectorImpl build() throws Exception {
            if (zkServer == null
                    || path == null
                    || baseSleepTimeMs == null
                    || maxRetries == null) {
                LOG.error(WRONG_ATTRIBUTES_LOG_MSG, zkServer, path, baseSleepTimeMs, maxRetries);
                throw new IllegalArgumentException(WRONG_ATTRIBUTES_EXCEPTION_MSG);
            }

            final var connectString = zkServer;
            ZookeeperFactory zooKeeperFactory = (String x, int sessionTimeout, Watcher watcher, boolean canBeReadOnly)
                    -> new ZooKeeper(connectString, sessionTimeout, watcher, canBeReadOnly);

            client = CuratorFrameworkFactory.builder()
                    .connectString(connectString)
                    .zookeeperFactory(zooKeeperFactory)
                    .retryPolicy(new ExponentialBackoffRetry(baseSleepTimeMs, maxRetries))
                    .build();

            client.start();
            if (initValue.isPresent() && client.checkExists().forPath(path) == null) {
                LOG.warn(INIT_NON_EXISTING_LOG_MSG, path, initValue.get());
                client.create()
                        .creatingParentsIfNeeded()
                        .withMode(CreateMode.PERSISTENT)
                        .withACL(ZooDefs.Ids.OPEN_ACL_UNSAFE)
                        .forPath(path, initValue.get().getBytes(UTF_8));
            }

            cache = CuratorCache.build(client, path, CuratorCache.Options.SINGLE_NODE_CACHE);
            CuratorCacheListener listener = CuratorCacheListener.builder()
                    .forInitialized(() -> initialised.set(true))
                    .build();
            cache.listenable().addListener(listener);
            cache.start();

            return new ZooKeeperConnectorImpl(this);
        }
    }
}
