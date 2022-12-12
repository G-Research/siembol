package uk.co.gresearch.siembol.common.filesystem;

import java.io.IOException;
import java.io.Serializable;
/**
 * An object for creating Siembol file systems
 *
 * <p>This interface extends Serializable interface, and it is used for creating Siembol file system.
 *
 * @author  Marian Novotny
 * @see SiembolFileSystem
 *
 */
public interface SiembolFileSystemFactory extends Serializable {
    /**
     * Creates a siembol file system
     * @return created file system
     * @throws IOException on error
     */
    SiembolFileSystem create() throws IOException;
}
