package uk.co.gresearch.siembol.configeditor.serviceaggregator;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import uk.co.gresearch.siembol.common.constants.ServiceType;
import uk.co.gresearch.siembol.configeditor.common.*;
import uk.co.gresearch.siembol.configeditor.configinfo.ConfigInfoProvider;
import uk.co.gresearch.siembol.configeditor.configstore.ConfigStore;
import uk.co.gresearch.siembol.configeditor.configstore.ConfigStoreImpl;
import uk.co.gresearch.siembol.configeditor.git.GitRepository;
import uk.co.gresearch.siembol.configeditor.git.ReleasePullRequestService;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorService;
import uk.co.gresearch.siembol.configeditor.model.ConfigStoreProperties;

import java.io.Closeable;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult.StatusCode.OK;
/**
 * An object for composition of a config store service and a config schema service.
 *
 *
 * <p>This class implements ServiceAggregator and Closeable interfaces.
 * It is for composing a config store service and a config schema service.
 * It checks an authorisation for a service and a user using an authorisation provider.
 * It checks health of all services.
 *
 * @author  Marian Novotny
 * @see ServiceAggregator
 * @see AuthorisationProvider
 *
 */
public class ServiceAggregatorImpl implements ServiceAggregator, Closeable {
    private static final Logger LOG =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final long EXECUTOR_MAXIMUM_SHUTTING_DOWN_TIME_IN_SEC = 10;
    private static final String UNSUPPORTED_SERVICE_MSG = "Unsupported service %s";
    private static final String AUTHORISATION_MSG = "User %s is unauthorised to access the service %s";
    private final AuthorisationProvider authProvider;
    private final Map<String, ServiceAggregatorService> serviceMap;
    private final List<Pair<GitRepository, ExecutorService>> gitRepositoriesServices;

