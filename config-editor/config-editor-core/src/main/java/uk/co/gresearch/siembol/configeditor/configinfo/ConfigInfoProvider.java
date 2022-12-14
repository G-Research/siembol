package uk.co.gresearch.siembol.configeditor.configinfo;
import uk.co.gresearch.siembol.configeditor.common.ConfigInfo;
import uk.co.gresearch.siembol.configeditor.common.ConfigInfoType;
import uk.co.gresearch.siembol.configeditor.common.UserInfo;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorFile;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.TimeZone;
/**
 * An object for providing metadata about a configuration change
 *
 * <p>This interface is for providing metadata about a configuration change.
 * It provides information such as the author of the change, type of change, and the version of the configuration.
 *
 * @author  Marian Novotny
 *
 */
public interface ConfigInfoProvider {
    String RELEASE_BRANCH_TEMPLATE = "ver_%d_by_%s_on_%s";
    String MISSING_ARGUMENTS_MSG = "missing user info attributes";
    String UNKNOWN_USER_INFO = "unknown";
    int MILLI_SECONDS = 1000;
    int INIT_RELEASE_VERSION = 0;

    /**
     * Gets the config info
     * @param user a user info object
     * @param config a json string with configuration
     * @return a config info object
     * @see ConfigInfo
     */
    ConfigInfo getConfigInfo(UserInfo user, String config);

    /**
     * Gets the config info for a default user
     *
     * @param config a json string with configuration
     * @return a config info object
     * @see ConfigInfo
     */
    default ConfigInfo getConfigInfo(String config) {
        UserInfo unknownUser = new UserInfo();
        unknownUser.setUserName(UNKNOWN_USER_INFO);
        unknownUser.setEmail(UNKNOWN_USER_INFO);
        return getConfigInfo(unknownUser, config);
    }

    /**
     * Gets a release info
     * @param user a user info object
     * @param release a json string with a release
     * @return a config info object
     * @see ConfigInfo
     */
    ConfigInfo getReleaseInfo(UserInfo user, String release);

    /**
     * Gets a release version from a list of files
     * @param files the list of files
     * @return version of the release
     */
    int getReleaseVersion(List<ConfigEditorFile> files);

    /**
     * Gets a release version from a release file
     * @param content a json string with release
     * @return version of the release
     */
    int getReleaseVersion(String content);

    /**
     * Gets information whether the config is included in the release
     * @param release a json string with release
     * @param configName teh name of config to check
     * @return true if the config is included in the release, otherwise false
     */
    boolean isConfigInRelease(String release, String configName);

    /**
     * Gets file content type
     * @return the content type of the config file
     * @see ConfigEditorFile.ContentType
     */
    ConfigEditorFile.ContentType getFileContentType();

    default ConfigInfo configInfoFromUser(UserInfo user) {
        if (user == null || user.getUserName() == null || user.getEmail() == null) {
            throw new IllegalArgumentException(MISSING_ARGUMENTS_MSG);
        }

        ConfigInfo ret = new ConfigInfo();
        String committer = user.getUserName();
        ret.setCommitter(committer);
        ret.setCommitterEmail(user.getEmail());
        return ret;
    }

    default String getLocalDateTime() {
        return LocalDateTime.ofInstant(Instant.ofEpochSecond(System.currentTimeMillis() / MILLI_SECONDS),
                TimeZone.getDefault().toZoneId())
                .toString()
                .replaceAll(":", "-");
    }

    default boolean isStoreFile(String fileName) {
        return true;
    }

    default boolean isReleaseFile(String fileName) {
        return true;
    }

    default boolean isInitReleaseVersion(int version) {
        return INIT_RELEASE_VERSION == version;
    }

    /**
     * Gets config info type
     * @return config info type for a configuration
     * @see ConfigInfoType
     */
    ConfigInfoType getConfigInfoType();
}
