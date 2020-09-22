package uk.co.gresearch.siembol.configeditor.configstore;
import uk.co.gresearch.siembol.configeditor.common.UserInfo;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorFile;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.TimeZone;

public interface ConfigInfoProvider {
    String RELEASE_BRANCH_TEMPLATE = "release_%d_by_%s_on_%s";
    String MISSING_ARGUMENTS_MSG = "missing user info attributes";
    int MILLI_SECONDS = 1000;

    ConfigInfo getConfigInfo(UserInfo user, String config);

    ConfigInfo getReleaseInfo(UserInfo user, String release);

    int getReleaseVersion(List<ConfigEditorFile> files);

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

    default boolean isStoreFile(String filename) {
        return true;
    }

    default boolean isReleaseFile(String filename) {
        return true;
    }
}
