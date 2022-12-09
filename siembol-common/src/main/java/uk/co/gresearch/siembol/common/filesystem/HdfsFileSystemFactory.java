package uk.co.gresearch.siembol.common.filesystem;

import java.io.IOException;
/**
 * An object for creating Hdfs file systems
 *
 * <p>This interface implements SiembolFilesystemFactory, and it is used for creating Hdfs file system.
 *
 * @author  Marian Novotny
 * @see SiembolFileSystemFactory
 *
 */
public class HdfsFileSystemFactory implements SiembolFileSystemFactory {
    private static final long serialVersionUID = 1L;
    private final String uri;

    public HdfsFileSystemFactory(String uri) {
        this.uri = uri;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SiembolFileSystem create() throws IOException {
        return new HdfsFileSystem(uri);
    }
}
