package uk.co.gresearch.siembol.configeditor.configstore;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.*;
import org.mockito.Mockito;
import org.springframework.boot.actuate.health.Health;
import uk.co.gresearch.siembol.configeditor.common.UserInfo;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorAttributes;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorFile;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.boot.actuate.health.Status.DOWN;
import static org.springframework.boot.actuate.health.Status.UP;
import static uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult.StatusCode.*;

public class ConfigStoreImplTest {
    private ExecutorService executorService;
    private Map<String, String> filesContent = new HashMap<>();
    private Map<String, String> filesTestCaseContent = new HashMap<>();
    private List<ConfigEditorFile> files;
    private ConfigEditorResult getFilesResult;
    private List<ConfigEditorFile> filesTestCases;
    private ConfigEditorResult filesTestCasesResult;
    private ConfigEditorResult genericResult;
    private UserInfo user;
    private ConfigRelease release;
    private ConfigRelease adminConfig;
    private ConfigItems configs;
    private ConfigItems testCases;
    private ConfigStoreImpl.Builder builder;
    private ConfigStore configStore;

    @Before
    public void setUp() throws IOException, GitAPIException {
        executorService = currentThreadExecutorService();
        release = Mockito.mock(ConfigRelease.class);
        adminConfig = Mockito.mock(ConfigRelease.class);
        configs = Mockito.mock(ConfigItems.class);
        testCases = Mockito.mock(ConfigItems.class);

        filesContent.put("File.json", "DUMMY_CONTENT");
        files = new ArrayList<>();
        files.add(new ConfigEditorFile("File.json",
                "DUMMY_CONTENT",
                ConfigEditorFile.ContentType.RAW_JSON_STRING));
        ConfigEditorAttributes attr = new ConfigEditorAttributes();
        attr.setFiles(files);
        getFilesResult = new ConfigEditorResult(OK, attr);

        filesTestCaseContent.put("TestCase.json", "DUMMY_CONTENT_TEST_CASE");
        filesTestCases = new ArrayList<>();
        filesTestCases.add(new ConfigEditorFile("TestCase.json",
                "DUMMY_CONTENT_TEST_CASE",
                ConfigEditorFile.ContentType.RAW_JSON_STRING));
        ConfigEditorAttributes attrTestCases = new ConfigEditorAttributes();
        attrTestCases.setFiles(filesTestCases);
        filesTestCasesResult = new ConfigEditorResult(OK, attrTestCases);

        when(configs.getFiles()).thenReturn(getFilesResult);
        when(testCases.getFiles()).thenReturn(filesTestCasesResult);
        when(release.getConfigsRelease()).thenReturn(getFilesResult);

        when(configs.getRepoUri()).thenReturn("configs");
        when(configs.getDirectoryUri()).thenReturn("configs_directory");
        when(release.getRepoUri()).thenReturn("release");
        when(release.getDirectoryUri()).thenReturn("release_directory");
        when(adminConfig.getRepoUri()).thenReturn("admin_config");
        when(adminConfig.getDirectoryUri()).thenReturn("admin_config_directory");

        when(testCases.getRepoUri()).thenReturn("test_cases");
        when(testCases.getDirectoryUri()).thenReturn("test_cases_directory");

        genericResult = ConfigEditorResult.fromMessage(OK, "OK");
        builder = new ConfigStoreImpl.Builder();
        builder.configs = configs;
        builder.storeExecutorService = executorService;
        builder.releaseExecutorService = executorService;
        builder.adminConfigExecutorService = executorService;
        builder.release = release;
        builder.testCases = testCases;
        builder.adminConfig = adminConfig;

        configStore = new ConfigStoreImpl(builder);

        user = new UserInfo();
        user.setUserName("john");
        user.setUserName("john@secret");

    }

    @After
    public void tearDown() {
        executorService.shutdown();
    }

