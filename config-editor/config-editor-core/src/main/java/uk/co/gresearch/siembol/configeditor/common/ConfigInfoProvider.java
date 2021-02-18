package uk.co.gresearch.siembol.configeditor.common;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorFile;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.TimeZone;

public interface ConfigInfoProvider {
    String RELEASE_BRANCH_TEMPLATE = "ver_%d_by_%s_on_%s";
    String MISSING_ARGUMENTS_MSG = "missing user info attributes";
    String UNKNOWN_USER_INFO = "unknown";
    int MILLI_SECONDS = 1000;

    ConfigInfo getConfigInfo(UserInfo user, String config);

    default ConfigInfo getConfigInfo(String config) {
        UserInfo unknownUser = new UserInfo();
        unknownUser.setUserName(UNKNOWN_USER_INFO);
        unknownUser.setEmail(UNKNOWN_USER_INFO);
        return getConfigInfo(unknownUser, config);
    }

    ConfigInfo getReleaseInfo(UserInfo user, String release);

    int getReleaseVersion(List<ConfigEditorFile> files);

    int getReleaseVersion(String content);

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

    ConfigInfoType getConfigInfoType();
}
