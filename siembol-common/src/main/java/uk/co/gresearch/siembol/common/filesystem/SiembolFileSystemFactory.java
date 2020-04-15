package uk.co.gresearch.siembol.common.filesystem;

import java.io.IOException;
import java.io.Serializable;

public interface SiembolFileSystemFactory extends Serializable {
    SiembolFileSystem create() throws IOException;
}
