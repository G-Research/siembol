package uk.co.gresearch.siembol.common.filesystem;

import uk.co.gresearch.siembol.common.utils.HttpProvider;

import java.io.IOException;
import java.io.InputStream;

public class HttpFileSystem implements SiembolFileSystem {
    private final HttpProvider httpProvider;

    public HttpFileSystem(HttpProvider httpProvider) {
        this.httpProvider = httpProvider;
    }

    @Override
    public InputStream openInputStream(String path) throws IOException {
        return httpProvider.getStream(path);
    }

    @Override
    public void close() throws IOException {
    }
}
