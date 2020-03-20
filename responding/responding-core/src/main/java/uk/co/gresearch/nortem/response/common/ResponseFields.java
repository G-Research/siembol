package uk.co.gresearch.nortem.response.common;

public enum ResponseFields {
    RULE_NAME("response_rule_name");

    private final String name;
    ResponseFields(String name) {
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
