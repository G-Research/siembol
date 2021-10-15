package uk.co.gresearch.siembol.configeditor.rest.common;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import uk.co.gresearch.siembol.common.model.ZooKeeperAttributesDto;
import uk.co.gresearch.siembol.configeditor.sync.common.SynchronisationType;

import java.util.Map;
import java.util.HashMap;

@ConfigurationProperties(prefix = "config-editor")
public class ConfigEditorConfigurationProperties {
    @NestedConfigurationProperty
    private Map<String, ServiceConfigurationProperties> services = new HashMap<>();
    private String testCasesUiConfigFileName;
    private SynchronisationType synchronisation;
    @NestedConfigurationProperty
    private ZooKeeperAttributesDto stormTopologiesZooKeeper;
    @NestedConfigurationProperty
    private Map<String, ZooKeeperAttributesDto> enrichmentTablesZooKeeper;
    private Map<String, String> testingZookeeperFiles;
    private String gitWebhookPassword;

    public Map<String, ZooKeeperAttributesDto> getEnrichmentTablesZooKeeper() {
        return enrichmentTablesZooKeeper;
    }

    public void setEnrichmentTablesZooKeeper(Map<String, ZooKeeperAttributesDto> enrichmentTablesZooKeeper) {
        this.enrichmentTablesZooKeeper = enrichmentTablesZooKeeper;
    }

    public String getGitWebhookPassword() {
        return gitWebhookPassword;
    }

    public Map<String, ServiceConfigurationProperties> getServices() {
        return services;
    }

    public void setServices(Map<String, ServiceConfigurationProperties> services) {
        this.services = services;
    }

    public String getTestCasesUiConfigFileName() {
        return testCasesUiConfigFileName;
    }

    public void setTestCasesUiConfigFileName(String testCasesUiConfigFileName) {
        this.testCasesUiConfigFileName = testCasesUiConfigFileName;
    }

    public SynchronisationType getSynchronisation() {
        return synchronisation;
    }

    public void setSynchronisation(SynchronisationType synchronisation) {
        this.synchronisation = synchronisation;
    }

    public ZooKeeperAttributesDto getStormTopologiesZooKeeper() {
        return stormTopologiesZooKeeper;
    }

    public void setStormTopologiesZooKeeper(ZooKeeperAttributesDto stormTopologiesZooKeeper) {
        this.stormTopologiesZooKeeper = stormTopologiesZooKeeper;
    }

    public Map<String, String> getTestingZookeeperFiles() {
        return testingZookeeperFiles;
    }

    public void setTestingZookeeperFiles(Map<String, String> testingZookeeperFiles) {
        this.testingZookeeperFiles = testingZookeeperFiles;
    }

    public String getGitWebhookSecret() {
        return gitWebhookPassword;
    }

    public void setGitWebhookPassword(String gitWebhookPassword) {
        this.gitWebhookPassword = gitWebhookPassword;
    }
}
