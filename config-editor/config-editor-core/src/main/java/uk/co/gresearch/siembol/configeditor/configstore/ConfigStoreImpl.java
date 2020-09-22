package uk.co.gresearch.siembol.configeditor.configstore;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import uk.co.gresearch.siembol.configeditor.common.UserInfo;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorAttributes;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorFile;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorRepositories;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult;

import java.io.Closeable;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

import static uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult.StatusCode.BAD_REQUEST;
import static uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult.StatusCode.ERROR;
import static uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult.StatusCode.OK;

public class ConfigStoreImpl implements ConfigStore, Closeable {
    public enum Flags {
        SUPPORT_TEST_CASE
    }
    private static final Logger LOG =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final int RELEASES_MAXIMUM_SHUTTING_DOWN_TIME = 60000; //1min
    private static final int RULES_MAXIMUM_SHUTTING_DOWN_TIME = 120000; //2min
    private static final String TEST_CASES_UNSUPPORTED_MSG = "Test cases are not supported";

    private final ExecutorService gitStoreService =
            Executors.newSingleThreadExecutor();
    private final ExecutorService gitReleasesService =
            Executors.newSingleThreadExecutor();

    private final AtomicReference<List<ConfigEditorFile>> configsCache =
            new AtomicReference<>();
    private final AtomicReference<List<ConfigEditorFile>> testCasesCache =
            new AtomicReference<>(new ArrayList<>());

    private final AtomicReference<Exception> exception =
            new AtomicReference<>();

    private final GitRepository gitStoreRepo;
    private final GitRepository gitReleasesRepo;
    private final ReleasePullRequestService pullRequestService;
    private final ConfigInfoProvider configInfoProvider;
    private final EnumSet<Flags> flags;
    private final ConfigInfoProvider testCaseInfoProvider;

    public ConfigStoreImpl(GitRepository gitStoreRepo,
                           GitRepository gitReleasesRepo,
                           ReleasePullRequestService pullRequestService,
                           ConfigInfoProvider configInfoProvider,
                           ConfigInfoProvider testCaseInfoProvider,
                           EnumSet<Flags> flags) throws IOException, GitAPIException {
        this.gitStoreRepo = gitStoreRepo;
        this.gitReleasesRepo = gitReleasesRepo;
        this.pullRequestService = pullRequestService;
        this.configInfoProvider = configInfoProvider;
        this.testCaseInfoProvider = testCaseInfoProvider;
        this.flags = flags;
        //NOTE: we would like to init rules cache
        initConfigs();
        if (this.flags.contains(Flags.SUPPORT_TEST_CASE)) {
            initTestCases();
        }
    }

    private List<ConfigEditorFile> getFiles(ConfigEditorResult result, Function<String, Boolean> filter) {
        List<ConfigEditorFile> ret = result.getAttributes().getFiles();
        ret.removeIf(x -> !filter.apply(x.getFileName()));
        return ret;
    }

    private void initConfigs() throws IOException, GitAPIException {
        ConfigEditorResult result = gitStoreRepo.getConfigs();
        if (result.getStatusCode() != OK) {
            throw new IllegalStateException("Problem during initialisation of configurations");
        }

        List<ConfigEditorFile> files = getFiles(result, configInfoProvider::isStoreFile);
        configsCache.set(files);
    }

    private void initTestCases() throws IOException, GitAPIException {
        ConfigEditorResult result = gitStoreRepo.getTestCases();
        if (result.getStatusCode() != OK) {
            throw new IllegalStateException("Problem during initialisation of test cases");
        }

        List<ConfigEditorFile> files = getFiles(result, testCaseInfoProvider::isStoreFile);
        testCasesCache.set(files);
    }

    private ConfigEditorResult updateConfigInternally(ConfigInfo configInfo,
                                                      AtomicReference<List<ConfigEditorFile>> cache) {
        try {
            Callable<ConfigEditorResult> command =
                    () -> gitStoreRepo.transactCopyAndCommit(configInfo);

            List<ConfigEditorFile> files = getFiles(gitStoreService.submit(command).get(),
                    configInfoProvider::isStoreFile);
            cache.set(files);
            ConfigEditorAttributes attributes = new ConfigEditorAttributes();
            attributes.setFiles(files);
            return new ConfigEditorResult(OK, attributes);
        } catch (Exception e) {
            exception.set(e);
            String msg = String.format("Exception %s\n during storing a %s with a content %s in git",
                    ExceptionUtils.getStackTrace(e),
                    configInfo.getConfigInfoType().getSingular(),
                    configInfo.getFilesContent());
            LOG.error(msg);
            gitStoreService.shutdown();
            return ConfigEditorResult.fromException(e);
        }
    }