    @Test
    public void getRules() {
        ConfigEditorResult ret = configStore.getConfigs();
        Assert.assertEquals(OK, ret.getStatusCode());
        Assert.assertEquals(1, ret.getAttributes().getFiles().size());
        Assert.assertEquals("File.json", ret.getAttributes().getFiles().get(0).getFileName());
        Assert.assertEquals("DUMMY_CONTENT", ret.getAttributes().getFiles().get(0).getContentValue());
    }

    @Test
    public void getTestCases() {
        ConfigEditorResult ret = configStore.getTestCases();
        Assert.assertEquals(OK, ret.getStatusCode());
        Assert.assertEquals(1, ret.getAttributes().getFiles().size());
        Assert.assertEquals("TestCase.json", ret.getAttributes().getFiles().get(0).getFileName());
        Assert.assertEquals("DUMMY_CONTENT_TEST_CASE", ret.getAttributes().getFiles().get(0).getContentValue());
    }

    @Test
    public void getDisabledTestCases()  {
        builder.testCases = null;
        configStore = new ConfigStoreImpl(builder);
        ConfigEditorResult ret = configStore.getTestCases();
        Assert.assertEquals(ERROR, ret.getStatusCode());
        Assert.assertNotNull(ret.getAttributes().getMessage());
    }

    @Test
    public void addTestCasesDisabled() {
        builder.testCases = null;
        configStore = new ConfigStoreImpl(builder);
        ConfigEditorResult ret = configStore.addTestCase(user, "NEW");
        Assert.assertEquals(ERROR, ret.getStatusCode());
        Assert.assertNotNull(ret.getAttributes().getMessage());
    }

    @Test
    public void updateTestCasesDisabled() {
        builder.testCases = null;
        configStore = new ConfigStoreImpl(builder);
        ConfigEditorResult ret = configStore.updateTestCase(user, "UPDATE");
        Assert.assertEquals(ERROR, ret.getStatusCode());
        Assert.assertNotNull(ret.getAttributes().getMessage());
    }

    @Test
    public void checkHealth() {
        Health health = configStore.checkHealth();
        Assert.assertEquals(UP, health.getStatus());
    }

    @Test
    public void checkHealthDown() throws Exception {
        when(configs.addConfigItem(any(UserInfo.class), anyString())).thenThrow(new IllegalStateException("exception"));
        ConfigEditorResult ret = configStore.addConfig(user, "dummy");
        Assert.assertEquals(ERROR, ret.getStatusCode());
        Assert.assertNotNull(ret.getAttributes().getException());

        Health health = configStore.checkHealth();
        Assert.assertEquals(DOWN, health.getStatus());
    }

    @Test
    public void getRepositories() {
        ConfigEditorResult ret = configStore.getRepositories();
        Assert.assertEquals(OK, ret.getStatusCode());

        Assert.assertNotNull(ret.getAttributes().getRulesRepositories());
        Assert.assertEquals("configs", ret.getAttributes()
                .getRulesRepositories().getRuleStoreUrl());
        Assert.assertEquals("configs_directory", ret.getAttributes()
                .getRulesRepositories().getRuleStoreDirectoryUrl());
        Assert.assertEquals("release",
                ret.getAttributes().getRulesRepositories().getRulesReleaseUrl());
        Assert.assertEquals("release_directory",
                ret.getAttributes().getRulesRepositories().getRulesReleaseDirectoryUrl());

        Assert.assertEquals("test_cases", ret.getAttributes()
                .getRulesRepositories().getTestCaseStoreUrl());
        Assert.assertEquals("test_cases_directory", ret.getAttributes()
                .getRulesRepositories().getTestCaseStoreDirectoryUrl());

        Assert.assertEquals("admin_config", ret.getAttributes()
                .getRulesRepositories().getAdminConfigUrl());
        Assert.assertEquals("admin_config_directory", ret.getAttributes()
                .getRulesRepositories().getAdminConfigDirectoryUrl());
    }

