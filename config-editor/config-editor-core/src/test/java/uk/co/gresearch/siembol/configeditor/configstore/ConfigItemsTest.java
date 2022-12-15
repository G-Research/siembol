package uk.co.gresearch.siembol.configeditor.configstore;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import org.junit.Before;
import uk.co.gresearch.siembol.configeditor.common.ConfigInfo;
import uk.co.gresearch.siembol.configeditor.configinfo.ConfigInfoProvider;
import uk.co.gresearch.siembol.configeditor.common.ConfigInfoType;
import uk.co.gresearch.siembol.configeditor.common.UserInfo;
import uk.co.gresearch.siembol.configeditor.git.GitRepository;
import uk.co.gresearch.siembol.configeditor.model.*;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;
import static uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult.StatusCode.*;

public class ConfigItemsTest {
    private GitRepository gitRepo;
    private String directory = "tmp";
    private String dummyRepoUrl = "url";
    private String dummyDirectoryRepoUrl = "directory url";

    private ConfigInfoProvider configInfoProvider;
    private ConfigItems configItems;

    private Map<String, String> filesContent = new HashMap<>();

    private List<ConfigEditorFile> files;
    private ConfigEditorResult getFilesResult;
    private ConfigInfo configInfo = new ConfigInfo();
    private ConfigInfo newConfigInfo = new ConfigInfo();
    private UserInfo user;

    @Before
    public void setUp() throws IOException, GitAPIException {
        gitRepo = Mockito.mock(GitRepository.class);

        configInfoProvider = Mockito.spy(ConfigInfoProvider.class);

        configInfo.setName("test");
        configInfo.setOldVersion(1);

        when(configInfoProvider.getConfigInfo(any(UserInfo.class), anyString())).thenReturn(configInfo);
        when(configInfoProvider.isStoreFile(anyString())).thenReturn(true);
        when(configInfoProvider.getFileContentType()).thenReturn(ConfigEditorFile.ContentType.STRING);
        filesContent.put("test.json", "DUMMY_CONTENT");
        files = new ArrayList<>();
        files.add(new ConfigEditorFile("test.json", "DUMMY_CONTENT",
                ConfigEditorFile.ContentType.RAW_JSON_STRING));
        ConfigEditorAttributes attr = new ConfigEditorAttributes();
        attr.setFiles(files);
        getFilesResult = new ConfigEditorResult(OK, attr);

        when(gitRepo.getFiles(eq(directory), ArgumentMatchers.<Function<String, Boolean>>any()))
                .thenReturn(getFilesResult);
        when(gitRepo.getRepoUri()).thenReturn(dummyRepoUrl);
        when(gitRepo.getDirectoryUrl(eq(directory))).thenReturn(dummyDirectoryRepoUrl);

        when(gitRepo.transactCopyAndCommit(any(ConfigInfo.class),
                eq(directory),
                ArgumentMatchers.<Function<String, Boolean>>any()))
                .thenReturn(getFilesResult);

        configItems = new ConfigItems(gitRepo, configInfoProvider, directory);
        user = new UserInfo();
        user.setUserName("john");
        user.setEmail("john@secret");
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
        Assert.assertEquals("test.json", result.getAttributes().getFiles().get(0).getFileName());
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
        configItems.init();
        Map<String, Optional<String>> fileContent = new HashMap<>();
        fileContent.put("new_config.json", Optional.of("NEW_CONFIG"));
        newConfigInfo.setOldVersion(0);
        newConfigInfo.setVersion(1);
        newConfigInfo.setName("new_config");
        newConfigInfo.setConfigInfoType(ConfigInfoType.CONFIG);
        newConfigInfo.setFilesContent(fileContent);

        when(configInfoProvider.getConfigInfo(any(UserInfo.class), eq("NEW_CONFIG"))).thenReturn(newConfigInfo);

        ConfigEditorResult result = configItems.addConfigItem(user, "NEW_CONFIG");

        verify(configInfoProvider, times(1))
                .getConfigInfo(user, "NEW_CONFIG");

        verify(gitRepo, times(1))
                .transactCopyAndCommit(eq(newConfigInfo), eq(directory), any());
        Assert.assertEquals(OK, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes().getFiles());
    }

    @Test
    public void addNewItemWrongVersion() throws IOException, GitAPIException {
        configItems.init();
        Map<String, Optional<String>> fileContent = new HashMap<>();
        fileContent.put("new_config.json", Optional.of("NEW_CONFIG"));
        newConfigInfo.setOldVersion(1);
        newConfigInfo.setVersion(2);
        newConfigInfo.setName("new_config");
        newConfigInfo.setConfigInfoType(ConfigInfoType.CONFIG);
        newConfigInfo.setFilesContent(fileContent);

        when(configInfoProvider.getConfigInfo(any(UserInfo.class), eq("NEW_CONFIG"))).thenReturn(newConfigInfo);

        ConfigEditorResult result = configItems.addConfigItem(user, "NEW_CONFIG");

        verify(configInfoProvider, times(1))
                .getConfigInfo(user, "NEW_CONFIG");
        verify(gitRepo, times(0))
                .transactCopyAndCommit(any(), eq(directory), any());
        Assert.assertEquals(BAD_REQUEST, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes().getMessage());
    }

