package uk.co.gresearch.nortem.configeditor.configstore;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import uk.co.gresearch.nortem.configeditor.model.ConfigEditorAttributes;
import uk.co.gresearch.nortem.configeditor.model.ConfigEditorFile;
import uk.co.gresearch.nortem.configeditor.model.ConfigEditorResult;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ConfigStoreImplTest {
    private GitRepository gitRulesRepo;
    private GitRepository gitReleasesRepo;
    private ReleasePullRequestService pullRequestService;
    private ConfigInfoProvider ruleInfoProvider;
    private ConfigStoreImpl ruleStore;
    private Map<String, String> filesContent = new HashMap<>();
    private List<ConfigEditorFile> files;
    private ConfigEditorResult getFilesResult;
    private ConfigInfo ruleInfo = new ConfigInfo();

    @Before
    public void setUp() throws IOException, GitAPIException {
        gitRulesRepo = Mockito.mock(GitRepository.class);
        gitReleasesRepo = Mockito.mock(GitRepository.class);
        pullRequestService = Mockito.mock(ReleasePullRequestService.class);
        ruleInfoProvider = Mockito.mock(ConfigInfoProvider.class);

        when(ruleInfoProvider.getConfigInfo(any(), any())).thenReturn(ruleInfo);
        when(ruleInfoProvider.getReleaseInfo(any(), any())).thenReturn(ruleInfo);
        when(ruleInfoProvider.isReleaseFile(any())).thenReturn(true);
        when(ruleInfoProvider.isStoreFile(any())).thenReturn(true);
        when(ruleInfoProvider.getFileContentType()).thenReturn(ConfigEditorFile.ContentType.STRING);

        filesContent.put("File.json", "DUMMY_CONTENT");
        files = new ArrayList<>();
        files.add(new ConfigEditorFile("File.json",
                "DUMMY_CONTENT",
                ConfigEditorFile.ContentType.RAW_JSON_STRING));
        ConfigEditorAttributes attr = new ConfigEditorAttributes();
        attr.setFiles(files);
        getFilesResult = new ConfigEditorResult(ConfigEditorResult.StatusCode.OK, attr);

        when(gitRulesRepo.getFiles()).thenReturn(getFilesResult);
        when(gitReleasesRepo.getFiles()).thenReturn(getFilesResult);
        when(gitRulesRepo.transactCopyAndCommit(any())).thenReturn(getFilesResult);

        ruleStore = new ConfigStoreImpl(gitRulesRepo,
                gitReleasesRepo,
                pullRequestService,
                ruleInfoProvider);
    }

    @After
    public void tearDown() {
        ruleStore.close();
    }

    @Test
    public void AddRuleOK() throws GitAPIException, IOException {
        ruleInfo.setOldVersion(0);
        ruleInfo.setFilesContent(new HashMap<>());
        ConfigEditorResult ret = ruleStore.addConfig("john", "NEW");
        verify(ruleInfoProvider).getConfigInfo("john", "NEW");
        verify(gitRulesRepo).transactCopyAndCommit(ruleInfo);
        Assert.assertEquals(ConfigEditorResult.StatusCode.OK, ret.getStatusCode());
        Assert.assertEquals(1, ret.getAttributes().getFiles().size());
        Assert.assertEquals("File.json", ret.getAttributes().getFiles().get(0).getFileName());
        Assert.assertEquals("DUMMY_CONTENT", ret.getAttributes().getFiles().get(0).getContentValue());
    }

    @Test
    public void AddRuleNotNew() {
        ruleInfo.setOldVersion(1);
        ruleInfo.setFilesContent(new HashMap<>());
        ConfigEditorResult ret = ruleStore.addConfig("john", "NEW");
        verify(ruleInfoProvider).getConfigInfo("john", "NEW");
        Assert.assertEquals(ConfigEditorResult.StatusCode.BAD_REQUEST, ret.getStatusCode());
        Assert.assertTrue(ret.getAttributes().getMessage().contains("wrong version"));
    }

    @Test
    public void AddRuleExisting() {
        ruleInfo.setOldVersion(0);
        ruleInfo.setFilesContent(filesContent);
        ConfigEditorResult ret = ruleStore.addConfig("john", "NEW");
        verify(ruleInfoProvider).getConfigInfo("john", "NEW");
        Assert.assertEquals(ConfigEditorResult.StatusCode.BAD_REQUEST, ret.getStatusCode());
        Assert.assertTrue(ret.getAttributes().getMessage().contains("already exists"));
    }

    @Test
    public void UpdateRuleOK() throws GitAPIException, IOException {
        ruleInfo.setOldVersion(1);
        ruleInfo.setFilesContent(filesContent);
        ConfigEditorResult ret = ruleStore.updateConfig("john", "UPDATE");

        verify(ruleInfoProvider).getConfigInfo("john", "UPDATE");
        verify(gitRulesRepo).transactCopyAndCommit(ruleInfo);

        Assert.assertEquals(ConfigEditorResult.StatusCode.OK, ret.getStatusCode());
        Assert.assertEquals(1, ret.getAttributes().getFiles().size());
        Assert.assertEquals("File.json", ret.getAttributes().getFiles().get(0).getFileName());
        Assert.assertEquals("DUMMY_CONTENT", ret.getAttributes().getFiles().get(0).getContentValue());
    }

    @Test
    public void UpdateNew() {
        ruleInfo.setOldVersion(0);
        ruleInfo.setFilesContent(filesContent);
        ConfigEditorResult ret = ruleStore.updateConfig("john", "UPDATE");
        Assert.assertEquals(ConfigEditorResult.StatusCode.BAD_REQUEST, ret.getStatusCode());
        Assert.assertTrue(ret.getAttributes().getMessage().contains("wrong version"));
    }

    @Test
    public void UpdateNotExist() {
        ruleInfo.setOldVersion(0);
        ruleInfo.setFilesContent(new HashMap<>());
        ConfigEditorResult ret = ruleStore.updateConfig("john", "NEW");
        Assert.assertEquals(ConfigEditorResult.StatusCode.BAD_REQUEST, ret.getStatusCode());
        Assert.assertTrue(ret.getAttributes().getMessage().contains("does not exist"));
    }

    @Test
    public void getRules() {
        ConfigEditorResult ret = ruleStore.getConfigs();
        verify(ruleInfoProvider).isStoreFile("File.json");
        Assert.assertEquals(ConfigEditorResult.StatusCode.OK, ret.getStatusCode());
        Assert.assertEquals(1, ret.getAttributes().getFiles().size());
        Assert.assertEquals("File.json", ret.getAttributes().getFiles().get(0).getFileName());
        Assert.assertEquals("DUMMY_CONTENT", ret.getAttributes().getFiles().get(0).getContentValue());
    }

    @Test
    public void getRulesFiltered() throws IOException, GitAPIException {
        when(ruleInfoProvider.isStoreFile(any())).thenReturn(false);
        ruleStore = new ConfigStoreImpl(gitRulesRepo,
                gitReleasesRepo,
                pullRequestService,
                ruleInfoProvider);
        ConfigEditorResult ret = ruleStore.getConfigs();
        verify(ruleInfoProvider, times(2)).isStoreFile("File.json");
        Assert.assertEquals(ConfigEditorResult.StatusCode.ERROR, ret.getStatusCode());
    }

    @Test
    public void getRulesRelease() throws IOException, GitAPIException {
        when(ruleInfoProvider.getReleaseVersion(any())).thenReturn(1234);
        ConfigEditorResult ret = ruleStore.getConfigsRelease();
        verify(gitReleasesRepo).getFiles();
        verify(ruleInfoProvider).isReleaseFile("File.json");
        verify(ruleInfoProvider).getReleaseVersion(files);
        Assert.assertEquals(ConfigEditorResult.StatusCode.OK, ret.getStatusCode());
        Assert.assertEquals(1, ret.getAttributes().getFiles().size());
        Assert.assertEquals("File.json", ret.getAttributes().getFiles().get(0).getFileName());
        Assert.assertEquals("DUMMY_CONTENT", ret.getAttributes().getFiles().get(0).getContentValue());
        Assert.assertEquals(1234, ret.getAttributes().getRulesVersion().intValue());
    }

    @Test
    public void getRulesReleaseFiltered() throws IOException, GitAPIException {
        when(ruleInfoProvider.isReleaseFile(any())).thenReturn(false);
        ConfigEditorResult ret = ruleStore.getConfigsRelease();
        verify(gitReleasesRepo).getFiles();
        verify(ruleInfoProvider).isReleaseFile("File.json");
        Assert.assertEquals(ConfigEditorResult.StatusCode.ERROR, ret.getStatusCode());
    }

    @Test
    public void getRulesReleaseStatusPending() throws IOException {
        ConfigEditorAttributes attr = new ConfigEditorAttributes();
        attr.setPendingPullRequest(true);
        attr.setPullRequestUrl("DUMMY_URL");
        ConfigEditorResult pullRequestResult = new ConfigEditorResult(ConfigEditorResult.StatusCode.OK, attr);
        when(pullRequestService.pendingPullRequest()).thenReturn(pullRequestResult);
        ConfigEditorResult ret = ruleStore.getConfigsReleaseStatus();

        verify(pullRequestService).pendingPullRequest();
        Assert.assertEquals(ConfigEditorResult.StatusCode.OK, ret.getStatusCode());
        Assert.assertTrue(ret.getAttributes().getPendingPullRequest());
        Assert.assertEquals("DUMMY_URL", ret.getAttributes().getPullRequestUrl());
    }

    @Test
    public void getRulesReleaseStatusNoPullRequest() throws IOException {
        ConfigEditorAttributes attr = new ConfigEditorAttributes();
        attr.setPendingPullRequest(false);
        ConfigEditorResult pullRequestResult = new ConfigEditorResult(ConfigEditorResult.StatusCode.OK, attr);
        when(pullRequestService.pendingPullRequest()).thenReturn(pullRequestResult);

        ConfigEditorResult ret = ruleStore.getConfigsReleaseStatus();

        verify(pullRequestService).pendingPullRequest();
        Assert.assertEquals(ConfigEditorResult.StatusCode.OK, ret.getStatusCode());
        Assert.assertFalse(ret.getAttributes().getPendingPullRequest());
    }

    @Test
    public void submitRulesRelease() throws IOException, GitAPIException {
        ConfigEditorAttributes attr = new ConfigEditorAttributes();
        attr.setPendingPullRequest(true);
        attr.setPullRequestUrl("DUMMY_URL");
        ConfigEditorResult pullRequestResult = new ConfigEditorResult(ConfigEditorResult.StatusCode.OK, attr);

        attr.setPendingPullRequest(false);
        ConfigEditorResult pendingPullRequestResult = new ConfigEditorResult(ConfigEditorResult.StatusCode.OK, attr);

        when(pullRequestService.createPullRequest(ruleInfo)).thenReturn(pullRequestResult);
        when(pullRequestService.pendingPullRequest()).thenReturn(pendingPullRequestResult);

        ConfigEditorResult ret = ruleStore.submitConfigsRelease("test", "dummy_rules");

        verify(ruleInfoProvider).getReleaseInfo("test", "dummy_rules");
        verify(gitReleasesRepo).transactCopyAndCommit(ruleInfo);
        verify(pullRequestService).createPullRequest(ruleInfo);
        verify(pullRequestService).pendingPullRequest();

        Assert.assertEquals(ConfigEditorResult.StatusCode.OK, ret.getStatusCode());
        Assert.assertEquals("DUMMY_URL", ret.getAttributes().getPullRequestUrl());
    }

    @Test
    public void submitRulesReleasePending() throws IOException {
        ConfigEditorAttributes attr = new ConfigEditorAttributes();
        attr.setPendingPullRequest(true);
        attr.setPullRequestUrl("DUMMY_URL");
        ConfigEditorResult pendingPullRequestResult = new ConfigEditorResult(ConfigEditorResult.StatusCode.OK, attr);
        when(pullRequestService.pendingPullRequest()).thenReturn(pendingPullRequestResult);

        ConfigEditorResult ret = ruleStore.submitConfigsRelease("test", "dummy_rules");

        verify(ruleInfoProvider).getReleaseInfo("test", "dummy_rules");
        verify(pullRequestService).pendingPullRequest();

        Assert.assertEquals(ConfigEditorResult.StatusCode.BAD_REQUEST, ret.getStatusCode());
        Assert.assertTrue(ret.getAttributes().getPendingPullRequest());
        Assert.assertEquals("DUMMY_URL", ret.getAttributes().getPullRequestUrl());
    }

    @Test
    public void getRepositoriesTest() {
        when(gitRulesRepo.getRepoUri()).thenReturn("RULES_URI");
        when(gitReleasesRepo.getRepoUri()).thenReturn("RELEASE_URI");
        ConfigEditorResult ret = ruleStore.getRepositories();

        Assert.assertEquals(ConfigEditorResult.StatusCode.OK, ret.getStatusCode());

        Assert.assertNotNull(ret.getAttributes().getRulesRepositories());
        Assert.assertEquals("RULES_URI", ret.getAttributes()
                .getRulesRepositories().getRuleStoreUrl());
        Assert.assertEquals("RELEASE_URI",
                ret.getAttributes().getRulesRepositories().getRulesReleaseUrl());
    }
}
