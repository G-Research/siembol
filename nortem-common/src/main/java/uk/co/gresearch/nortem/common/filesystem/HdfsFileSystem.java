package uk.co.gresearch.nortem.common.filesystem;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;

import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.net.URI;
import org.apache.hadoop.fs.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HdfsFileSystem implements NortemFileSystem {
    private static final Logger LOG = LoggerFactory
            .getLogger(MethodHandles.lookup().lookupClass());
    private static final String FILE_DOES_NOT_EXIST_MSG = "The file: %s does not exist";
    private final FileSystem fs;

    public HdfsFileSystem(String uri, Configuration configuration) throws IOException {
        this.fs = FileSystem.newInstance(URI.create(uri), configuration);
    }

    public HdfsFileSystem(String uri) throws IOException {
        this(uri, new Configuration());
    }

    public InputStream openInputStream(String path) throws IOException {
        Path hdfsPath = new Path(path);
        if (fs.exists(hdfsPath)) {
            return fs.open(hdfsPath);
        } else {
            String errorMsg = String.format(FILE_DOES_NOT_EXIST_MSG, path);
            LOG.error(errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }
    }

    @Override
    public void close() throws IOException {
        fs.close();
    }
}
