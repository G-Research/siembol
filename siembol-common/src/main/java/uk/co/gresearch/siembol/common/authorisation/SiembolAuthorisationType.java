package uk.co.gresearch.siembol.common.authorisation;


public enum SiembolAuthorisationType {
    DISABLED("disabled"),
    OAUTH2("oauth2");
    private final String name;

    SiembolAuthorisationType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
