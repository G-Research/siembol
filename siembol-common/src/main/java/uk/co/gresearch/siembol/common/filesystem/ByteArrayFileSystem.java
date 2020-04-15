package uk.co.gresearch.siembol.common.filesystem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.invoke.MethodHandles;

public class ByteArrayFileSystem implements SiembolFileSystem, Serializable {
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private String data;

    public ByteArrayFileSystem(String data) {
        this.data = data;
    }

    @Override
    public InputStream openInputStream(String path) throws IOException {
        LOG.debug("For the file path: {} returning stream with data: {}", path, data);
        return new ByteArrayInputStream(data.getBytes());
    }

    @Override
    public void close() throws IOException {
    }
}
