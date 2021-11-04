package uk.co.gresearch.siembol.common.filesystem;

import uk.co.gresearch.siembol.common.utils.HttpProvider;

import java.io.IOException;

public class HttpFileSystemFactory implements SiembolFileSystemFactory {
    private static final long serialVersionUID = 1L;
    private final String uri;

    public HttpFileSystemFactory(String uri) {
        this.uri = uri;
    }

    @Override
    public SiembolFileSystem create() throws IOException {
        final HttpProvider httpProvider = new HttpProvider(uri, HttpProvider::getHttpClient);
        return new HttpFileSystem(httpProvider);
    }
}
