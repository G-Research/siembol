package uk.co.gresearch.siembol.configeditor.configstore;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import uk.co.gresearch.siembol.configeditor.common.ConfigEditorUtils;
import uk.co.gresearch.siembol.configeditor.common.ConfigInfoProvider;
import uk.co.gresearch.siembol.configeditor.common.UserInfo;
import uk.co.gresearch.siembol.configeditor.configinfo.AdminConfigInfoProvider;
import uk.co.gresearch.siembol.configeditor.configinfo.TestCaseInfoProvider;
import uk.co.gresearch.siembol.configeditor.git.GitRepository;
import uk.co.gresearch.siembol.configeditor.git.ReleasePullRequestService;
import uk.co.gresearch.siembol.configeditor.model.*;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicReference;

import static uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult.StatusCode.ERROR;
import static uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult.StatusCode.OK;

public class ConfigStoreImpl implements ConfigStore {
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final String TEST_CASES_UNSUPPORTED_MSG = "Test cases are not supported";
    private static final String ADMIN_CONFIG_UNSUPPORTED_MSG = "Admin configuration is not supported for the service";
    private static final String GENERIC_EXCEPTION_LOG_MSG = "Exception {} during getting or updating git repositories";
    private static final String MISSING_ARGUMENTS_MSG = "Missing arguments required for config store initialisation";
    private static final String ERROR_RESULT_LOG_MSG = "The command finished with status: {}, message: {}";

    private final AtomicReference<Exception> exception = new AtomicReference<>();
    private final ExecutorService storeExecutorService;
    private final ExecutorService releaseExecutorService;
    private final ExecutorService adminConfigExecutorService;
    private final ConfigRelease release;
    private final ConfigRelease adminConfig;
    private final ConfigItems configs;
    private final ConfigItems testCases;

    ConfigStoreImpl(Builder builder) {
        this.storeExecutorService = builder.storeExecutorService;
        this.releaseExecutorService = builder.releaseExecutorService;
        this.adminConfigExecutorService = builder.adminConfigExecutorService;
        this.release = builder.release;
        this.configs = builder.configs;
        this.testCases = builder.testCases;
        this.adminConfig = builder.adminConfig;
    }

    @Override
    public ConfigEditorResult addTestCase(UserInfo user, String testCase) {
        if (testCases == null) {
            return ConfigEditorResult.fromMessage(ERROR, TEST_CASES_UNSUPPORTED_MSG);
        }

        Callable<ConfigEditorResult> command = () -> testCases.addConfigItem(user, testCase);
        return executeStoreCommand(command, storeExecutorService);
    }

    @Override
    public ConfigEditorResult updateTestCase(UserInfo user, String testCase) {
        if (testCases == null) {
            return ConfigEditorResult.fromMessage(ERROR, TEST_CASES_UNSUPPORTED_MSG);
        }

        Callable<ConfigEditorResult> command = () -> testCases.updateConfigItem(user, testCase);
        return executeStoreCommand(command, storeExecutorService);
    }

    @Override
    public ConfigEditorResult deleteTestCase(UserInfo user, String configName, String testCaseName) {
        if (testCases == null) {
            return ConfigEditorResult.fromMessage(ERROR, TEST_CASES_UNSUPPORTED_MSG);
        }

        final String testCaseFileName = ConfigEditorUtils.getTestCaseFileName(configName, testCaseName);
        Callable<ConfigEditorResult> command = () -> testCases.deleteItems(user, testCaseFileName);
        return executeStoreCommand(command, storeExecutorService);
    }

    @Override
    public ConfigEditorResult getTestCases() {
        if (testCases == null) {
            return ConfigEditorResult.fromMessage(ERROR, TEST_CASES_UNSUPPORTED_MSG);
        }

        if (exception.get() != null) {
            return ConfigEditorResult.fromException(exception.get());
        }
        return testCases.getFiles();
    }

    @Override
    public ConfigEditorResult addConfig(UserInfo user, String newConfig) {
        Callable<ConfigEditorResult> command = () -> configs.addConfigItem(user, newConfig);
        return executeStoreCommand(command, storeExecutorService);
    }

    @Override
    public ConfigEditorResult updateConfig(UserInfo user, String configToUpdate) {
        Callable<ConfigEditorResult> command = () -> configs.updateConfigItem(user, configToUpdate);
        return executeStoreCommand(command, storeExecutorService);
    }

