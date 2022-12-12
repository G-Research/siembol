package uk.co.gresearch.siembol.response.common;
/**
 * An enum for Siembol response field names which are added to an alert after matching
 *
 * @author  Marian Novotny
 * @see #ALERT_ID
 * @see #RULE_NAME
 * @see #FULL_RULE_NAME
 * @see #ORIGINAL_STRING
 */
public enum ResponseFields {
    ALERT_ID("siembol_response_alert_id"),
    RULE_NAME("siembol_response_rule_name"),
    FULL_RULE_NAME("siembol_response_full_rule_name"),
    ORIGINAL_STRING("siembol_response_original_string");

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
