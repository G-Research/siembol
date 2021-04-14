package uk.co.gresearch.siembol.configeditor.configstore;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import org.junit.Before;
import uk.co.gresearch.siembol.configeditor.common.ConfigInfo;
import uk.co.gresearch.siembol.configeditor.common.ConfigInfoProvider;
import uk.co.gresearch.siembol.configeditor.common.ConfigInfoType;
import uk.co.gresearch.siembol.configeditor.common.UserInfo;
import uk.co.gresearch.siembol.configeditor.git.GitRepository;
import uk.co.gresearch.siembol.configeditor.model.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;
import static uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult.StatusCode.BAD_REQUEST;
import static uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult.StatusCode.OK;

public class ConfigItemsTest {
    private GitRepository gitRepo;
    private String directory = "tmp";
    private String dummyRepoUrl = "url";
    private String dummyDirectoryRepoUrl = "directory url";

    private ConfigInfoProvider configInfoProvider;
    private ConfigItems configItems;

    private Map<String, String> filesContent = new HashMap<>();

    private List<ConfigEditorFile> files;
    private List<ConfigEditorFile> transactFiles;
    private ConfigEditorResult getFilesResult;
    private ConfigEditorResult transactGetFilesResult;
    private ConfigInfo configInfo = new ConfigInfo();
    private UserInfo user;

    @Before
    public void setUp() throws IOException, GitAPIException {
        gitRepo = Mockito.mock(GitRepository.class);

        configInfoProvider = Mockito.spy(ConfigInfoProvider.class);

        configInfo.setName("test");
        configInfo.setVersion(1);
        when(configInfoProvider.getConfigInfo(any(UserInfo.class), anyString())).thenReturn(configInfo);

        when(configInfoProvider.isStoreFile(anyString())).thenReturn(true);
        when(configInfoProvider.getFileContentType()).thenReturn(ConfigEditorFile.ContentType.STRING);
        filesContent.put("File.json", "DUMMY_CONTENT");
        files = new ArrayList<>();
        files.add(new ConfigEditorFile("File.json",
                "DUMMY_CONTENT",
                ConfigEditorFile.ContentType.RAW_JSON_STRING));
        ConfigEditorAttributes attr = new ConfigEditorAttributes();
        attr.setFiles(files);
        getFilesResult = new ConfigEditorResult(OK, attr);

        when(gitRepo.getFiles(eq(directory), ArgumentMatchers.<Function<String, Boolean>>any()))
                .thenReturn(getFilesResult);
        when(gitRepo.getRepoUri()).thenReturn(dummyRepoUrl);
        when(gitRepo.getDirectoryUrl(eq(directory))).thenReturn(dummyDirectoryRepoUrl);

        transactFiles = new ArrayList<>();
        transactFiles.add(new ConfigEditorFile("Updated.json",
                "DUMMY_CONTENT_UPDATED",
                ConfigEditorFile.ContentType.RAW_JSON_STRING));
        ConfigEditorAttributes attrTransact = new ConfigEditorAttributes();
        attrTransact.setFiles(transactFiles);
        transactGetFilesResult = new ConfigEditorResult(OK, attrTransact);
        when(gitRepo.transactCopyAndCommit(any(ConfigInfo.class),
                eq(directory),
                ArgumentMatchers.<Function<String, Boolean>>any()))
                .thenReturn(transactGetFilesResult);

        configItems = new ConfigItems(gitRepo, configInfoProvider, directory);
        user = new UserInfo();
        user.setUserName("john");
        user.setUserName("john@secret");
    }

    @Test
    public void getRepoUrl() {
        String repoUrl = configItems.getRepoUri();
        Assert.assertEquals(dummyRepoUrl, repoUrl);
        verify(gitRepo, times(1)).getRepoUri();
    }

    @Test
    public void getRepoDirectoryUrl() {
        String directoryUrl = configItems.getDirectoryUri();
        Assert.assertEquals(directoryUrl, dummyDirectoryRepoUrl);
        verify(gitRepo, times(1)).getDirectoryUrl(directory);
    }

    @Test
    public void initOK() throws IOException, GitAPIException {
        configItems.init();
        verify(gitRepo, times(1))
                .getFiles(eq(directory), ArgumentMatchers.<Function<String, Boolean>>any());
        ConfigEditorResult result = configItems.getFiles();
        Assert.assertEquals(OK, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes().getFiles());
        Assert.assertEquals(1, result.getAttributes().getFiles().size());
        Assert.assertEquals(1, result.getAttributes().getFiles().size());
        Assert.assertEquals("File.json", result.getAttributes().getFiles().get(0).getFileName());
        Assert.assertEquals("DUMMY_CONTENT", result.getAttributes().getFiles().get(0).getContent());
    }