    @Override
    public ConfigEditorResult deleteConfig(UserInfo user, String configName) {
        Callable<ConfigEditorResult> releaseCheckCommand = () -> release.checkConfigNotInRelease(configName);
        ConfigEditorResult releaseCheck = executeStoreCommand(releaseCheckCommand, releaseExecutorService);
        if (releaseCheck.getStatusCode() != OK) {
            return releaseCheck;
        }

        Callable<ConfigEditorResult> deleteCommand = () -> {
            ConfigEditorAttributes attributes = new ConfigEditorAttributes();
            final String configFileName = ConfigEditorUtils.getConfigNameFileName(configName);
            ConfigEditorResult deleteConfigResult = configs.deleteItems(user, configFileName);
            if (deleteConfigResult.getStatusCode() != OK) {
                return deleteConfigResult;
            }
            attributes.setConfigsFiles(deleteConfigResult.getAttributes().getFiles());

            if (testCases != null) {
                String testCaseNamePrefix = ConfigEditorUtils.getTestCaseFileNamePrefix(configName);
                ConfigEditorResult deleteTestCasesResult = testCases.deleteItems(user, testCaseNamePrefix);
                if (deleteTestCasesResult.getStatusCode() != OK) {
                    return deleteTestCasesResult;
                }
                attributes.setTestCasesFiles(deleteTestCasesResult.getAttributes().getFiles());
            }
            return new ConfigEditorResult(OK, attributes);
        };

        return executeStoreCommand(deleteCommand, storeExecutorService);
    }

    @Override
    public ConfigEditorResult getConfigs() {
        if (exception.get() != null) {
            return ConfigEditorResult.fromException(exception.get());
        }

        return configs.getFiles();
    }

    @Override
    public ConfigEditorResult getConfigsReleaseFromCache() {
        if (exception.get() != null) {
            return ConfigEditorResult.fromException(exception.get());
        }

        return release.getConfigsReleaseFromCache();
    }

    @Override
    public ConfigEditorResult getConfigsRelease() {
        Callable<ConfigEditorResult> command = release::getConfigsRelease;
        return executeStoreCommand(command, releaseExecutorService);
    }

    @Override
    public ConfigEditorResult getConfigsReleaseStatus() {
        Callable<ConfigEditorResult> command = release::getConfigsReleaseStatus;
        return executeStoreCommand(command, releaseExecutorService);
    }

    @Override
    public ConfigEditorResult submitConfigsRelease(UserInfo user, String rulesRelease) {
        Callable<ConfigEditorResult> command = () -> release.submitConfigsRelease(user, rulesRelease);
        return executeStoreCommand(command, releaseExecutorService);
    }

    @Override
    public ConfigEditorResult getAdminConfigFromCache() {
        if (adminConfig == null) {
            return ConfigEditorResult.fromMessage(ERROR, ADMIN_CONFIG_UNSUPPORTED_MSG);
        }

        if (exception.get() != null) {
            return ConfigEditorResult.fromException(exception.get());
        }

        return adminConfig.getConfigsReleaseFromCache();
    }

    @Override
    public ConfigEditorResult getAdminConfig() {
        if (adminConfig == null) {
            return ConfigEditorResult.fromMessage(ERROR, ADMIN_CONFIG_UNSUPPORTED_MSG);
        }

        Callable<ConfigEditorResult> command = adminConfig::getConfigsRelease;
        return executeStoreCommand(command, adminConfigExecutorService);
    }

    @Override
    public ConfigEditorResult getAdminConfigStatus() {
        if (adminConfig == null) {
            return ConfigEditorResult.fromMessage(ERROR, ADMIN_CONFIG_UNSUPPORTED_MSG);
        }

        Callable<ConfigEditorResult> command = adminConfig::getConfigsReleaseStatus;
        return executeStoreCommand(command, adminConfigExecutorService);
    }

    @Override
    public ConfigEditorResult submitAdminConfig(UserInfo user, String adminConfigStr) {
        if (adminConfig == null) {
            return ConfigEditorResult.fromMessage(ERROR, ADMIN_CONFIG_UNSUPPORTED_MSG);
        }

        Callable<ConfigEditorResult> command = () -> adminConfig.submitConfigsRelease(user, adminConfigStr);
        return executeStoreCommand(command, adminConfigExecutorService);
    }

    @Override
    public ConfigEditorResult getRepositories() {
        ConfigEditorRepositories repositories = new ConfigEditorRepositories();
        repositories.setRuleStoreUrl(configs.getRepoUri());
        repositories.setRuleStoreDirectoryUrl(configs.getDirectoryUri());

        repositories.setRulesReleaseUrl(release.getRepoUri());
        repositories.setRulesReleaseDirectoryUrl(release.getDirectoryUri());

        if (testCases != null) {
            repositories.setTestCaseStoreUrl(testCases.getRepoUri());
            repositories.setTestCaseStoreDirectoryUrl(testCases.getDirectoryUri());
        }

        if (adminConfig != null) {
            repositories.setAdminConfigUrl(adminConfig.getRepoUri());
            repositories.setAdminConfigDirectoryUrl(adminConfig.getDirectoryUri());
        }

        ConfigEditorAttributes attr = new ConfigEditorAttributes();
        attr.setRulesRepositories(repositories);
        return new ConfigEditorResult(OK, attr);
    }

    @Override
    public Health checkHealth() {
        Exception e = exception.get();
        return e == null
                ? Health.up().build()
                : Health.down(e).build();
    }

