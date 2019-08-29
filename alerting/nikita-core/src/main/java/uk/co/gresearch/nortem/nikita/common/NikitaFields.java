package uk.co.gresearch.nortem.nikita.common;

import java.util.HashMap;
import java.util.Map;

public enum NikitaFields {
    FULL_RULE_NAME("full_rule_name"),
    RULE_NAME("rule_name"),
    MAX_PER_HOUR_FIELD("max_per_hour"),
    MAX_PER_DAY_FIELD("max_per_day"),
    EXCEPTION("exception"),
    PROCESSING_TIME("processing_time");

    private final String name;
    private static final String NIKITA_PREFIX = "nikita";
    private static final String NIKITA_CORRELATION_PREFIX = "nikita_correlation";

    private static final Map<String, String> nikitaFields = new HashMap<>();
    private static final Map<String, String> nikitaCorrelationFields = new HashMap<>();
    static {
        for (NikitaFields field : NikitaFields.values()) {
            nikitaFields.put(field.toString(),
                    String.format("%s:%s", NIKITA_PREFIX, field.toString()));
            nikitaCorrelationFields.put(field.toString(),
                    String.format("%s:%s", NIKITA_CORRELATION_PREFIX, field.toString()));
        }
    }

    NikitaFields(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    public String getNikitaName() {
        return nikitaFields.get(name);
    }

    public String getNikitaCorrelationName() {
        return nikitaCorrelationFields.get(name);
    }
}