    @Test
    public void getRepositoriesNoAdminConfig() {
        builder.adminConfig = null;
        configStore = new ConfigStoreImpl(builder);
        ConfigEditorResult ret = configStore.getRepositories();
        Assert.assertEquals(OK, ret.getStatusCode());

        Assert.assertNotNull(ret.getAttributes().getRulesRepositories());
        Assert.assertEquals("configs", ret.getAttributes()
                .getRulesRepositories().getRuleStoreUrl());
        Assert.assertEquals("configs_directory", ret.getAttributes()
                .getRulesRepositories().getRuleStoreDirectoryUrl());
        Assert.assertEquals("release",
                ret.getAttributes().getRulesRepositories().getRulesReleaseUrl());
        Assert.assertEquals("release_directory",
                ret.getAttributes().getRulesRepositories().getRulesReleaseDirectoryUrl());

        Assert.assertEquals("test_cases", ret.getAttributes()
                .getRulesRepositories().getTestCaseStoreUrl());
        Assert.assertEquals("test_cases_directory", ret.getAttributes()
                .getRulesRepositories().getTestCaseStoreDirectoryUrl());

        Assert.assertNull(ret.getAttributes().getRulesRepositories().getAdminConfigUrl());
        Assert.assertNull(ret.getAttributes().getRulesRepositories().getAdminConfigDirectoryUrl());
    }

    @Test
    public void getRepositoriesNoTestCase() {
        builder.testCases = null;
        configStore = new ConfigStoreImpl(builder);

        ConfigEditorResult ret = configStore.getRepositories();
        Assert.assertEquals(OK, ret.getStatusCode());

        Assert.assertNotNull(ret.getAttributes().getRulesRepositories());
        Assert.assertEquals("configs", ret.getAttributes()
                .getRulesRepositories().getRuleStoreUrl());
        Assert.assertEquals("configs_directory", ret.getAttributes()
                .getRulesRepositories().getRuleStoreDirectoryUrl());
        Assert.assertEquals("release",
                ret.getAttributes().getRulesRepositories().getRulesReleaseUrl());
        Assert.assertEquals("release_directory",
                ret.getAttributes().getRulesRepositories().getRulesReleaseDirectoryUrl());

        Assert.assertNull(ret.getAttributes().getRulesRepositories().getTestCaseStoreUrl());
        Assert.assertNull(ret.getAttributes().getRulesRepositories().getTestCaseStoreDirectoryUrl());
    }

    @Test
    public void addConfig() throws GitAPIException, IOException {
        when(configs.addConfigItem(eq(user), eq("DUMMY"))).thenReturn(genericResult);
        ConfigEditorResult ret = configStore.addConfig(user, "DUMMY");
        verify(configs).addConfigItem(user, "DUMMY");
        Assert.assertEquals(ret, genericResult);
    }

    @Test
    public void updateConfig() throws GitAPIException, IOException {
        when(configs.updateConfigItem(eq(user), eq("DUMMY"))).thenReturn(genericResult);
        ConfigEditorResult ret = configStore.updateConfig(user, "DUMMY");
        verify(configs).updateConfigItem(user, "DUMMY");
        Assert.assertEquals(ret, genericResult);
    }

    @Test
    public void addTestCase() throws GitAPIException, IOException {
        when(testCases.addConfigItem(eq(user), eq("DUMMY"))).thenReturn(genericResult);
        ConfigEditorResult ret = configStore.addTestCase(user, "DUMMY");
        verify(testCases).addConfigItem(user, "DUMMY");
        Assert.assertEquals(ret, genericResult);
    }

    @Test
    public void updateTestCase() throws GitAPIException, IOException {
        when(testCases.updateConfigItem(eq(user), eq("DUMMY"))).thenReturn(genericResult);
        ConfigEditorResult ret = configStore.updateTestCase(user, "DUMMY");
        verify(testCases).updateConfigItem(user, "DUMMY");
        Assert.assertEquals(genericResult, ret);
    }

    @Test
    public void getRelease() throws GitAPIException, IOException {
        when(release.getConfigsRelease()).thenReturn(genericResult);
        ConfigEditorResult ret = configStore.getConfigsRelease();
        verify(release).getConfigsRelease();
        Assert.assertEquals(ret, genericResult);
    }

