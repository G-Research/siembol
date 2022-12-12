package uk.co.gresearch.siembol.common.constants;
/**
 * An enum for Siembol response application endpoints
 *
 * @author  Marian Novotny
 *
 * @see #GET_SCHEMA
 * @see #GET_TEST_SCHEMA
 * @see #VALIDATE_RULES
 * @see #TEST_RULES
 *
 */
public enum ResponseApplicationPaths {
    GET_SCHEMA("/api/v1/rules/schema"),
    GET_TEST_SCHEMA("/api/v1/rules/testschema"),
    VALIDATE_RULES("/api/v1/rules/validate"),
    TEST_RULES("/api/v1/rules/test");

    private final String name;
    ResponseApplicationPaths(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    public String getName() {
        return name;
    }
}
