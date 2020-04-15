package uk.co.gresearch.siembol.common.zookeper;

import org.apache.curator.framework.recipes.cache.NodeCacheListener;
import java.io.Closeable;

public interface ZookeperConnector extends Closeable {
    String getData();

    void addCacheListener(NodeCacheListener listener);
}