    ServiceAggregatorImpl(Builder builder) {
        this.authProvider = builder.authProvider;
        this.serviceMap = builder.serviceMap;
        this.gitRepositoriesServices = builder.gitRepositoriesServices;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ConfigStore getConfigStore(UserInfo user, String serviceName) throws AuthorisationException {
        return getService(user, serviceName).getConfigStore();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ConfigSchemaService getConfigSchema(UserInfo user, String serviceName) throws AuthorisationException {
        return getService(user, serviceName).getConfigSchemaService();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ServiceAggregatorService> getAggregatorServices() {
        return serviceMap.values().stream().collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ConfigStore> getConfigStoreServices() {
        return serviceMap.keySet().stream()
                .map(x -> serviceMap.get(x).getConfigStore())
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ConfigSchemaService> getConfigSchemaServices() {
        return serviceMap.keySet().stream()
                .map(x -> serviceMap.get(x).getConfigSchemaService())
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ConfigEditorService> getConfigEditorServices(UserInfo user) {
        List<ConfigEditorService> ret = new ArrayList<>();
        for (String serviceName : serviceMap.keySet()) {
            ServiceAggregatorService currentService = serviceMap.get(serviceName);
            List<ServiceUserRole> roles = new ArrayList<>();
            for (ServiceUserRole role: ServiceUserRole.values()) {
                try {
                    if (role == ServiceUserRole.SERVICE_ADMIN && !currentService.supportsAdminConfiguration()) {
                        continue;
                    }

                    user.setServiceUserRole(role);
                    getService(user, serviceName);
                    roles.add(role);
                } catch (AuthorisationException e) {
                    continue;
                }
            }

            if (!roles.isEmpty()) {
                ConfigEditorService configEditorService = new ConfigEditorService();
                configEditorService.setName(serviceName);
                configEditorService.setType(currentService.getType().getName());
                configEditorService.setUserRoles(roles);
                ret.add(configEditorService);
            }
        }

        ret.sort(Comparator.comparing(ConfigEditorService::getName));
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Health checkConfigStoreServicesHealth() {
        return checkServiceHealth(getConfigStoreServices());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Health checkConfigSchemaServicesHealth() {
        return checkServiceHealth(getConfigSchemaServices());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ConfigEditorResult shutDown() {
        LOG.info("Initiating shutting down the config store services");
        gitRepositoriesServices.forEach(x -> x.getRight().shutdown());
        return new ConfigEditorResult(OK);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ConfigEditorResult awaitShutDown() {
        LOG.info("Initiating awaiting shutting down the config store services");

        gitRepositoriesServices.forEach(x -> {
            try {
                x.getRight().awaitTermination(EXECUTOR_MAXIMUM_SHUTTING_DOWN_TIME_IN_SEC, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                LOG.error("Exception during shutting down the config store services {}",
                        ExceptionUtils.getStackTrace(e));
                return;
            }
        });

        LOG.info("Shutting down the config store services completed");
        return new ConfigEditorResult(OK);
    }


    private ServiceAggregatorService getService(UserInfo user, String serviceName) throws AuthorisationException {
        if (!serviceMap.containsKey(serviceName)) {
            throw new UnsupportedOperationException(String.format(UNSUPPORTED_SERVICE_MSG, serviceName));
        }

        AuthorisationProvider.AuthorisationResult authResult = authProvider
                .getUserAuthorisation(user, serviceName);
        if (authResult == AuthorisationProvider.AuthorisationResult.FORBIDDEN) {
            throw new AuthorisationException(String.format(AUTHORISATION_MSG, user, serviceName));
        }
        return serviceMap.get(serviceName);
    }

    private <T extends HealthCheckable> Health checkServiceHealth(List<T> checkList) {
        for (T service : checkList) {
            Health current = service.checkHealth();
            if (current.getStatus() != Status.UP) {
                return current;
            }
        }
        return new Health.Builder().up().build();
    }

    @Override
    public void close() {
        gitRepositoriesServices.forEach(x -> x.getLeft().close());
    }

    /**
     * A builder for a service aggregator
     *
     * @author  Marian Novotny
     */
    public static class Builder {
        private static final String SERVICE_ALREADY_REGISTERED = "Service is already registered";
        private static final String NO_SERVICE_REGISTERED = "No services registered in aggregator";
        private final AuthorisationProvider authProvider;
        private Map<String, ServiceAggregatorService> serviceMap = new HashMap<>();
        private Map<String, Pair<GitRepository, ExecutorService>> gitRepositoriesMap = new HashMap<>();
        private List<Pair<GitRepository, ExecutorService>> gitRepositoriesServices;

        /**
         * Creates a builder
         *
         * @param authProvider an authorisation provider for evaluating the access af a user to a service
         */
        public Builder(AuthorisationProvider authProvider) {
            this.authProvider = authProvider;
        }

        /**
         * Adds a service into the builder
         * @param name the name of the service
         * @param type the type of the service
         * @param configStore an already created config store for the service
         * @param schemaService an already created config schema service for the service
         * @return this builder
         * @see ServiceType
         * @see ConfigStore
         * @see ConfigSchemaService
         */
        Builder addService(String name, ServiceType type, ConfigStore configStore, ConfigSchemaService schemaService) {
            if (serviceMap.containsKey(name)) {
                throw new IllegalArgumentException(SERVICE_ALREADY_REGISTERED);
            }

            ServiceAggregatorService current = new ServiceAggregatorService(name,
                    type,
                    configStore.withErrorMessage(),
                    schemaService.withErrorMessage());
            serviceMap.put(name, current);
            return this;
        }

        /**
         * Adds a service into the builder
         * @param name the name of the service
         * @param type the type of the service
         * @param storeProperties properties of the config store
         * @param configInfoProvider config info provider for the service
         * @param schemaService an already created config schema service for the service
         * @return this builder
         */
        public Builder addService(String name,
                                  ServiceType type,
                                  ConfigStoreProperties storeProperties,
                                  ConfigInfoProvider configInfoProvider,
                                  ConfigSchemaService schemaService) throws Exception {
            ConfigStore configStore = createConfigStore(storeProperties, configInfoProvider);
            return addService(name, type, configStore, schemaService);
        }

        /**
         *
         * @return the service aggregator built from the builder state
         */
        public ServiceAggregator build() {
            if (serviceMap.isEmpty()) {
                throw new IllegalArgumentException(NO_SERVICE_REGISTERED);
            }
            gitRepositoriesServices = gitRepositoriesMap.values().stream().collect(Collectors.toList());
            return new ServiceAggregatorImpl(this);
        }

        private Pair<GitRepository, ExecutorService> getGitRepository(
                ConfigStoreProperties props,
                String repoName,
                String repoFolder) throws GitAPIException, IOException {
            String gitRepoKey = props.getGithubUrl() + repoName;
            if (!gitRepositoriesMap.containsKey(gitRepoKey)) {
                ExecutorService executorService = Executors.newSingleThreadExecutor();

                GitRepository gitRepo = new GitRepository.Builder()
                        .gitUrl(props.getGithubUrl())
                        .repoFolder(repoFolder)
                        .repoName(repoName)
                        .credentials(props.getGitUserName(), props.getGitPassword())
                        .build();

                gitRepositoriesMap.put(gitRepoKey, Pair.of(gitRepo, executorService));
            }

            return gitRepositoriesMap.get(gitRepoKey);
        }

        private ConfigStore createConfigStore(ConfigStoreProperties props,
                                              ConfigInfoProvider configInfoProvider) throws Exception {

            Pair<GitRepository, ExecutorService> storeRepo = getGitRepository(props,
                    props.getStoreRepositoryName(), props.getStoreRepositoryPath());

            Pair<GitRepository, ExecutorService> releaseRepo = getGitRepository(props,
                    props.getReleaseRepositoryName(), props.getReleaseRepositoryPath());

            String defaultReleaseBranch = releaseRepo.getKey().getDefaultBranch();
            ReleasePullRequestService pullRequestService = new ReleasePullRequestService.Builder()
                    .uri(props.getGithubUrl())
                    .repoName(props.getReleaseRepositoryName())
                    .credentials(props.getGitUserName(), props.getGitPassword())
                    .branchTo(defaultReleaseBranch)
                    .build();

            ConfigStoreImpl.Builder builder = new  ConfigStoreImpl.Builder()
                    .configInfoProvider(configInfoProvider)
                    .gitStoreRepo(storeRepo.getLeft())
                    .storeExecutorService(storeRepo.getRight())
                    .gitReleaseRepo(releaseRepo.getLeft())
                    .releaseExecutorService(releaseRepo.getRight())
                    .pullRequestService(pullRequestService)
                    .configStoreDirectory(props.getStoreDirectory())
                    .releaseDirectory(props.getReleaseDirectory())
                    .testCaseDirectory(props.getTestCaseDirectory());

            if (props.getAdminConfigDirectory() != null) {
                Pair<GitRepository, ExecutorService> adminConfigRepo = getGitRepository(props,
                        props.getAdminConfigRepositoryName(), props.getAdminConfigRepositoryPath());

                String defaultAdminConfigBranch = adminConfigRepo.getKey().getDefaultBranch();
                ReleasePullRequestService adminConfigPullRequestService = new ReleasePullRequestService.Builder()
                        .uri(props.getGithubUrl())
                        .repoName(props.getAdminConfigRepositoryName())
                        .credentials(props.getGitUserName(), props.getGitPassword())
                        .branchTo(defaultAdminConfigBranch)
                        .build();

                builder.adminConfigDirectory(props.getAdminConfigDirectory())
                        .adminConfigExecutorService(adminConfigRepo.getRight())
                        .gitAdminConfigRepo(adminConfigRepo.getLeft())
                        .adminConfigPullRequestService(adminConfigPullRequestService);
            }
            return builder.build();
        }
    }
}
