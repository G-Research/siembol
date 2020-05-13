package uk.co.gresearch.siembol.response.common;

public enum ResponseFields {
    ALERT_ID("siembol_response_alert_id"),
    RULE_NAME("siembol_response_rule_name");

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
