package uk.co.gresearch.siembol.common.filesystem;

import uk.co.gresearch.siembol.common.utils.HttpProvider;

import java.io.IOException;
/**
 * An object for creating Http file systems
 *
 * <p>This interface implements SiembolFilesystemFactory, and it is used for creating Http file system.
 *
 * @author  Marian Novotny
 * @see SiembolFileSystemFactory
 *
 */
public class HttpFileSystemFactory implements SiembolFileSystemFactory {
    private static final long serialVersionUID = 1L;
    private final String uri;

    public HttpFileSystemFactory(String uri) {
        this.uri = uri;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SiembolFileSystem create() throws IOException {
        final HttpProvider httpProvider = new HttpProvider(uri, HttpProvider::getHttpClient);
        return new HttpFileSystem(httpProvider);
    }
}
