package uk.co.gresearch.siembol.configeditor.configstore;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import uk.co.gresearch.siembol.configeditor.common.ConfigInfoProvider;
import uk.co.gresearch.siembol.configeditor.common.UserInfo;
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
    private static final String GENERIC_EXCEPTION_LOG_MSG = "Exception {} during getting or updating git repositories";
    private static final String MISSING_ARGUMENTS_MSG = "Missing arguments required for config store initialisation";

    private final AtomicReference<Exception> exception = new AtomicReference<>();
    private final ExecutorService storeExecutorService;
    private final ExecutorService releaseExecutorService;
    private final ConfigRelease release;
    private final ConfigItems configs;
    private final ConfigItems testCases;

    ConfigStoreImpl(Builder builder) {
        this.storeExecutorService = builder.storeExecutorService;
        this.releaseExecutorService = builder.releaseExecutorService;
        this.release = builder.release;
        this.configs = builder.configs;
        this.testCases = builder.testCases;
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
    public ConfigEditorResult getConfigs() {
        if (exception.get() != null) {
            return ConfigEditorResult.fromException(exception.get());
        }

        return configs.getFiles();
    }

    @Override
    public ConfigEditorResult getConfigsRelease() {
        Callable<ConfigEditorResult> command = () -> release.getConfigsRelease();
        return executeStoreCommand(command, releaseExecutorService);
    }

    @Override
    public ConfigEditorResult getConfigsReleaseStatus() {
        Callable<ConfigEditorResult> command = () -> release.getConfigsReleaseStatus();
        return executeStoreCommand(command, releaseExecutorService);
    }

    @Override
    public ConfigEditorResult submitConfigsRelease(UserInfo user, String rulesRelease) {
        Callable<ConfigEditorResult> command = () -> release.submitConfigsRelease(user, rulesRelease);
        return executeStoreCommand(command, releaseExecutorService);
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
            return executorService.submit(command).get();
        } catch (Exception e) {
            exception.set(e);
            LOG.error(GENERIC_EXCEPTION_LOG_MSG, ExceptionUtils.getStackTrace(e));
            executorService.shutdown();
            return ConfigEditorResult.fromException(e);
        }
    }

    public static class Builder {
        private final static ConfigInfoProvider TEST_CASE_INFO_PROVIDER = new TestCaseInfoProvider();
        private GitRepository gitStoreRepo;
        private GitRepository gitReleaseRepo;

        private String configStoreDirectory;
        private String testCaseDirectory;
        private String releaseDirectory;
        private ConfigInfoProvider configInfoProvider;
        private ReleasePullRequestService pullRequestService;

        ExecutorService storeExecutorService;
        ExecutorService releaseExecutorService;
        ConfigRelease release;
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

        public Builder storeExecutorService(ExecutorService storeExecutorService) {
            this.storeExecutorService = storeExecutorService;
            return this;
        }

        public Builder releaseExecutorService(ExecutorService releaseExecutorService) {
            this.releaseExecutorService = releaseExecutorService;
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

        public Builder releaseDirectory(String releaseDirectory) {
            this.releaseDirectory = releaseDirectory;
            return this;
        }

        public Builder pullRequestService(ReleasePullRequestService pullRequestService) {
            this.pullRequestService = pullRequestService;
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

            release = new ConfigRelease(gitReleaseRepo, pullRequestService, configInfoProvider, releaseDirectory);

            configs = new ConfigItems(gitStoreRepo, configInfoProvider, configStoreDirectory);
            configs.init();

            if (testCaseDirectory != null) {
                testCases = new ConfigItems(gitStoreRepo, TEST_CASE_INFO_PROVIDER, testCaseDirectory);
                testCases.init();
            }
            return new ConfigStoreImpl(this);
        }
    }
}
