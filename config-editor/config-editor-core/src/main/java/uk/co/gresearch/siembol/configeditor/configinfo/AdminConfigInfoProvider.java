package uk.co.gresearch.siembol.configeditor.configinfo;

import uk.co.gresearch.siembol.configeditor.common.ConfigInfo;
import uk.co.gresearch.siembol.configeditor.common.ConfigInfoProvider;
import uk.co.gresearch.siembol.configeditor.common.ConfigInfoType;
import uk.co.gresearch.siembol.configeditor.common.UserInfo;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorFile;

import java.util.List;

public class AdminConfigInfoProvider implements ConfigInfoProvider {
    private static final String UNSUPPORTED_MESSAGE = "Not supported operation";
    private static final String ADMIN_CONFIG_FILE_NAME = "admin_config.json";
    private static final String VERSION_FIELD = "config_version";
    private static final String UNDEFINED = "undefined";
    private final JsonConfigInfoProvider jsonHelperProvider;

    public AdminConfigInfoProvider() {
        jsonHelperProvider = new JsonConfigInfoProvider.Builder()
                .configAuthorField(UNDEFINED)
                .configNameField(UNDEFINED)
                .configNamePrefixField(UNDEFINED)
                .configsVersionField(VERSION_FIELD)
                .configVersionField(UNDEFINED)
                .setConfigInfoType(ConfigInfoType.ADMIN_CONFIG)
                .releaseFilename(ADMIN_CONFIG_FILE_NAME)
                .build();
    }

    @Override
    public ConfigInfo getConfigInfo(UserInfo user, String config) {
        throw new UnsupportedOperationException(UNSUPPORTED_MESSAGE);
    }

    @Override
    public ConfigInfo getReleaseInfo(UserInfo user, String release) {
        return jsonHelperProvider.getReleaseInfo(user, release);
    }

    @Override
    public int getReleaseVersion(List<ConfigEditorFile> files) {
        return jsonHelperProvider.getReleaseVersion(files);
    }

    @Override
    public boolean isReleaseFile(String filename) {
        return jsonHelperProvider.isReleaseFile(filename);
    }

    @Override
    public ConfigInfoType getConfigInfoType() {
        return jsonHelperProvider.getConfigInfoType();
    }

    @Override
    public ConfigEditorFile.ContentType getFileContentType() {
        return ConfigEditorFile.ContentType.RAW_JSON_STRING;
    }

    @Override
    public boolean isStoreFile(String filename) {
        throw new UnsupportedOperationException(UNSUPPORTED_MESSAGE);
    }
}
