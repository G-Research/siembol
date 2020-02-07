package uk.co.gresearch.nortem.common.filesystem;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

public interface NortemFileSystem extends Closeable {
    InputStream openInputStream(String path) throws IOException;
}
