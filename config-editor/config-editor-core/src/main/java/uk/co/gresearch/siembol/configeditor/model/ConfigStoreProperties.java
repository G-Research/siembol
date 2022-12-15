package uk.co.gresearch.siembol.configeditor.model;
/**
 * A properties object that represents config store properties
 *
 * <p>This class represents config store properties for a service. It is used in config editor application properties.
 *
 * @author  Marian Novotny
 */
public class ConfigStoreProperties {
    private String githubUrl;
    private String gitUserName;
    private String gitPassword;
    private String storeRepositoryName;
    private String releaseRepositoryName;
    private String adminConfigRepositoryName;
    private String storeRepositoryPath;
    private String releaseRepositoryPath;
    private String adminConfigRepositoryPath;
    private String storeDirectory;
    private String testCaseDirectory;
    private String releaseDirectory;
    private String adminConfigDirectory;

    public String getGithubUrl() {
        return githubUrl;
    }

    public void setGithubUrl(String githubUrl) {
        this.githubUrl = githubUrl;
    }

    public String getGitUserName() {
        return gitUserName;
    }

    public void setGitUserName(String gitUserName) {
        this.gitUserName = gitUserName;
    }

    public String getGitPassword() {
        return gitPassword;
    }

    public void setGitPassword(String gitPassword) {
        this.gitPassword = gitPassword;
    }

    public String getStoreRepositoryName() {
        return storeRepositoryName;
    }

    public void setStoreRepositoryName(String storeRepositoryName) {
        this.storeRepositoryName = storeRepositoryName;
    }

    public String getReleaseRepositoryName() {
        return releaseRepositoryName;
    }

    public void setReleaseRepositoryName(String releaseRepositoryName) {
        this.releaseRepositoryName = releaseRepositoryName;
    }

    public String getStoreRepositoryPath() {
        return storeRepositoryPath;
    }

    public void setStoreRepositoryPath(String storeRepositoryPath) {
        this.storeRepositoryPath = storeRepositoryPath;
    }

    public String getReleaseRepositoryPath() {
        return releaseRepositoryPath;
    }

    public void setReleaseRepositoryPath(String releaseRepositoryPath) {
        this.releaseRepositoryPath = releaseRepositoryPath;
    }

    public String getStoreDirectory() {
        return storeDirectory;
    }

    public void setStoreDirectory(String storeDirectory) {
        this.storeDirectory = storeDirectory;
    }

    public String getReleaseDirectory() {
        return releaseDirectory;
    }

    public void setReleaseDirectory(String releaseDirectory) {
        this.releaseDirectory = releaseDirectory;
    }

    public String getTestCaseDirectory() {
        return testCaseDirectory;
    }

    public void setTestCaseDirectory(String testCaseDirectory) {
        this.testCaseDirectory = testCaseDirectory;
    }

    public String getAdminConfigRepositoryName() {
        return adminConfigRepositoryName;
    }

    public void setAdminConfigRepositoryName(String adminConfigRepositoryName) {
        this.adminConfigRepositoryName = adminConfigRepositoryName;
    }

    public String getAdminConfigDirectory() {
        return adminConfigDirectory;
    }

    public void setAdminConfigDirectory(String adminConfigDirectory) {
        this.adminConfigDirectory = adminConfigDirectory;
    }

    public String getAdminConfigRepositoryPath() {
        return adminConfigRepositoryPath;
    }

    public void setAdminConfigRepositoryPath(String adminConfigRepositoryPath) {
        this.adminConfigRepositoryPath = adminConfigRepositoryPath;
    }
}
