package uk.co.gresearch.siembol.configeditor.configstore;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.gresearch.siembol.configeditor.common.ConfigInfoProvider;
import uk.co.gresearch.siembol.configeditor.common.UserInfo;
import uk.co.gresearch.siembol.configeditor.git.GitRepository;
import uk.co.gresearch.siembol.configeditor.git.ReleasePullRequestService;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult;
import uk.co.gresearch.siembol.configeditor.common.ConfigInfo;

import java.io.IOException;
import java.lang.invoke.MethodHandles;

public class ConfigRelease {
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final String SUBMIT_INIT_LOG_MSG = "User: {} trying to release version: {}";
    private static final String PENDING_PR_ERROR_MSG = "Can not release config release because PR %s is pending";
    private static final String WRONG_VERSION_ERROR_MSG = "Can not release version %d from version %d";
    private static final String SUBMIT_COMPLETED_LOG_MSG = "Prepared PR in the branch name: {} PR: {}";

    private final String directory;
    private final GitRepository gitRepository;
    private final ConfigInfoProvider configInfoProvider;
    private final ReleasePullRequestService pullRequestService;

    public ConfigRelease(GitRepository gitRepository,
                         ReleasePullRequestService pullRequestService,
                         ConfigInfoProvider configInfoProvider,
                         String directory) {
        this.directory = directory;
        this.gitRepository = gitRepository;
        this.configInfoProvider = configInfoProvider;
        this.pullRequestService = pullRequestService;
    }

    public ConfigEditorResult getConfigsReleaseStatus() throws IOException {
        return pullRequestService.pendingPullRequest();
    }

    public ConfigEditorResult getConfigsRelease() throws IOException, GitAPIException {
        ConfigEditorResult ret = gitRepository.getFiles(directory, configInfoProvider::isReleaseFile);
        if (ret.getStatusCode() != ConfigEditorResult.StatusCode.OK) {
            return ret;
        }

        int rulesVersion = configInfoProvider.getReleaseVersion(ret.getAttributes().getFiles());
        ret.getAttributes().setRulesVersion(rulesVersion);
        return ret;
    }

    public ConfigEditorResult submitConfigsRelease(UserInfo user, String rulesRelease) throws Exception {
        ConfigInfo newReleaseInfo = configInfoProvider.getReleaseInfo(user, rulesRelease);
        LOG.info(SUBMIT_INIT_LOG_MSG, user.getUserName(), newReleaseInfo.getVersion());

        ConfigEditorResult pullRequest = getConfigsReleaseStatus();
        if (pullRequest.getStatusCode() != ConfigEditorResult.StatusCode.OK) {
            return pullRequest;
        }

        if (pullRequest.getAttributes().getPendingPullRequest()) {
            return ConfigEditorResult.fromMessage(ConfigEditorResult.StatusCode.BAD_REQUEST,
                    String.format(PENDING_PR_ERROR_MSG, pullRequest.getAttributes().getPullRequestUrl()));
        }

        ConfigEditorResult currentRelease = getConfigsRelease();
        if (currentRelease.getStatusCode() != ConfigEditorResult.StatusCode.OK) {
            return currentRelease;
        }

        if (currentRelease.getAttributes().getRulesVersion() != newReleaseInfo.getOldVersion()) {
            return ConfigEditorResult.fromMessage(ConfigEditorResult.StatusCode.BAD_REQUEST,
                    String.format(WRONG_VERSION_ERROR_MSG,
                            newReleaseInfo.getVersion(),
                            currentRelease.getAttributes().getRulesVersion()));
        }

        ConfigEditorResult createBranchResult = gitRepository.transactCopyAndCommit(newReleaseInfo,
                directory,
                configInfoProvider::isStoreFile);
        if (createBranchResult.getStatusCode() != ConfigEditorResult.StatusCode.OK) {
            return createBranchResult;
        }

        ConfigEditorResult newPullRequestResult = pullRequestService.createPullRequest(newReleaseInfo);
        LOG.info(SUBMIT_COMPLETED_LOG_MSG,
                newReleaseInfo.getBranchName(),
                newPullRequestResult.getAttributes().getPullRequestUrl());
        return newPullRequestResult;
    }

    public String getRepoUri() {
        return gitRepository.getRepoUri();
    }

    public String getDirectoryUri() {
        return gitRepository.getDirectoryUrl(directory);
    }
}