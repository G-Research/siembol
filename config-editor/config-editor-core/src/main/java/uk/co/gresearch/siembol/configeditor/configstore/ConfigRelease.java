package uk.co.gresearch.siembol.configeditor.configstore;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.gresearch.siembol.configeditor.common.*;
import uk.co.gresearch.siembol.configeditor.git.GitRepository;
import uk.co.gresearch.siembol.configeditor.git.ReleasePullRequestService;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorFile;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult;
import uk.co.gresearch.siembol.configeditor.model.ErrorMessages;
import uk.co.gresearch.siembol.configeditor.model.ErrorResolutions;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult.StatusCode.OK;

public class ConfigRelease {
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final String SUBMIT_INIT_LOG_MSG = "User: {} trying to release {} version: {}";
    private static final String SUBMIT_COMPLETED_LOG_MSG = "Prepared {} PR in the branch name: {} PR: {}";
    private static final String CONFIG_IN_RELEASE = "Config %s is in the current release";
    private static final String NOT_INITIALISED_ERROR_MSG = "The release was not initialised";

    private final String directory;
    private final GitRepository gitRepository;
    private final ConfigInfoProvider configInfoProvider;
    private final ReleasePullRequestService pullRequestService;
    private final ConfigInfoType configType;
    private final AtomicReference<ConfigEditorResult> cacheResult;

    public ConfigRelease(GitRepository gitRepository,
                         ReleasePullRequestService pullRequestService,
                         ConfigInfoProvider configInfoProvider,
                         String directory) {
        this.directory = directory;
        this.gitRepository = gitRepository;
        this.configInfoProvider = configInfoProvider;
        this.pullRequestService = pullRequestService;
        this.configType = configInfoProvider.getConfigInfoType();
        this.cacheResult = new AtomicReference<>(
                ConfigEditorResult.fromMessage(ConfigEditorResult.StatusCode.ERROR, NOT_INITIALISED_ERROR_MSG));
    }

    public ConfigEditorResult getConfigsReleaseStatus() throws IOException {
        return pullRequestService.pendingPullRequest();
    }

    public void init() throws IOException, GitAPIException {
        var current = getConfigsRelease();
        if (current.getStatusCode() != OK) {
            throw new IllegalStateException(NOT_INITIALISED_ERROR_MSG);
        }
    }

    public ConfigEditorResult getConfigsReleaseFromCache() {
        var current = cacheResult.get();
        return new ConfigEditorResult(current.getStatusCode(), current.getAttributes());
    }

    public ConfigEditorResult getConfigsRelease() throws IOException, GitAPIException {
        ConfigEditorResult ret = gitRepository.getFiles(directory, configInfoProvider::isReleaseFile);
        if (ret.getStatusCode() != ConfigEditorResult.StatusCode.OK) {
            return ret;
        }

        int releaseVersion = configInfoProvider.getReleaseVersion(ret.getAttributes().getFiles());
        ret.getAttributes().setReleaseVersion(releaseVersion, configType);
        cacheResult.set(ret);
        return ret;
    }

    public ConfigEditorResult submitConfigsRelease(UserInfo user, String rulesRelease) throws Exception {
        ConfigInfo newReleaseInfo = configInfoProvider.getReleaseInfo(user, rulesRelease);
        LOG.info(SUBMIT_INIT_LOG_MSG, user.getUserName(), configType.getReleaseName(), newReleaseInfo.getVersion());

        ConfigEditorResult pullRequest = getConfigsReleaseStatus();
        if (pullRequest.getStatusCode() != ConfigEditorResult.StatusCode.OK) {
            return pullRequest;
        }

        if (pullRequest.getAttributes().getPendingPullRequest()) {
            var ret = ConfigEditorResult.fromMessage(ConfigEditorResult.StatusCode.BAD_REQUEST,
                    ErrorMessages.PR_PENDING.getMessage(
                            configType.getReleaseName(),
                            pullRequest.getAttributes().getPullRequestUrl()));
            ret.getAttributes().setErrorResolutionIfNotPresent(ErrorResolutions.CONCURRENT_USERS.getResolution());
            return ret;
        }

        ConfigEditorResult currentRelease = getConfigsRelease();
        if (currentRelease.getStatusCode() != ConfigEditorResult.StatusCode.OK) {
            return currentRelease;
        }

        if (currentRelease.getAttributes().getReleaseVersion(configType) != newReleaseInfo.getOldVersion()) {

            var ret = ConfigEditorResult.fromMessage(ConfigEditorResult.StatusCode.BAD_REQUEST,
                    ErrorMessages.PR_UNEXPECTED_VERSION.getMessage(
                            configType.getReleaseName(),
                            newReleaseInfo.getVersion(),
                            currentRelease.getAttributes().getReleaseVersion(configType) + 1));
            ret.getAttributes().setErrorResolutionIfNotPresent(ErrorResolutions.CONCURRENT_USERS.getResolution());
            return ret;
        }

        ConfigEditorResult createBranchResult = gitRepository.transactCopyAndCommit(newReleaseInfo,
                directory,
                configInfoProvider::isReleaseFile);
        if (createBranchResult.getStatusCode() != ConfigEditorResult.StatusCode.OK) {
            return createBranchResult;
        }

        ConfigEditorResult newPullRequestResult = pullRequestService.createPullRequest(newReleaseInfo);
        LOG.info(SUBMIT_COMPLETED_LOG_MSG,
                configType.getReleaseName(),
                newReleaseInfo.getBranchName(),
                newPullRequestResult.getAttributes().getPullRequestUrl());
        return newPullRequestResult;
    }

    public ConfigEditorResult checkConfigNotInRelease(String configName) throws GitAPIException, IOException {
        ConfigEditorResult releaseResult = getConfigsRelease();
        if (releaseResult.getStatusCode() != ConfigEditorResult.StatusCode.OK) {
            return releaseResult;
        }

        Optional<ConfigEditorFile> release = releaseResult.getAttributes().getFiles().stream().findFirst();
        if (release.isPresent() && configInfoProvider.isConfigInRelease(release.get().getContent(), configName)) {
            String message = String.format(CONFIG_IN_RELEASE, configName);
            return ConfigEditorResult.fromMessage(ConfigEditorResult.StatusCode.BAD_REQUEST, message);
        } else {
            return releaseResult;
        }
    }

    public String getRepoUri() {
        return gitRepository.getRepoUri();
    }

    public String getDirectoryUri() {
        return gitRepository.getDirectoryUrl(directory);
    }
}