    @Test(expected = IllegalStateException.class)
    public void initError() throws IOException, GitAPIException {
        getFilesResult = ConfigEditorResult.fromMessage(ConfigEditorResult.StatusCode.ERROR,
                "init failure");
        when(gitRepo.getFiles(eq(directory), any())).thenReturn(getFilesResult);
        configItems.init();
    }

    @Test
    public void addItemOK() throws IOException, GitAPIException {
        configInfo.setOldVersion(0);
        configInfo.setVersion(1);
        configInfo.setName("new_config");
        configInfo.setConfigInfoType(ConfigInfoType.CONFIG);

        ConfigEditorResult result = configItems.addConfigItem(user, "DUMMY_CONTENT_UPDATED");
        verify(configInfoProvider, times(1))
                .getConfigInfo(user, "DUMMY_CONTENT_UPDATED");
        verify(gitRepo, times(1))
                .transactCopyAndCommit(eq(configInfo), eq(directory), any());
        Assert.assertEquals(OK, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes().getFiles());
        Assert.assertEquals("Updated.json", result.getAttributes().getFiles().get(0).getFileName());
        Assert.assertEquals("DUMMY_CONTENT_UPDATED", result.getAttributes().getFiles().get(0).getContent());

        ConfigEditorResult resultGetFiles = configItems.getFiles();
        Assert.assertEquals("Updated.json", resultGetFiles.getAttributes().getFiles().get(0).getFileName());
        Assert.assertEquals("DUMMY_CONTENT_UPDATED",
                resultGetFiles.getAttributes().getFiles().get(0).getContent());
    }

    @Test
    public void addNewItemWrongVersion() throws IOException, GitAPIException {
        configInfo.setOldVersion(1);
        configInfo.setVersion(2);
        configInfo.setName("new_config");
        configInfo.setConfigInfoType(ConfigInfoType.CONFIG);

        ConfigEditorResult result = configItems.addConfigItem(user, "NEW_DUMMY_ITEM");
        verify(configInfoProvider, times(1))
                .getConfigInfo(user, "NEW_DUMMY_ITEM");
        verify(gitRepo, times(0))
                .transactCopyAndCommit(any(), eq(directory), any());
        Assert.assertEquals(BAD_REQUEST, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes().getMessage());
    }

    @Test
    public void addAndUpdateItemOK() throws IOException, GitAPIException {
        configInfo.setOldVersion(0);
        configInfo.setVersion(1);
        configInfo.setName("test_config");
        configInfo.setConfigInfoType(ConfigInfoType.CONFIG);

        ConfigEditorResult result = configItems.addConfigItem(user, "NEW_DUMMY_ITEM");
        verify(configInfoProvider, times(1))
                .getConfigInfo(user, "NEW_DUMMY_ITEM");

        Assert.assertEquals(OK, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes().getFiles());

        configInfo.setOldVersion(1);
        configInfo.setVersion(2);

        result = configItems.updateConfigItem(user, "NEW_DUMMY_ITEM_UPDATE");
        verify(configInfoProvider, times(1))
                .getConfigInfo(user, "NEW_DUMMY_ITEM_UPDATE");
        Assert.assertEquals(OK, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes().getFiles());
    }

    @Test
    public void addAndUpdateItemWrongVersion() throws IOException, GitAPIException {
        configInfo.setOldVersion(0);
        configInfo.setVersion(1);
        configInfo.setName("test_config");
        configInfo.setConfigInfoType(ConfigInfoType.CONFIG);

        ConfigEditorResult result = configItems.addConfigItem(user, "NEW_DUMMY_ITEM");
        Assert.assertEquals(OK, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes().getFiles());

        configInfo.setOldVersion(0);
        configInfo.setVersion(1);

        result = configItems.updateConfigItem(user, "NEW_DUMMY_ITEM_UPDATE");
        Assert.assertEquals(BAD_REQUEST, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes().getMessage());
    }

    @Test
    public void addAndUpdateAndUpdateItemWrongOK() throws IOException, GitAPIException {
        configInfo.setOldVersion(0);
        configInfo.setVersion(1);
        configInfo.setName("test_config");
        configInfo.setConfigInfoType(ConfigInfoType.CONFIG);

        ConfigEditorResult result = configItems.addConfigItem(user, "NEW_DUMMY_ITEM");
        Assert.assertEquals(OK, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes().getFiles());

        configInfo.setOldVersion(1);
        configInfo.setVersion(2);

        result = configItems.updateConfigItem(user, "NEW_DUMMY_ITEM_UPDATE");
        Assert.assertEquals(OK, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes().getFiles());

        result = configItems.updateConfigItem(user, "NEW_DUMMY_ITEM_UPDATE_2");
        Assert.assertEquals(BAD_REQUEST, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes().getMessage());
    }
}
