package uk.co.gresearch.siembol.common.filesystem;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

public interface SiembolFileSystem extends Closeable {
    InputStream openInputStream(String path) throws IOException;
}
