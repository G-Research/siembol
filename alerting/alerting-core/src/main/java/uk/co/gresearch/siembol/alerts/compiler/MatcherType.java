package uk.co.gresearch.siembol.alerts.compiler;

public enum MatcherType {
    REGEX_MATCH("REGEX_MATCH"),
    IS_IN_SET("IS_IN_SET"),
    COMPOSITE_OR("COMPOSITE_OR"),
    COMPOSITE_AND("COMPOSITE_AND");
    private final String name;

    MatcherType(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
