package uk.co.gresearch.siembol.configeditor.configstore;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.gresearch.siembol.configeditor.common.ConfigInfoProvider;
import uk.co.gresearch.siembol.configeditor.common.UserInfo;
import uk.co.gresearch.siembol.configeditor.git.GitRepository;
import uk.co.gresearch.siembol.configeditor.model.*;
import uk.co.gresearch.siembol.configeditor.common.ConfigInfo;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult.StatusCode.BAD_REQUEST;
import static uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult.StatusCode.OK;

public class ConfigItems {
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final String INIT_START = "Trying Initialise a git repository: {}";
    private static final String INIT_COMPLETED = "Initialisation of a git repository completed";
    private static final String INIT_ERROR_MSG = "Problem during initialisation of config items";
    private static final String UPDATE_INIT_LOG_MSG = "User {} requested to add/update {} name: {} to version: {}";
    private static final String DELETE_FILES_LOG_MSG = "User {} requested to delete files: {}";
    private static final String UPDATE_COMPLETED_LOG_MSG = "{} name: {} to version: {} update completed";
    private static final String DELETE_COMMIT_MSG = "Deleted %s: %s";
    private static final String FILES_SEPARATOR = ",\n";

    private final String directory;
    private final GitRepository gitRepository;
    private final ConfigInfoProvider configInfoProvider;
    private final AtomicReference<Map<String, ConfigEditorFile>> filesCache = new AtomicReference<>();

    public ConfigItems(GitRepository gitRepository,
                       ConfigInfoProvider configInfoProvider,
                       String directory) {
        this.directory = directory;
        this.gitRepository = gitRepository;
        this.configInfoProvider = configInfoProvider;
    }

    private boolean checkVersion(ConfigInfo itemInfo) {
        String fileName = itemInfo.getFilesContent().keySet().stream().findFirst().get();
        if (filesCache.get().containsKey(fileName)) {
            ConfigInfo current = configInfoProvider.getConfigInfo(filesCache.get().get(fileName).getContent());
            return current.getOldVersion() == itemInfo.getOldVersion();
        } else {
            return itemInfo.isNewConfig();
        }
    }

    public void init() throws IOException, GitAPIException {
        LOG.info(INIT_START, gitRepository.getRepoUri());
        ConfigEditorResult result = gitRepository.getFiles(directory, configInfoProvider::isStoreFile);
        if (result.getStatusCode() != OK) {
            throw new IllegalStateException(INIT_ERROR_MSG);
        }

        updateCache(result.getAttributes().getFiles());
        LOG.info(INIT_COMPLETED);
    }

    public ConfigEditorResult getFiles() {
        List<ConfigEditorFile> files = new ArrayList<>(filesCache.get().values());
        ConfigEditorAttributes attributes = new ConfigEditorAttributes();
        attributes.setFiles(files);
        return new ConfigEditorResult(OK, attributes);
    }

    private void updateCache(List<ConfigEditorFile> files) {
        filesCache.set(files.stream().collect(Collectors.toMap(ConfigEditorFile::getFileName, x -> x)));
    }

    private ConfigEditorResult updateConfigItemInternally(UserInfo user,
                                                          String configItem,
                                                          boolean shouldBeNew) throws GitAPIException, IOException {
        ConfigInfo configInfo = configInfoProvider.getConfigInfo(user, configItem);
        LOG.info(UPDATE_INIT_LOG_MSG,
                user.getUserName(),
                configInfo.getConfigInfoType().getSingular(),
                configInfo.getName(),
                configInfo.getVersion());

        if (shouldBeNew != configInfo.isNewConfig()
                || !checkVersion(configInfo)) {
            String msg = shouldBeNew ? ErrorMessages.CONFIG_ITEM_ALREADY_EXISTS.getMessage(configInfo.getName())
                    : ErrorMessages.CONFIG_ITEM_UNEXPECTED_VERSION.getMessage(configInfo.getName());
            LOG.info(msg);
            var result = ConfigEditorResult.fromMessage(BAD_REQUEST, msg);
            result.getAttributes().setErrorResolutionIfNotPresent(ErrorResolution.CONCURENT_USERS.getResolution());
            return result;
        }

        ConfigEditorResult result = gitRepository.transactCopyAndCommit(configInfo,
                directory,
                configInfoProvider::isStoreFile);
        if (result.getStatusCode() == OK) {
            updateCache(result.getAttributes().getFiles());
            LOG.info(UPDATE_COMPLETED_LOG_MSG,
                    configInfo.getConfigInfoType().getSingular(),
                    configInfo.getName(),
                    configInfo.getVersion());
        }
        return result;
    }

    public ConfigEditorResult deleteItems(UserInfo user, String prefixItemName) throws GitAPIException, IOException {
        Map<String, Optional<String>> filesToDelete = filesCache.get().values().stream()
                .filter(x -> x.getFileName().startsWith(prefixItemName))
                .collect(Collectors.toMap(ConfigEditorFile::getFileName, x -> Optional.empty()));

        if (!filesToDelete.isEmpty()) {
            List<String> files = new ArrayList<>(filesToDelete.keySet());
            files.sort(Comparator.naturalOrder());
            String filesString =  StringUtils.join(files, FILES_SEPARATOR);
            LOG.info(DELETE_FILES_LOG_MSG, user.getUserName(), filesString);

            ConfigInfo configInfo = configInfoProvider.configInfoFromUser(user);
            String commitMessage = String.format(DELETE_COMMIT_MSG,
                    files.size() == 1
                            ? configInfoProvider.getConfigInfoType().getSingular()
                            : configInfoProvider.getConfigInfoType().getPlural(),
                    filesString);
            configInfo.setCommitMessage(commitMessage);

            configInfo.setFilesContent(filesToDelete);
            ConfigEditorResult deleteResult = gitRepository.transactCopyAndCommit(configInfo,
                    directory, configInfoProvider::isStoreFile);
            if (deleteResult.getStatusCode() != OK) {
                return deleteResult;
            }
            updateCache(deleteResult.getAttributes().getFiles());
        }

        return getFiles();
    }

    public ConfigEditorResult updateConfigItem(UserInfo user, String configItem) throws GitAPIException, IOException {
        return updateConfigItemInternally(user, configItem, false);
    }

    public ConfigEditorResult addConfigItem(UserInfo user, String configItem) throws GitAPIException, IOException {
        return updateConfigItemInternally(user, configItem, true);
    }

    public String getRepoUri() {
        return gitRepository.getRepoUri();
    }

    public String getDirectoryUri() {
        return gitRepository.getDirectoryUrl(directory);
    }
}
