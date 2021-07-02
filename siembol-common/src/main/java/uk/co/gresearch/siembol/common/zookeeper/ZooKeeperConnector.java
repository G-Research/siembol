package uk.co.gresearch.siembol.common.zookeeper;

import org.apache.curator.framework.recipes.cache.NodeCacheListener;
import java.io.Closeable;

public interface ZooKeeperConnector extends Closeable {
    String getData();
    void setData(String data) throws Exception;
    void addCacheListener(NodeCacheListener listener);
}
