package uk.co.gresearch.siembol.common.zookeeper;

import java.util.List;
/**
 * An object for watching and manipulating a ZooKeeper list of Strings cache
 *
 * <p>This interface extends ZooKeeperGenericConnector interface, and it is used for watching and
 * manipulating a ZooKeeper list of Strings cache.
 *
 * @author  Marian Novotny
 * @see ZooKeeperGenericConnector
 *
 */
public interface ZooKeeperCompositeConnector extends ZooKeeperGenericConnector<List<String>> {
}