    @Override
    public ConfigEditorResult addTestCase(UserInfo user, String testCase) {
        if (!flags.contains(Flags.SUPPORT_TEST_CASE)) {
            return ConfigEditorResult.fromMessage(ERROR, TEST_CASES_UNSUPPORTED_MSG);
        }

        return addConfigItem(user, testCase, testCaseInfoProvider, testCasesCache);
    }

    @Override
    public ConfigEditorResult updateTestCase(UserInfo user, String testCase) {
        if (!flags.contains(Flags.SUPPORT_TEST_CASE)) {
            return ConfigEditorResult.fromMessage(ERROR, TEST_CASES_UNSUPPORTED_MSG);
        }
        return updateConfigItem(user, testCase, testCaseInfoProvider, testCasesCache);
    }

    @Override
    public ConfigEditorResult getTestCases() {
        if (exception.get() != null) {
            return ConfigEditorResult.fromException(exception.get());
        }

        List<ConfigEditorFile> files = testCasesCache.get();
        ConfigEditorAttributes attributes = new ConfigEditorAttributes();
        attributes.setFiles(files);
        return new ConfigEditorResult(OK, attributes);
    }

    @Override
    public ConfigEditorResult addConfig(UserInfo user, String newConfig)  {
        return addConfigItem(user, newConfig, configInfoProvider, configsCache);
    }

    private ConfigEditorResult addConfigItem(UserInfo user,
                                             String newConfig,
                                             ConfigInfoProvider provider,
                                             AtomicReference<List<ConfigEditorFile>> cache)  {
        if (exception.get() != null) {
            return ConfigEditorResult.fromException(exception.get());
        }

        LOG.info(String.format("User %s requested to add a configuration", user));
        ConfigInfo configInfo;
        try {
            configInfo = provider.getConfigInfo(user, newConfig);
            Set<String> intersection = cache.get().stream().map(x -> x.getFileName()).collect(Collectors.toSet());
            intersection.retainAll(configInfo.getFilesContent().keySet());
            if (!configInfo.isNewConfig()
                    || !intersection.isEmpty()) {
                String msg = "Configuration already exists or has wrong version";
                LOG.error(msg);
                return ConfigEditorResult.fromMessage(
                        ConfigEditorResult.StatusCode.BAD_REQUEST,
                        msg);
            }

            return updateConfigInternally(configInfo, cache);
        } catch (Exception e) {
            String msg = String.format("Exception %s\n during adding new configuration %s, user: %s",
                    ExceptionUtils.getStackTrace(e),
                    newConfig,
                    user);
            LOG.error(msg);
            return ConfigEditorResult.fromMessage(
                    ConfigEditorResult.StatusCode.BAD_REQUEST,
                    msg);
        }
    }

    private ConfigEditorResult updateConfigItem(UserInfo user,
                                                String configToUpdate,
                                                ConfigInfoProvider provider,
                                                AtomicReference<List<ConfigEditorFile>> cache) {
        if (exception.get() != null) {
            return ConfigEditorResult.fromException(exception.get());
        }

        LOG.info(String.format("User %s requested to update the configuration", user));
        ConfigInfo configInfo;
        try {
            configInfo = provider.getConfigInfo(user, configToUpdate);
            Set<String> intersection = cache.get().stream().map(x -> x.getFileName()).collect(Collectors.toSet());
            intersection.retainAll(configInfo.getFilesContent().keySet());
            if (configInfo.isNewConfig()
                    || intersection.isEmpty()) {
                String msg = "Rule does not exist or has wrong version";
                LOG.error(msg);
                return ConfigEditorResult.fromMessage(
                        ConfigEditorResult.StatusCode.BAD_REQUEST,
                        msg);
            }
            return updateConfigInternally(configInfo, cache);
        } catch (Exception e) {
            String msg = String.format("Exception %s\n during updating configuration %s, user: %s",
                    ExceptionUtils.getStackTrace(e),
                    configToUpdate,
                    user);
            LOG.error(msg);
            return ConfigEditorResult.fromMessage(
                    ConfigEditorResult.StatusCode.BAD_REQUEST,
                    msg);
        }
    }

