package uk.co.gresearch.siembol.deployment.storm.model;

public class KubernetesAttributesDto {
    private String namespace;
    private String stormNimbusServer;
    private String stormSubmitJobTemplateFile;

    public String getStormNimbusServer() {
        return stormNimbusServer;
    }

    public void setStormNimbusServer(String stormNimbusServer) {
        this.stormNimbusServer = stormNimbusServer;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getStormSubmitJobTemplateFile() {
        return stormSubmitJobTemplateFile;
    }

    public void setStormSubmitJobTemplateFile(String stormSubmitJobTemplateFile) {
        this.stormSubmitJobTemplateFile = stormSubmitJobTemplateFile;
    }
}
