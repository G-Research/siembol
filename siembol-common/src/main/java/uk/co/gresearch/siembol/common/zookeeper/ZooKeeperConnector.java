package uk.co.gresearch.siembol.common.zookeeper;
/**
 * An object for watching and manipulating a ZooKeeper String cache
 *
 * <p>This interface extends ZooKeeperGenericConnector interface, and it is used for watching and
 * manipulating a ZooKeeper String cache.
 *
 * @author  Marian Novotny
 * @see ZooKeeperGenericConnector
 *
 */
public interface ZooKeeperConnector extends ZooKeeperGenericConnector<String> {
}
