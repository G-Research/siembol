package uk.co.gresearch.siembol.common.filesystem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.invoke.MethodHandles;
/**
 * An object for opening input streams from a string
 *
 * <p>This class implements SiembolFileSystem, and it is used for opening input streams from a string.
 * It is used in unit tests.
 *
 * @author  Marian Novotny
 * @see SiembolFileSystem
 *
 */
public class ByteArrayFileSystem implements SiembolFileSystem, Serializable {
    private static final long serialVersionUID = 1L;
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final String data;

    public ByteArrayFileSystem(String data) {
        this.data = data;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InputStream openInputStream(String path) {
        LOG.debug("For the file path: {} returning stream with data: {}", path, data);
        return new ByteArrayInputStream(data.getBytes());
    }

    @Override
    public void close() throws IOException {
    }
}
