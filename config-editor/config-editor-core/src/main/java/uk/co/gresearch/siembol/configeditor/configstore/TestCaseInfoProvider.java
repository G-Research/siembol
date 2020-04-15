package uk.co.gresearch.siembol.configeditor.configstore;

import uk.co.gresearch.siembol.configeditor.model.ConfigEditorFile;

import java.util.List;

public class TestCaseInfoProvider implements ConfigInfoProvider {
    private static final String UNSUPPORTED_MESSAGE = "Not supported operation";
    private static final String AUTHOR_FIELD = "author";
    private static final String NAME_FIELD = "test_case_name";
    private static final String NAME_PREFIX_FIELD = "config_name";
    private static final String VERSION_FIELD = "version";
    private static final String UNDEFINED = "undefined";
    private final JsonConfigInfoProvider jsonHelperProvider;

    public TestCaseInfoProvider() {
        jsonHelperProvider = new JsonConfigInfoProvider.Builder()
                .configAuthorField(AUTHOR_FIELD)
                .configNameField(NAME_FIELD)
                .configNamePrefixField(NAME_PREFIX_FIELD)
                .configsVersionField(UNDEFINED)
                .configVersionField(VERSION_FIELD)
                .setConfigInfoType(ConfigInfoType.TEST_CASE)
                .releaseFilename(UNDEFINED)
                .build();
    }

    @Override
    public ConfigInfo getConfigInfo(String user, String config) {
        return jsonHelperProvider.getConfigInfo(user, config);
    }

    @Override
    public ConfigInfo getReleaseInfo(String user, String release) {
        throw new UnsupportedOperationException(UNSUPPORTED_MESSAGE);
    }

    @Override
    public int getReleaseVersion(List<ConfigEditorFile> files) {
        throw new UnsupportedOperationException(UNSUPPORTED_MESSAGE);
    }

    @Override
    public boolean isReleaseFile(String filename) {
        throw new UnsupportedOperationException(UNSUPPORTED_MESSAGE);
    }

    @Override
    public ConfigEditorFile.ContentType getFileContentType() {
        return ConfigEditorFile.ContentType.RAW_JSON_STRING;
    }

    @Override
    public boolean isStoreFile(String filename) {
        return jsonHelperProvider.isStoreFile(filename);
    }
}
