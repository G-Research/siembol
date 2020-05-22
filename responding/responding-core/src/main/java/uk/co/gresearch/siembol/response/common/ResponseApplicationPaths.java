package uk.co.gresearch.siembol.response.common;

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