    @Test
    public void getAdminConfig() throws GitAPIException, IOException {
        when(adminConfig.getConfigsRelease()).thenReturn(genericResult);
        ConfigEditorResult ret = configStore.getAdminConfig();
        verify(adminConfig).getConfigsRelease();
        Assert.assertEquals(ret, genericResult);
    }

    @Test
    public void getAdminConfigUnsupported() {
        builder.adminConfig = null;
        configStore = new ConfigStoreImpl(builder);
        ConfigEditorResult ret = configStore.getAdminConfig();
        Assert.assertEquals(ERROR, ret.getStatusCode());
        Assert.assertNotNull(ret.getAttributes().getMessage());
    }

    @Test
    public void getReleaseStatus() throws IOException {
        when(release.getConfigsReleaseStatus()).thenReturn(genericResult);
        ConfigEditorResult ret = configStore.getConfigsReleaseStatus();
        verify(release).getConfigsReleaseStatus();
        Assert.assertEquals(ret, genericResult);
    }

    @Test
    public void getAdminConfigStatus() throws IOException {
        when(adminConfig.getConfigsReleaseStatus()).thenReturn(genericResult);
        ConfigEditorResult ret = configStore.getAdminConfigStatus();
        verify(adminConfig).getConfigsReleaseStatus();
        Assert.assertEquals(genericResult, ret);
    }

    @Test
    public void getAdminConfigStatusUnsupported() {
        builder.adminConfig = null;
        configStore = new ConfigStoreImpl(builder);
        ConfigEditorResult ret = configStore.getAdminConfigStatus();
        Assert.assertEquals(ERROR, ret.getStatusCode());
        Assert.assertNotNull(ret.getAttributes().getMessage());
    }

    @Test
    public void submitRelease() throws Exception {
        when(release.submitConfigsRelease(eq(user), eq("DUMMY"))).thenReturn(genericResult);
        ConfigEditorResult ret = configStore.submitConfigsRelease(user, "DUMMY");
        verify(release).submitConfigsRelease(user, "DUMMY");
        Assert.assertEquals(ret, genericResult);
    }

    @Test
    public void submitAdminConfig() throws Exception {
        when(adminConfig.submitConfigsRelease(eq(user), eq("DUMMY"))).thenReturn(genericResult);
        ConfigEditorResult ret = configStore.submitAdminConfig(user, "DUMMY");
        verify(adminConfig).submitConfigsRelease(user, "DUMMY");
        Assert.assertEquals(ret, genericResult);
    }

    @Test
    public void submitAdminConfigUnsupported() {
        builder.adminConfig = null;
        configStore = new ConfigStoreImpl(builder);
        ConfigEditorResult ret = configStore.submitAdminConfig(user, "DUMMY");
        Assert.assertEquals(ERROR, ret.getStatusCode());
        Assert.assertNotNull(ret.getAttributes().getMessage());
    }

    @Test
    public void deleteTestCase() throws GitAPIException, IOException {
        configStore = new ConfigStoreImpl(builder);
        when(testCases.deleteItems(eq(user), eq("test_config-test_case.json")))
                .thenReturn(getFilesResult);

        ConfigEditorResult ret = configStore.deleteTestCase(user, "test_config", "test_case");
        Assert.assertEquals(OK, ret.getStatusCode());
        verify(testCases, times(1)).deleteItems(eq(user), eq("test_config-test_case.json"));
    }

    @Test
    public void deleteConfigInRelease() throws GitAPIException, IOException {
        configStore = new ConfigStoreImpl(builder);
        when(release.checkConfigNotInRelease(eq("test_config")))
                .thenReturn(ConfigEditorResult.fromMessage(BAD_REQUEST, "msg"));

        ConfigEditorResult ret = configStore.deleteConfig(user, "test_config");
        Assert.assertEquals(BAD_REQUEST, ret.getStatusCode());
        verify(release, times(1)).checkConfigNotInRelease(eq("test_config"));
    }

