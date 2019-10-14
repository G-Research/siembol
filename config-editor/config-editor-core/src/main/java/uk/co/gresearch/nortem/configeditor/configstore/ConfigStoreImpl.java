package uk.co.gresearch.nortem.configeditor.configstore;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import uk.co.gresearch.nortem.configeditor.model.ConfigEditorAttributes;
import uk.co.gresearch.nortem.configeditor.model.ConfigEditorFile;
import uk.co.gresearch.nortem.configeditor.model.ConfigEditorRepositories;
import uk.co.gresearch.nortem.configeditor.model.ConfigEditorResult;

import java.io.Closeable;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

import static uk.co.gresearch.nortem.configeditor.model.ConfigEditorResult.StatusCode.BAD_REQUEST;
import static uk.co.gresearch.nortem.configeditor.model.ConfigEditorResult.StatusCode.OK;

public class ConfigStoreImpl implements ConfigStore, Closeable {
    private static final Logger LOG =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final int RELEASES_MAXIMUM_SHUTTING_DOWN_TIME = 60000; //1min
    private static final int RULES_MAXIMUM_SHUTTING_DOWN_TIME = 120000; //2min

    private final ExecutorService gitStoreService =
            Executors.newSingleThreadExecutor();
    private final ExecutorService gitReleasesService =
            Executors.newSingleThreadExecutor();

    private final AtomicReference<List<ConfigEditorFile>> filesCache =
            new AtomicReference<>();
    private final AtomicReference<Exception> exception =
            new AtomicReference<>();

    private final GitRepository gitStoreRepo;
    private final GitRepository gitReleasesRepo;
    private final ReleasePullRequestService pullRequestService;
    private final ConfigInfoProvider configInfoProvider;

    public ConfigStoreImpl(GitRepository gitStoreRepo,
                           GitRepository gitReleasesRepo,
                           ReleasePullRequestService pullRequestService,
                           ConfigInfoProvider configInfoProvider) throws IOException, GitAPIException {
        this.gitStoreRepo = gitStoreRepo;
        this.gitReleasesRepo = gitReleasesRepo;
        this.pullRequestService = pullRequestService;
        this.configInfoProvider = configInfoProvider;
        //NOTE: we would like to init rules cache
        init();
    }

    private List<ConfigEditorFile> getFiles(ConfigEditorResult result, Function<String, Boolean> filter) {
        List<ConfigEditorFile> ret = result.getAttributes().getFiles();
        ret.removeIf(x -> !filter.apply(x.getFileName()));
        return ret;
    }

    private void init() throws IOException, GitAPIException {
        ConfigEditorResult result = gitStoreRepo.getFiles();
        if (result.getStatusCode() != OK) {
            throw new IllegalStateException("Problem during initialisation");
        }

        List<ConfigEditorFile> files = getFiles(result, configInfoProvider::isStoreFile);
        filesCache.set(files);
    }

    private ConfigEditorResult updateConfigInternally(ConfigInfo configInfo) {
        try {
            Callable<ConfigEditorResult> command =
                    () -> gitStoreRepo.transactCopyAndCommit(configInfo);

            filesCache.set(getFiles(gitStoreService.submit(command).get(), configInfoProvider::isStoreFile));
            return getConfigs();
        } catch (Exception e) {
            exception.set(e);
            String msg = String.format("Exception %s\n during storing a config %s in git",
                    ExceptionUtils.getStackTrace(e),
                    configInfo.getFilesContent());
            LOG.error(msg);
            gitStoreService.shutdown();
            return ConfigEditorResult.fromException(e);
        }
    }

    @Override
    public ConfigEditorResult addConfig(String user, String newConfig)  {
        if (exception.get() != null) {
            return ConfigEditorResult.fromException(exception.get());
        }

        LOG.info(String.format("User %s requested to add configuration", user));
        ConfigInfo configInfo;
        try {
            configInfo = configInfoProvider.getConfigInfo(user, newConfig);
            Set<String> intersection = filesCache.get().stream().map(x -> x.getFileName()).collect(Collectors.toSet());
            intersection.retainAll(configInfo.getFilesContent().keySet());
            if (!configInfo.isNewConfig()
                    || !intersection.isEmpty()) {
                String msg = "Configuration already exists or has wrong version";
                LOG.error(msg);
                return ConfigEditorResult.fromMessage(
                        ConfigEditorResult.StatusCode.BAD_REQUEST,
                        msg);
            }

            return updateConfigInternally(configInfo);
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

    @Override
    public ConfigEditorResult updateConfig(String user, String configToUpdate) {
        if (exception.get() != null) {
            return ConfigEditorResult.fromException(exception.get());
        }

        LOG.info(String.format("User %s requested to update the configuration", user));
        ConfigInfo configInfo;
        try {
            configInfo = configInfoProvider.getConfigInfo(user, configToUpdate);
            Set<String> intersection = filesCache.get().stream().map(x -> x.getFileName()).collect(Collectors.toSet());
            intersection.retainAll(configInfo.getFilesContent().keySet());
            if (configInfo.isNewConfig()
                    || intersection.isEmpty()) {
                String msg = "Rule does not exist or has wrong version";
                LOG.error(msg);
                return ConfigEditorResult.fromMessage(
                        ConfigEditorResult.StatusCode.BAD_REQUEST,
                        msg);
            }
            return updateConfigInternally(configInfo);
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
    public ConfigEditorResult getConfigs() {
        if (exception.get() != null) {
            return ConfigEditorResult.fromException(exception.get());
        }

        List<ConfigEditorFile> files = filesCache.get();
        if (files == null || files.isEmpty()) {
            return ConfigEditorResult.fromMessage(ConfigEditorResult.StatusCode.ERROR,
                    "Empty rules");
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

        Callable<ConfigEditorResult> command = () -> gitReleasesRepo.getFiles();
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
    public ConfigEditorResult submitConfigsRelease(String user, String rulesRelease) {
        LOG.info(String.format("User %s would like submit release", user));

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

        LOG.info("Initialising git config store service completed");
        return new ConfigStoreImpl(gitStoreRepo,
                gitReleasesRepo,
                pullRequestService,
                ruleInfoProvider);
    }

    @Override
    public void close() {
        gitStoreRepo.close();
        gitReleasesRepo.close();
    }
}
