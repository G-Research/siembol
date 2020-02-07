package uk.co.gresearch.nortem.common.filesystem;

import java.io.IOException;

public class HdfsFileSystemFactory implements NortemFileSystemFactory {
    private final String uri;

    public HdfsFileSystemFactory(String uri) {
        this.uri = uri;
    }

    @Override
    public NortemFileSystem create() throws IOException {
        return new HdfsFileSystem(uri);
    }
}