    @Override
    public ConfigEditorResult updateConfig(UserInfo user, String configToUpdate) {
        return updateConfigItem(user, configToUpdate, configInfoProvider, configsCache);
    }

    @Override
    public ConfigEditorResult getConfigs() {
        if (exception.get() != null) {
            return ConfigEditorResult.fromException(exception.get());
        }

        List<ConfigEditorFile> files = configsCache.get();
        if (files == null || files.isEmpty()) {
            return ConfigEditorResult.fromMessage(ConfigEditorResult.StatusCode.ERROR,
                    "Empty configs");
        }

        ConfigEditorAttributes attributes = new ConfigEditorAttributes();
        attributes.setFiles(files);
        return new ConfigEditorResult(OK, attributes);
    }

    @Override
    public ConfigEditorResult getConfigsRelease() {
        if (exception.get() != null) {
            return ConfigEditorResult.fromException(exception.get());
        }

        Callable<ConfigEditorResult> command = () -> gitReleasesRepo.getConfigs();
        try {
            ConfigEditorResult ret = gitReleasesService
                    .submit(command)
                    .get();

            int rulesVersion = configInfoProvider.getReleaseVersion(ret.getAttributes().getFiles());
            List<ConfigEditorFile> files = getFiles(ret, configInfoProvider::isReleaseFile);

            ConfigEditorAttributes attributes = new ConfigEditorAttributes();
            attributes.setRulesVersion(rulesVersion);
            attributes.setFiles(files);
            return new ConfigEditorResult(files.isEmpty() ? ConfigEditorResult.StatusCode.ERROR : OK,
                    attributes);
        } catch (Exception e) {
            exception.set(e);
            String msg = String.format("Exception %s\n during obtaining release files",
                    ExceptionUtils.getStackTrace(e));
            LOG.error(msg);
            gitReleasesService.shutdown();
            return ConfigEditorResult.fromException(e);
        }
    }

    @Override
    public ConfigEditorResult getConfigsReleaseStatus() {
        if (exception.get() != null) {
            return ConfigEditorResult.fromException(exception.get());
        }

        try {
            return pullRequestService.pendingPullRequest();
        } catch (IOException e) {
            exception.set(e);
            String msg = String.format("Exception %s\n during obtaining pull requests",
                    ExceptionUtils.getStackTrace(e));
            LOG.error(msg);
            gitReleasesService.shutdown();
            return ConfigEditorResult.fromException(e);
        }
    }

    @Override
    public ConfigEditorResult submitConfigsRelease(UserInfo user, String rulesRelease) {
        LOG.info(String.format("User %s would like submit release", user.getUserName()));

        if (exception.get() != null) {
            return ConfigEditorResult.fromException(exception.get());
        }

        ConfigInfo releaseInfo;
        try {
            releaseInfo = configInfoProvider.getReleaseInfo(user, rulesRelease);
        } catch (Exception e) {
            String msg = String.format("Exception %s\n processing config info %s, user: %s",
                    ExceptionUtils.getStackTrace(e),
                    rulesRelease,
                    user);
            LOG.error(msg);
            return ConfigEditorResult.fromMessage(
                    ConfigEditorResult.StatusCode.BAD_REQUEST, msg);
        }

        Callable<ConfigEditorResult> command =
                () -> {
            ConfigEditorResult pending = pullRequestService.pendingPullRequest();
            if (pending.getStatusCode() != OK) {
                return pending;
            }
            if (pending.getAttributes().getPendingPullRequest()) {
                return new ConfigEditorResult(BAD_REQUEST, pending.getAttributes());
            }

            gitReleasesRepo.transactCopyAndCommit(releaseInfo);
            return pullRequestService.createPullRequest(releaseInfo);
        };

        try {
            return gitReleasesService
                    .submit(command)
                    .get();
        } catch (Exception e) {
            exception.set(e);
            String msg = String.format("Exception %s\n during submitting rule release %s in git",
                    ExceptionUtils.getStackTrace(e),
                    rulesRelease);
            LOG.error(msg);
            gitReleasesService.shutdown();
            return ConfigEditorResult.fromException(e);
        }
    }

