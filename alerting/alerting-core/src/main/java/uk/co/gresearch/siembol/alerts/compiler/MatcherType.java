package uk.co.gresearch.siembol.alerts.compiler;

public enum MatcherType {
    REGEX_MATCH("REGEX_MATCH"),
    IS_IN_SET("IS_IN_SET"),
    CONTAINS("CONTAINS"),
    COMPOSITE_OR("COMPOSITE_OR"),
    COMPOSITE_AND("COMPOSITE_AND"),
    NUMERIC_COMPARE("NUMERIC_COMPARE");
    private final String name;

    MatcherType(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
