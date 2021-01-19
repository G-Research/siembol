package uk.co.gresearch.siembol.configeditor.common;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum ServiceUserRole {
    @JsonProperty("service_user") SERVICE_USER("service_user"),
    @JsonProperty("service_admin") SERVICE_ADMIN("service_admin");
    private final String name;

    ServiceUserRole(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
