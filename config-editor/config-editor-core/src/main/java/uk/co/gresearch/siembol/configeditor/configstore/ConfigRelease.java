package uk.co.gresearch.siembol.configeditor.configstore;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.gresearch.siembol.configeditor.common.*;
import uk.co.gresearch.siembol.configeditor.git.GitRepository;
import uk.co.gresearch.siembol.configeditor.git.ReleasePullRequestService;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult;

import java.io.IOException;
import java.lang.invoke.MethodHandles;

public class ConfigRelease {
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final String SUBMIT_INIT_LOG_MSG = "User: {} trying to release {} version: {}";
    private static final String PENDING_PR_ERROR_MSG = "Can not release %s because PR %s is pending";
    private static final String WRONG_VERSION_ERROR_MSG = "Can not release %s version %d from version %d";
    private static final String SUBMIT_COMPLETED_LOG_MSG = "Prepared {} PR in the branch name: {} PR: {}";
    private static final String CONFIG_IN_RELEASE= "Config %s in in the current release";

    private final String directory;
    private final GitRepository gitRepository;
    private final ConfigInfoProvider configInfoProvider;
    private final ReleasePullRequestService pullRequestService;
    private final ConfigInfoType configType;

    public ConfigRelease(GitRepository gitRepository,
                         ReleasePullRequestService pullRequestService,
                         ConfigInfoProvider configInfoProvider,
                         String directory) {
        this.directory = directory;
        this.gitRepository = gitRepository;
        this.configInfoProvider = configInfoProvider;
        this.pullRequestService = pullRequestService;
        this.configType = configInfoProvider.getConfigInfoType();
    }

    public ConfigEditorResult getConfigsReleaseStatus() throws IOException {
        return pullRequestService.pendingPullRequest();
    }

    public ConfigEditorResult getConfigsRelease() throws IOException, GitAPIException {
        ConfigEditorResult ret = gitRepository.getFiles(directory, configInfoProvider::isReleaseFile);
        if (ret.getStatusCode() != ConfigEditorResult.StatusCode.OK) {
            return ret;
        }

        int releaseVersion = configInfoProvider.getReleaseVersion(ret.getAttributes().getFiles());
        ret.getAttributes().setReleaseVersion(releaseVersion, configType);
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
            return ConfigEditorResult.fromMessage(ConfigEditorResult.StatusCode.BAD_REQUEST,
                    String.format(PENDING_PR_ERROR_MSG,
                            configType.getReleaseName(),
                            pullRequest.getAttributes().getPullRequestUrl()));
        }

        ConfigEditorResult currentRelease = getConfigsRelease();
        if (currentRelease.getStatusCode() != ConfigEditorResult.StatusCode.OK) {
            return currentRelease;
        }

        if (currentRelease.getAttributes().getReleaseVersion(configType) != newReleaseInfo.getOldVersion()) {
            return ConfigEditorResult.fromMessage(ConfigEditorResult.StatusCode.BAD_REQUEST,
                    String.format(WRONG_VERSION_ERROR_MSG,
                            configType.getReleaseName(),
                            newReleaseInfo.getVersion(),
                            currentRelease.getAttributes().getReleaseVersion(configType)));
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

        String release = releaseResult.getAttributes().getFiles().stream().findFirst().get().getContent();
        if (configInfoProvider.isConfigInRelease(release, configName)) {
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