    @Test
    public void updateItemOK() throws IOException, GitAPIException {
        configItems.init();
        Map<String, Optional<String>> fileContent = new HashMap<>();
        fileContent.put("test.json", Optional.of("DUMMY_CONTENT_UPDATE"));
        newConfigInfo.setOldVersion(1);
        newConfigInfo.setVersion(2);
        newConfigInfo.setName("test");
        newConfigInfo.setConfigInfoType(ConfigInfoType.CONFIG);
        newConfigInfo.setFilesContent(fileContent);

        when(configInfoProvider.getConfigInfo(any(UserInfo.class), eq("DUMMY_CONTENT_UPDATE")))
                .thenReturn(newConfigInfo);

        ConfigEditorResult result = configItems.updateConfigItem(user, "DUMMY_CONTENT_UPDATE");
        verify(configInfoProvider, times(1))
                .getConfigInfo(eq(user), eq("DUMMY_CONTENT_UPDATE"));
        verify(configInfoProvider, times(1))
                .getConfigInfo(any(UserInfo.class), eq("DUMMY_CONTENT"));
        verify(gitRepo, times(1))
                .transactCopyAndCommit(any(), eq(directory), any());
        Assert.assertEquals(OK, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes().getFiles());
    }

    @Test
    public void updateItemWrongVersion() throws IOException, GitAPIException {
        configItems.init();
        Map<String, Optional<String>> fileContent = new HashMap<>();
        fileContent.put("test.json", Optional.of("DUMMY_CONTENT_UPDATE"));
        newConfigInfo.setOldVersion(2);
        newConfigInfo.setVersion(3);
        newConfigInfo.setName("test");
        newConfigInfo.setConfigInfoType(ConfigInfoType.CONFIG);
        newConfigInfo.setFilesContent(fileContent);

        when(configInfoProvider.getConfigInfo(any(UserInfo.class), eq("DUMMY_CONTENT_UPDATE")))
                .thenReturn(newConfigInfo);

        ConfigEditorResult result = configItems.updateConfigItem(user, "DUMMY_CONTENT_UPDATE");
        verify(configInfoProvider, times(1))
                .getConfigInfo(eq(user), eq("DUMMY_CONTENT_UPDATE"));
        verify(configInfoProvider, times(1))
                .getConfigInfo(any(UserInfo.class), eq("DUMMY_CONTENT"));
        verify(gitRepo, times(0))
                .transactCopyAndCommit(any(), eq(directory), any());
        Assert.assertEquals(BAD_REQUEST, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes().getMessage());
    }

    @Test
    public void deleteOK() throws IOException, GitAPIException {
        configItems.init();
        when(configInfoProvider.getConfigInfoType()).thenReturn(ConfigInfoType.CONFIG);
        verify(gitRepo, times(1))
                .getFiles(eq(directory), ArgumentMatchers.<Function<String, Boolean>>any());
        ConfigEditorResult result = configItems.deleteItems(user, "test.json");
        Assert.assertEquals(OK, result.getStatusCode());
    }

    @Test
    public void deleteTwoFilesOK() throws IOException, GitAPIException {
        files.add(new ConfigEditorFile("test2.json",
                "DUMMY_CONTENT",
                ConfigEditorFile.ContentType.RAW_JSON_STRING));

        configItems.init();
        when(configInfoProvider.getConfigInfoType()).thenReturn(ConfigInfoType.CONFIG);
        verify(gitRepo, times(1))
                .getFiles(eq(directory), ArgumentMatchers.<Function<String, Boolean>>any());
        ConfigEditorResult result = configItems.deleteItems(user, "test");
        Assert.assertEquals(OK, result.getStatusCode());
    }

    @Test
    public void deleteError() throws IOException, GitAPIException {
        configItems.init();
        when(configInfoProvider.getConfigInfoType()).thenReturn(ConfigInfoType.CONFIG);

        verify(gitRepo, times(1))
                .getFiles(eq(directory), ArgumentMatchers.<Function<String, Boolean>>any());
        when(gitRepo.transactCopyAndCommit(any(ConfigInfo.class),
                eq(directory),
                ArgumentMatchers.<Function<String, Boolean>>any()))
                .thenReturn(ConfigEditorResult.fromMessage(ERROR, "error"));
        ConfigEditorResult result = configItems.deleteItems(user, "test.json");
        Assert.assertEquals(ERROR, result.getStatusCode());
    }
}