    private ConfigEditorResult executeStoreCommand(Callable<ConfigEditorResult> command,
                                                   ExecutorService executorService) {
        if (exception.get() != null) {
            return ConfigEditorResult.fromException(exception.get());
        }

        try {
            ConfigEditorResult result = executorService.submit(command).get();
            if (result.getStatusCode() != OK) {
                LOG.error(ERROR_RESULT_LOG_MSG, result.getStatusCode().toString(), result.getAttributes().getMessage());
            }
            return result;
        } catch (Exception e) {
            exception.set(e);
            LOG.error(GENERIC_EXCEPTION_LOG_MSG, ExceptionUtils.getStackTrace(e));
            executorService.shutdown();
            return ConfigEditorResult.fromException(e);
        }
    }

    public static class Builder {
        private static final ConfigInfoProvider TEST_CASE_INFO_PROVIDER = new TestCaseInfoProvider();
        private static final ConfigInfoProvider ADMIN_CONFIG_INFO_PROVIDER = new AdminConfigInfoProvider();

        private GitRepository gitStoreRepo;
        private GitRepository gitReleaseRepo;
        private GitRepository gitAdminConfigRepo;

        private String configStoreDirectory;
        private String testCaseDirectory;
        private String releaseDirectory;
        private String adminConfigDirectory;
        private ConfigInfoProvider configInfoProvider;
        private ReleasePullRequestService pullRequestService;
        private ReleasePullRequestService adminConfigPullRequestService;

        ExecutorService storeExecutorService;
        ExecutorService releaseExecutorService;
        ExecutorService adminConfigExecutorService;

        ConfigRelease release;
        ConfigRelease adminConfig;
        ConfigItems configs;
        ConfigItems testCases;

        public Builder() {
        }

        public Builder gitStoreRepo(GitRepository gitStoreRepo) {
            this.gitStoreRepo = gitStoreRepo;
            return this;
        }

        public Builder gitReleaseRepo(GitRepository gitReleaseRepo) {
            this.gitReleaseRepo = gitReleaseRepo;
            return this;
        }

        public Builder gitAdminConfigRepo(GitRepository gitAdminConfigRepo) {
            this.gitAdminConfigRepo = gitAdminConfigRepo;
            return this;
        }

        public Builder storeExecutorService(ExecutorService storeExecutorService) {
            this.storeExecutorService = storeExecutorService;
            return this;
        }

        public Builder releaseExecutorService(ExecutorService releaseExecutorService) {
            this.releaseExecutorService = releaseExecutorService;
            return this;
        }

        public Builder adminConfigExecutorService(ExecutorService adminConfigExecutorService) {
            this.adminConfigExecutorService = adminConfigExecutorService;
            return this;
        }

        public Builder configStoreDirectory(String configStoreDirectory) {
            this.configStoreDirectory = configStoreDirectory;
            return this;
        }

        public Builder testCaseDirectory(String testCaseDirectory) {
            this.testCaseDirectory = testCaseDirectory;
            return this;
        }

        public Builder adminConfigDirectory(String adminConfigDirectory) {
            this.adminConfigDirectory = adminConfigDirectory;
            return this;
        }

        public Builder releaseDirectory(String releaseDirectory) {
            this.releaseDirectory = releaseDirectory;
            return this;
        }

        public Builder pullRequestService(ReleasePullRequestService pullRequestService) {
            this.pullRequestService = pullRequestService;
            return this;
        }

        public Builder adminConfigPullRequestService(ReleasePullRequestService adminConfigPullRequestService) {
            this.adminConfigPullRequestService = adminConfigPullRequestService;
            return this;
        }

        public Builder configInfoProvider(ConfigInfoProvider configInfoProvider) {
            this.configInfoProvider = configInfoProvider;
            return this;
        }

        public ConfigStore build() throws IOException, GitAPIException {
            if (storeExecutorService == null
                    || releaseExecutorService == null
                    || gitStoreRepo == null
                    || gitReleaseRepo == null
                    || configInfoProvider == null
                    || pullRequestService == null
                    || configStoreDirectory == null
                    || releaseDirectory == null) {
                throw new IllegalArgumentException(MISSING_ARGUMENTS_MSG);
            }

            release = new ConfigRelease(gitReleaseRepo,
                    pullRequestService,
                    configInfoProvider,
                    releaseDirectory);
            release.init();
            
            configs = new ConfigItems(gitStoreRepo, configInfoProvider, configStoreDirectory);
            configs.init();

            if (testCaseDirectory != null) {
                testCases = new ConfigItems(gitStoreRepo, TEST_CASE_INFO_PROVIDER, testCaseDirectory);
                testCases.init();
            }

            if (adminConfigDirectory != null) {
                if (gitAdminConfigRepo == null
                        || adminConfigExecutorService == null
                        || adminConfigPullRequestService == null) {
                    throw new IllegalArgumentException(MISSING_ARGUMENTS_MSG);
                }
                adminConfig = new ConfigRelease(gitAdminConfigRepo,
                        adminConfigPullRequestService,
                        ADMIN_CONFIG_INFO_PROVIDER,
                        adminConfigDirectory);
                adminConfig.init();
            }

            return new ConfigStoreImpl(this);
        }
    }
}
