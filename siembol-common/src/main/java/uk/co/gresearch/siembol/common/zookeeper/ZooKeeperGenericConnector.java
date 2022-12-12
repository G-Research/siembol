package uk.co.gresearch.siembol.common.zookeeper;

import java.io.Closeable;
/**
 * An object for watching and manipulating a ZooKeeper cache
 *
 * <p>This interface extends Closeable interface, and it is used for watching and manipulating a ZooKeeper cache.
 *
 * @author  Marian Novotny
 *
 */
public interface ZooKeeperGenericConnector<T> extends Closeable {
    /**
     * Gets the value of the cache node
     * @return the value of the cache node
     */
    T getData();

    /**
     * Sets the value of the cache node
     * @param data a value to be set
     * @throws Exception on error
     */
    void setData(T data) throws Exception;

    /**
     * Adds a callback to be called after a node change
     *
     * @param listener a callback to be called after a node change
     */
    void addCacheListener(Runnable listener);

    /**
     * Initializes the node and waits until the node is ready
     *
     * @throws Exception on error
     */
    default void initialise() throws Exception {
    }
}