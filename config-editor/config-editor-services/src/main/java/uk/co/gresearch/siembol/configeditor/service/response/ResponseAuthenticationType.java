package uk.co.gresearch.siembol.configeditor.service.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum ResponseAuthenticationType {
    @JsonProperty("kerberos") KERBEROS("kerberos");
    private final String name;
    ResponseAuthenticationType(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
