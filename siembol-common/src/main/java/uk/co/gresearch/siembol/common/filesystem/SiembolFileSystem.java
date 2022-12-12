package uk.co.gresearch.siembol.common.filesystem;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
/**
 * An object for opening input streams
 *
 * <p>This interface extends Closeable interface, and it is used for opening input streams.
 *
 * @author  Marian Novotny
 *
 */
public interface SiembolFileSystem extends Closeable {
    /**
     * Opens input stream from a path
     * @param path a path to an input stream
     * @return opened input stream
     * @throws IOException on error
     */
    InputStream openInputStream(String path) throws IOException;
}