    @Override
    public ConfigEditorResult getRepositories() {
        ConfigEditorRepositories repositories = new ConfigEditorRepositories(
                gitStoreRepo.getRepoUri(),
                gitReleasesRepo.getRepoUri());

        ConfigEditorAttributes attr = new ConfigEditorAttributes();
        attr.setRulesRepositories(repositories);
        return new ConfigEditorResult(OK, attr);
    }

    @Override
    public ConfigEditorResult shutDown() {
        LOG.info("Initiating shutting down the config store service");
        gitStoreService.shutdown();
        gitReleasesService.shutdown();
        return new ConfigEditorResult(OK);
    }

    @Override
    public ConfigEditorResult awaitShutDown() {
        LOG.info("Initiating awaiting shutting down the config store service");
        try {
            String errorMsg = "";
            if (!gitReleasesService.awaitTermination(RELEASES_MAXIMUM_SHUTTING_DOWN_TIME,
                    TimeUnit.MILLISECONDS)) {
                errorMsg = "Git releases services not shut down in the timeout.";
            }

            if (!gitStoreService.awaitTermination(RULES_MAXIMUM_SHUTTING_DOWN_TIME,
                    TimeUnit.MILLISECONDS)) {
                errorMsg += "Git store services not shut down in the timeout.";
            }

            if (!errorMsg.isEmpty()) {
                LOG.error(errorMsg);
                return ConfigEditorResult.fromMessage(ConfigEditorResult.StatusCode.ERROR, errorMsg);
            }
        } catch (InterruptedException e) {
            LOG.error(String.format("Exception during shutting down the config store services %s",
                    ExceptionUtils.getStackTrace(e)));
            return ConfigEditorResult.fromException(e);
        }

        LOG.info("Shutting down the rule store services completed");
        return new ConfigEditorResult(OK);
    }

    @Override
    public Health checkHealth() {
        Exception e = exception.get();
        return e == null
                ? Health.up().build()
                : Health.down(e).build();
    }

    public static ConfigStore createRuleStore(ConfigStoreProperties props,
                                              ConfigInfoProvider ruleInfoProvider) throws GitAPIException, IOException {
        LOG.info(String.format("Initialising git config store service for repositories: %s, %s",
                props.getStoreRepositoryName(),
                props.getReleaseRepositoryName()));

        GitRepository gitStoreRepo = new GitRepository.Builder()
                .gitUrl(props.getGithubUrl())
                .repoFolder(props.getStoreRepositoryPath())
                .repoName(props.getStoreRepositoryName())
                .rulesDirectory(props.getStoreDirectory())
                .testCaseDirectory(props.getTestCaseDirectory())
                .credentials(props.getGitUserName(), props.getGitPassword())
                .contentType(ruleInfoProvider.getFileContentType())
                .build();

        GitRepository gitReleasesRepo = new GitRepository.Builder()
                .gitUrl(props.getGithubUrl())
                .repoFolder(props.getReleaseRepositoryPath())
                .repoName(props.getReleaseRepositoryName())
                .rulesDirectory(props.getReleaseDirectory())
                .credentials(props.getGitUserName(), props.getGitPassword())
                .contentType(ruleInfoProvider.getFileContentType())
                .build();

        ReleasePullRequestService pullRequestService = new ReleasePullRequestService.Builder()
                .uri(props.getGithubUrl())
                .repoName(props.getReleaseRepositoryName())
                .credentials(props.getGitUserName(), props.getGitPassword())
                .build();

        EnumSet<Flags> flags = props.getTestCaseDirectory() == null
                ? EnumSet.noneOf(Flags.class)
                : EnumSet.of(Flags.SUPPORT_TEST_CASE);
        LOG.info("Initialising git config store service completed");
        return new ConfigStoreImpl(gitStoreRepo,
                gitReleasesRepo,
                pullRequestService,
                ruleInfoProvider,
                new TestCaseInfoProvider(),
                flags);
    }

    @Override
    public void close() {
        gitStoreRepo.close();
        gitReleasesRepo.close();
    }
}
