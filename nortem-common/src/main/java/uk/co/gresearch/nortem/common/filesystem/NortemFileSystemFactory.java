package uk.co.gresearch.nortem.common.filesystem;

import java.io.IOException;
import java.io.Serializable;

public interface NortemFileSystemFactory extends Serializable {
    NortemFileSystem create() throws IOException;
}
