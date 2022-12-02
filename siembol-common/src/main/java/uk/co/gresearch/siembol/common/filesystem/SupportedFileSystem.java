package uk.co.gresearch.siembol.common.filesystem;

import java.util.function.Function;
/**
 * An enum for representing a Siembol file system for loading enrichment tables
 *
 * @author  Marian Novotny
 *
 * @see #HDFS
 * @see #HTTP
 */
public enum SupportedFileSystem {
    HDFS("hdfs", HdfsFileSystemFactory::new),
    HTTP("http", HttpFileSystemFactory::new);

    private final String urlPrefix;
    private final Function<String, SiembolFileSystemFactory> factoryFun;
    private static final String UNSUPPORTED_FILE_SYSTEM_FOR_URI = "Unsupported file system for uri: %s";

    SupportedFileSystem(String name, Function<String, SiembolFileSystemFactory> factoryFun) {
        this.urlPrefix = name;
        this.factoryFun = factoryFun;
    }

    public static SiembolFileSystemFactory fromUri(String uri) {
        for (SupportedFileSystem fs : SupportedFileSystem.values()) {
            if (uri.toLowerCase().startsWith(fs.urlPrefix)) {
                return fs.factoryFun.apply(uri);
            }
        }
        throw new IllegalArgumentException(String.format(UNSUPPORTED_FILE_SYSTEM_FOR_URI, uri));
    }
}
