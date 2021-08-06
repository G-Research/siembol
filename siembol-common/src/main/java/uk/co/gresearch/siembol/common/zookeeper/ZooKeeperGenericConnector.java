package uk.co.gresearch.siembol.common.zookeeper;

import org.apache.curator.framework.recipes.cache.NodeCacheListener;

import java.io.Closeable;

public interface ZooKeeperGenericConnector<T> extends Closeable {
    T getData();
    void setData(T data) throws Exception;
    void addCacheListener(NodeCacheListener listener);
}