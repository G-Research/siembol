package uk.co.gresearch.siembol.configeditor.configstore;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import uk.co.gresearch.siembol.configeditor.common.ConfigInfo;
import uk.co.gresearch.siembol.configeditor.common.ConfigInfoProvider;
import uk.co.gresearch.siembol.configeditor.common.ConfigInfoType;
import uk.co.gresearch.siembol.configeditor.common.UserInfo;
import uk.co.gresearch.siembol.configeditor.git.GitRepository;
import uk.co.gresearch.siembol.configeditor.git.ReleasePullRequestService;
import uk.co.gresearch.siembol.configeditor.model.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;
import static uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult.StatusCode.BAD_REQUEST;
import static uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult.StatusCode.ERROR;
import static uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult.StatusCode.OK;

public class ConfigReleaseTest {
    private GitRepository gitRepo;
    private String directory = "tmp";
    private String dummyRepoUrl = "url";
    private String dummyDirectoryRepoUrl = "directory url";

    private ReleasePullRequestService pullRequestService;
    private ConfigInfoProvider configInfoProvider;
    private ConfigRelease configRelease;

    private Map<String, String> filesContent = new HashMap<>();

    private List<ConfigEditorFile> files;
    private ConfigEditorResult getFilesResult;
    private ConfigInfo releaseInfo;
    private UserInfo user;
    private ConfigEditorResult pullRequestResult;
    private Integer releaseVersion = 10;
    @Before
    public void setUp() throws IOException, GitAPIException {
        gitRepo = Mockito.mock(GitRepository.class);
        pullRequestService = Mockito.mock(ReleasePullRequestService.class);
        configInfoProvider = Mockito.spy(ConfigInfoProvider.class);

        filesContent.put("File.json", "DUMMY_CONTENT");
        files = new ArrayList<>();
        files.add(new ConfigEditorFile("File.json",
                "DUMMY_CONTENT",
                ConfigEditorFile.ContentType.RAW_JSON_STRING));
        ConfigEditorAttributes attr = new ConfigEditorAttributes();
        attr.setFiles(files);
        getFilesResult = new ConfigEditorResult(OK, attr);

        releaseInfo = new ConfigInfo();

        when(configInfoProvider.getReleaseInfo(any(), any())).thenReturn(releaseInfo);
        when(configInfoProvider.getReleaseVersion(files)).thenReturn(releaseVersion);
        when(configInfoProvider.isReleaseFile(any())).thenReturn(true);
        when(configInfoProvider.getFileContentType()).thenReturn(ConfigEditorFile.ContentType.STRING);
        when(configInfoProvider.getConfigInfoType()).thenReturn(ConfigInfoType.CONFIG);

        when(gitRepo.getFiles(eq(directory), any())).thenReturn(getFilesResult);
        when(gitRepo.getRepoUri()).thenReturn(dummyRepoUrl);
        when(gitRepo.getDirectoryUrl(eq(directory))).thenReturn(dummyDirectoryRepoUrl);


        when(gitRepo.transactCopyAndCommit(eq(releaseInfo), eq(directory), any())).thenReturn(getFilesResult);

        configRelease = new ConfigRelease(gitRepo, pullRequestService, configInfoProvider, directory);
        user = new UserInfo();
        user.setUserName("john");
        user.setUserName("john@secret");

        ConfigEditorAttributes attributes = new ConfigEditorAttributes();
        attributes.setPendingPullRequest(true);
        attributes.setPullRequestUrl("DUMMY_URL");
        pullRequestResult = new ConfigEditorResult(ConfigEditorResult.StatusCode.OK, attributes);
        when(pullRequestService.pendingPullRequest()).thenReturn(pullRequestResult);
        when(pullRequestService.createPullRequest(eq(releaseInfo))).thenReturn(pullRequestResult);
    }

    @Test
    public void getRepoUrl() {
        String repoUrl = configRelease.getRepoUri();
        Assert.assertEquals(dummyRepoUrl, repoUrl);
        verify(gitRepo, times(1)).getRepoUri();
    }

    @Test
    public void getRepoDirectoryUrl() {
        String directoryUrl = configRelease.getDirectoryUri();
        Assert.assertEquals(directoryUrl, dummyDirectoryRepoUrl);
        verify(gitRepo, times(1)).getDirectoryUrl(directory);
    }

    @Test
    public void getRulesReleaseStatusPending() throws IOException {
        ConfigEditorResult ret = configRelease.getConfigsReleaseStatus();
        verify(pullRequestService, times(1)).pendingPullRequest();
        Assert.assertEquals(ConfigEditorResult.StatusCode.OK, ret.getStatusCode());
        Assert.assertTrue(ret.getAttributes().getPendingPullRequest());
        Assert.assertEquals("DUMMY_URL", ret.getAttributes().getPullRequestUrl());
    }

    @Test
    public void getRelease() throws IOException, GitAPIException {
        ConfigEditorResult result = configRelease.getConfigsRelease();
        Assert.assertEquals(OK, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes().getFiles());
        Assert.assertEquals(releaseVersion, result.getAttributes().getRulesVersion());
    }

    @Test
    public void submitReleasePendingPr() throws Exception {
        ConfigEditorResult result = configRelease.submitConfigsRelease(user, "NEW_DUMMY_RELEASE");
        Assert.assertEquals(BAD_REQUEST, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes().getMessage());
    }

    @Test
    public void submitReleasePrServiceError() throws Exception {
        when(pullRequestService.pendingPullRequest())
                .thenReturn(ConfigEditorResult.fromMessage(ERROR, "error in pr service"));
        ConfigEditorResult result = configRelease.submitConfigsRelease(user, "NEW_DUMMY_RELEASE");
        Assert.assertEquals(ERROR, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes().getMessage());
    }

    @Test
    public void submitReleaseOk() throws Exception {
        pullRequestResult.getAttributes().setPendingPullRequest(false);
        releaseInfo.setOldVersion(releaseVersion);
        releaseInfo.setVersion(releaseVersion + 1);

        ConfigEditorResult result = configRelease.submitConfigsRelease(user, "NEW_DUMMY_RELEASE");
        Assert.assertEquals(OK, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes().getPullRequestUrl());
    }

    @Test
    public void submitAdminConfigOk() throws Exception {
        when(configInfoProvider.getConfigInfoType()).thenReturn(ConfigInfoType.ADMIN_CONFIG);
        pullRequestResult.getAttributes().setPendingPullRequest(false);
        releaseInfo.setOldVersion(releaseVersion);
        releaseInfo.setVersion(releaseVersion + 1);

        ConfigEditorResult result = configRelease.submitConfigsRelease(user, "NEW_DUMMY_RELEASE");
        Assert.assertEquals(OK, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes().getPullRequestUrl());
    }

    @Test
    public void sumbitReleaseWrongVersion() throws Exception {
        pullRequestResult.getAttributes().setPendingPullRequest(false);
        releaseInfo.setOldVersion(releaseVersion - 1);
        releaseInfo.setVersion(releaseVersion);

        ConfigEditorResult result = configRelease.submitConfigsRelease(user, "NEW_DUMMY_RELEASE");
        Assert.assertEquals(BAD_REQUEST, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes().getMessage());
    }
}
