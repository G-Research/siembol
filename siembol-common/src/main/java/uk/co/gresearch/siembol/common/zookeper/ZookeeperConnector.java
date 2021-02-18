package uk.co.gresearch.siembol.common.zookeper;

import org.apache.curator.framework.recipes.cache.NodeCacheListener;
import java.io.Closeable;

public interface ZookeeperConnector extends Closeable {
    String getData();
    void setData(String data) throws Exception;
    void addCacheListener(NodeCacheListener listener);
}
