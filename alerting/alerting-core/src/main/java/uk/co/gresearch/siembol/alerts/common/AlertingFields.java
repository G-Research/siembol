package uk.co.gresearch.siembol.alerts.common;

import java.util.HashMap;
import java.util.Map;
/**
 * An enum of fields that can be added into the event after matching the rule
 *
 * @author  Marian Novotny
 * @see #FULL_RULE_NAME
 * @see #RULE_NAME
 * @see #MAX_PER_HOUR_FIELD
 * @see #MAX_PER_DAY_FIELD
 * @see #EXCEPTION
 * @see #PROCESSING_TIME
 * @see #CORRELATED_ALERTS
 *
 */
public enum AlertingFields {
    FULL_RULE_NAME("full_rule_name"),
    RULE_NAME("rule_name"),
    MAX_PER_HOUR_FIELD("max_per_hour"),
    MAX_PER_DAY_FIELD("max_per_day"),
    EXCEPTION("exception"),
    PROCESSING_TIME("processing_time"),
    CORRELATED_ALERTS("correlated_alerts");

    private final String name;
    private static final String ALERTS_PREFIX = "siembol_alerts";
    private static final String CORRELATION_ALERTS_PREFIX = "siembol_correlation_alerts";

    private static final Map<String, String> ALERTING_FIELDS = new HashMap<>();
    private static final Map<String, String> CORRELATION_ALERTING_FIELDS = new HashMap<>();
    static {
        for (AlertingFields field : AlertingFields.values()) {
            ALERTING_FIELDS.put(field.toString(),
                    String.format("%s_%s", ALERTS_PREFIX, field));
            CORRELATION_ALERTING_FIELDS.put(field.toString(),
                    String.format("%s_%s", CORRELATION_ALERTS_PREFIX, field));
        }
    }

    AlertingFields(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    public String getAlertingName() {
        return ALERTING_FIELDS.get(name);
    }

    public String getCorrelationAlertingName() {
        return CORRELATION_ALERTING_FIELDS.get(name);
    }
}
