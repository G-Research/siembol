package uk.co.gresearch.siembol.deployment.storm.model;

public class StormClusterDto {
    private String url;
    private String authenticationType;
    private int killWaitSeconds = 15;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getAuthenticationType() {
        return authenticationType;
    }

    public void setAuthenticationType(String authenticationType) {
        this.authenticationType = authenticationType;
    }

    public int getKillWaitSeconds() {
        return killWaitSeconds;
    }

    public void setKillWaitSeconds(int killWaitSeconds) {
        this.killWaitSeconds = killWaitSeconds;
    }
}
