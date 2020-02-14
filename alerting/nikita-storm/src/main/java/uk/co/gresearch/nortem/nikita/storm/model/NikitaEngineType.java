package uk.co.gresearch.nortem.nikita.storm.model;

import java.io.Serializable;

public enum NikitaEngineType implements Serializable {
    NIKITA("nikita"),
    NIKITA_CORRELATION("nikita-correlation");

    private static final String ENGINE_STR = "%s-engine";
    private static final String UNKNOWN_MSG = "unknown type: %s";
    private final String name;

    NikitaEngineType(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    public String getEngineName() {
        return String.format(ENGINE_STR, name);
    }

    public static NikitaEngineType valueOfName(String str) {
        for (NikitaEngineType type : NikitaEngineType.values()) {
            if (type.name.equalsIgnoreCase(str)) {
                return type;
            }
        }
        throw new IllegalArgumentException(String.format(UNKNOWN_MSG, str));
    }
}