    @Test
    public void deleteConfigNotInReleaseNoTestCases() throws GitAPIException, IOException {
        builder.testCases = null;
        configStore = new ConfigStoreImpl(builder);
        when(release.checkConfigNotInRelease(eq("test_config")))
                .thenReturn(ConfigEditorResult.fromMessage(OK, "msg"));

        when(configs.deleteItems(eq(user), eq("test_config.json")))
                .thenReturn(getFilesResult);

        ConfigEditorResult ret = configStore.deleteConfig(user, "test_config");
        Assert.assertEquals(OK, ret.getStatusCode());
        Assert.assertEquals(getFilesResult.getAttributes().getFiles(), ret.getAttributes().getConfigsFiles());
        verify(configs, times(1)).deleteItems(eq(user), eq("test_config.json"));
    }

    @Test
    public void deleteConfigNotInReleaseTestCases() throws GitAPIException, IOException {
        configStore = new ConfigStoreImpl(builder);
        when(release.checkConfigNotInRelease(eq("test_config")))
                .thenReturn(ConfigEditorResult.fromMessage(OK, "msg"));

        when(configs.deleteItems(eq(user), eq("test_config.json")))
                .thenReturn(getFilesResult);

        when(testCases.deleteItems(eq(user), eq("test_config-")))
                .thenReturn(getFilesResult);


        ConfigEditorResult ret = configStore.deleteConfig(user, "test_config");
        Assert.assertEquals(OK, ret.getStatusCode());
        Assert.assertEquals(getFilesResult.getAttributes().getFiles(), ret.getAttributes().getConfigsFiles());
        Assert.assertEquals(getFilesResult.getAttributes().getFiles(), ret.getAttributes().getTestCasesFiles());
        verify(configs, times(1)).deleteItems(eq(user), eq("test_config.json"));
        verify(testCases, times(1)).deleteItems(eq(user), eq("test_config-"));
    }

    @Test
    public void deleteConfigNotInReleaseErrorDelete() throws GitAPIException, IOException {
        configStore = new ConfigStoreImpl(builder);
        when(release.checkConfigNotInRelease(eq("test_config")))
                .thenReturn(ConfigEditorResult.fromMessage(OK, "msg"));

        when(configs.deleteItems(eq(user), eq("test_config.json")))
                .thenReturn(ConfigEditorResult.fromMessage(ERROR, "error"));

        ConfigEditorResult ret = configStore.deleteConfig(user, "test_config");
        Assert.assertEquals(ERROR, ret.getStatusCode());
        verify(configs, times(1)).deleteItems(eq(user), eq("test_config.json"));
    }

    @Test
    public void deleteConfigNotInReleaseTestCasesError() throws GitAPIException, IOException {
        configStore = new ConfigStoreImpl(builder);
        when(release.checkConfigNotInRelease(eq("test_config")))
                .thenReturn(ConfigEditorResult.fromMessage(OK, "msg"));

        when(configs.deleteItems(eq(user), eq("test_config.json")))
                .thenReturn(getFilesResult);

        when(testCases.deleteItems(eq(user), eq("test_config-")))
                .thenReturn(ConfigEditorResult.fromMessage(ERROR, "error"));


        ConfigEditorResult ret = configStore.deleteConfig(user, "test_config");
        Assert.assertEquals(ERROR, ret.getStatusCode());
        verify(configs, times(1)).deleteItems(eq(user), eq("test_config.json"));
        verify(testCases, times(1)).deleteItems(eq(user), eq("test_config-"));
    }



    private static ExecutorService currentThreadExecutorService() {
        ThreadPoolExecutor.CallerRunsPolicy callerRunsPolicy = new ThreadPoolExecutor.CallerRunsPolicy();
        return new ThreadPoolExecutor(0, 1, 0L,
                TimeUnit.SECONDS, new SynchronousQueue<Runnable>(), callerRunsPolicy) {
            @Override
            public void execute(Runnable command) {
                callerRunsPolicy.rejectedExecution(command, this);
            }
        };
    }
}
