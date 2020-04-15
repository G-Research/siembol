package uk.co.gresearch.siembol.configeditor.configstore;

public enum ConfigInfoType {
    RULE("rule", "Rules"),
    CONFIG("configuration", "Configurations"),
    TEST_CASE("test case", "Test cases");
    private String singular;
    private String plural;

    ConfigInfoType(String singular, String plural) {
        this.singular = singular;
        this.plural = plural;
    }

    public String getSingular() {
        return singular;
    }

    public String getPlural() {
        return plural;
    }
}
