package uk.co.gresearch.siembol.configeditor.common;
/**
 * An enum of configuration types
 *
 * @author  Marian Novotny
 * @see #RULE
 * @see #CONFIG
 * @see #TEST_CASE
 * @see #ADMIN_CONFIG
 */
public enum ConfigInfoType {
    RULE("rule", "rules", "Rules"),
    CONFIG("configuration", "configurations", "Configurations"),
    TEST_CASE("test case", "test cases", "Test cases"),
    ADMIN_CONFIG("admin configuration", "admin configurations", "Admin configuration");
    private final String singular;
    private final String plural;
    private final String releaseName;

    ConfigInfoType(String singular, String plural, String releaseName) {
        this.singular = singular;
        this.plural = plural;
        this.releaseName = releaseName;
    }

    public String getSingular() {
        return singular;
    }

    public String getReleaseName() {
        return releaseName;
    }

    public String getPlural() {
        return plural;
    }
}
