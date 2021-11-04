package uk.co.gresearch.siembol.common.filesystem;

import uk.co.gresearch.siembol.common.utils.HttpProvider;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class HttpFileSystem implements SiembolFileSystem {
    private final HttpProvider httpProvider;

    public HttpFileSystem(HttpProvider httpProvider) {
        this.httpProvider = httpProvider;
    }

    @Override
    public InputStream openInputStream(String path) throws IOException {
        return new ByteArrayInputStream(httpProvider.get(path).getBytes());
    }

    @Override
    public void close() throws IOException {
    }
}
