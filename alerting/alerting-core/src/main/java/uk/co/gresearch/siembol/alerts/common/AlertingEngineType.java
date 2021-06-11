package uk.co.gresearch.siembol.alerts.common;

import java.io.Serializable;

public enum AlertingEngineType implements Serializable {
    SIEMBOL_ALERTS("siembol_alerts"),
    SIEMBOL_CORRELATION_ALERTS("siembol_correlation_alerts");

    private static final long serialVersionUID = 1L;
    private static final String ENGINE_STR = "%s_engine";
    private static final String UNKNOWN_MSG = "unknown type: %s";
    private final String name;

    AlertingEngineType(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    public String getEngineName() {
        return String.format(ENGINE_STR, name);
    }

    public static AlertingEngineType valueOfName(String str) {
        for (AlertingEngineType type : AlertingEngineType.values()) {
            if (type.name.equalsIgnoreCase(str)) {
                return type;
            }
        }
        throw new IllegalArgumentException(String.format(UNKNOWN_MSG, str));
    }
}
