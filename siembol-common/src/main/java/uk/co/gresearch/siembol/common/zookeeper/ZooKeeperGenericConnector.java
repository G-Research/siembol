package uk.co.gresearch.siembol.common.zookeeper;

import java.io.Closeable;

public interface ZooKeeperGenericConnector<T> extends Closeable {
    T getData();

    void setData(T data) throws Exception;

    void addCacheListener(Runnable listener);

    default void initialise() throws Exception {
    }
}