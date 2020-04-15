package uk.co.gresearch.siembol.common.filesystem;

import java.io.IOException;

public class HdfsFileSystemFactory implements SiembolFileSystemFactory {
    private final String uri;

    public HdfsFileSystemFactory(String uri) {
        this.uri = uri;
    }

    @Override
    public SiembolFileSystem create() throws IOException {
        return new HdfsFileSystem(uri);
    }
}
