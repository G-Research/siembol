package uk.co.gresearch.siembol.configeditor.rest.common;

import uk.co.gresearch.siembol.configeditor.model.ConfigEditorAttributes;
import uk.co.gresearch.siembol.configeditor.model.ConfigStoreProperties;

import java.util.*;
import java.util.stream.Collectors;

public class ConfigEditorHelper {
    private static final String INVALID_CONFIG_STORE_PROPERTIES = "Invalid Config Store properties";
    private static final int EXPECTED_ONE_ITEM_SIZE = 1;

    public static Optional<String> getFileContent(ConfigEditorAttributes attributes) {
        return attributes == null || attributes.getFiles() == null || attributes.getFiles().isEmpty()
                ? Optional.empty()
                : Optional.ofNullable(attributes.getFiles().get(0).getContentValue());
    }

    private static Map<String, GitRepoContext> getGitRepositories(Map<String, ServiceConfigurationProperties> services) {
        Map<String, GitRepoContext> gitRepos = new HashMap<>();

        services.forEach((x, y) -> {
            ConfigStoreProperties configStore = y.getConfigStore();
            String storeRepoUrl = configStore.getGithubUrl() + configStore.getStoreRepositoryName();
            String releaseRepoUrl = configStore.getGithubUrl() + configStore.getReleaseRepositoryName();
            if (!gitRepos.containsKey(storeRepoUrl)) {
                gitRepos.put(storeRepoUrl, new GitRepoContext());
            }
            if (!gitRepos.containsKey(releaseRepoUrl)) {
                gitRepos.put(releaseRepoUrl, new GitRepoContext());
            }

            gitRepos.get(storeRepoUrl).userNames.add(configStore.getGitUserName());
            gitRepos.get(releaseRepoUrl).userNames.add(configStore.getGitUserName());

            gitRepos.get(storeRepoUrl).passwords.add(configStore.getGitPassword());
            gitRepos.get(releaseRepoUrl).passwords.add(configStore.getGitPassword());

            gitRepos.get(storeRepoUrl).paths.add(configStore.getStoreRepositoryPath());
            gitRepos.get(releaseRepoUrl).paths.add(configStore.getReleaseRepositoryPath());

            if (gitRepos.get(storeRepoUrl).storeDirectories.contains(configStore.getStoreDirectory())
                    || gitRepos.get(storeRepoUrl).releaseDirectories.contains(configStore.getReleaseDirectory())
                    || (configStore.getTestCaseDirectory() != null
                    && gitRepos.get(storeRepoUrl).testCaseDirectories.contains(configStore.getTestCaseDirectory()))) {
                throw new IllegalArgumentException(INVALID_CONFIG_STORE_PROPERTIES);
            }

            gitRepos.get(storeRepoUrl).storeDirectories.add(configStore.getStoreDirectory());
            gitRepos.get(releaseRepoUrl).releaseDirectories.add(configStore.getReleaseDirectory());
            gitRepos.get(storeRepoUrl).testCaseDirectories.add(configStore.getTestCaseDirectory());
        });

        return gitRepos;
    }

    public static Map<String, ConfigStoreProperties> getConfigStoreProperties(
            ConfigEditorConfigurationProperties properties) {
        Map<String, GitRepoContext> gitRepos = getGitRepositories(properties.getServices());

        gitRepos.keySet().forEach(x -> {
            GitRepoContext current = gitRepos.get(x);
            current.removeNullStrings();

            Set<String> directoriesUnion = new HashSet<>();
            directoriesUnion.addAll(current.storeDirectories);
            directoriesUnion.addAll(current.releaseDirectories);
            directoriesUnion.addAll(current.testCaseDirectories);
            int expectedDirectoriesSize = current.storeDirectories.size()
                    + current.releaseDirectories.size()
                    + current.testCaseDirectories.size();

            if (current.userNames.size() != EXPECTED_ONE_ITEM_SIZE
                    || current.passwords.size() != EXPECTED_ONE_ITEM_SIZE
                    || current.paths.size() != EXPECTED_ONE_ITEM_SIZE
                    || directoriesUnion.size() != expectedDirectoriesSize) {
                throw new IllegalArgumentException(INVALID_CONFIG_STORE_PROPERTIES);
            }
        });

        return properties.getServices().keySet().stream()
                .collect(Collectors.toMap(name -> name, name -> {
                    ConfigStoreProperties current = properties.getServices().get(name).getConfigStore();
                    String storeRepoUrl = current.getGithubUrl() + current.getStoreRepositoryName();
                    String releaseRepoUrl = current.getGithubUrl() + current.getReleaseRepositoryName();

                    current.setGitUserName(gitRepos.get(storeRepoUrl).userNames.iterator().next());
                    current.setGitPassword(gitRepos.get(storeRepoUrl).passwords.iterator().next());

                    current.setStoreRepositoryPath(gitRepos.get(storeRepoUrl).paths.iterator().next());
                    current.setReleaseRepositoryPath(gitRepos.get(releaseRepoUrl).paths.iterator().next());

                    return current;
                }));
    }

    private static class GitRepoContext {
        Set<String> userNames = new HashSet<>();
        Set<String> passwords = new HashSet<>();
        Set<String> paths = new HashSet<>();
        Set<String> storeDirectories = new HashSet<>();
        Set<String> releaseDirectories = new HashSet<>();
        Set<String> testCaseDirectories = new HashSet<>();

        void removeNullStrings() {
            userNames.remove(null);
            passwords.remove(null);
            paths.remove(null);
            storeDirectories.remove(null);
            releaseDirectories.remove(null);
            testCaseDirectories.remove(null);
        }
    }
}