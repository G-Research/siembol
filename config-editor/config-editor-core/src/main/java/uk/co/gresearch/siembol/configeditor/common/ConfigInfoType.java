package uk.co.gresearch.siembol.configeditor.common;

public enum ConfigInfoType {
    RULE("rule", "Rules"),
    CONFIG("configuration", "Configurations"),
    TEST_CASE("test case", "Test cases"),
    ADMIN_CONFIG("admin configuration", "Admin configuration");
    private String singular;
    private String releaseName;

    ConfigInfoType(String singular, String releaseName) {
        this.singular = singular;
        this.releaseName = releaseName;
    }

    public String getSingular() {
        return singular;
    }

    public String getReleaseName() {
        return releaseName;
    }
}
