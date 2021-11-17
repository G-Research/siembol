package uk.co.gresearch.siembol.common.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum FirstPoolOffsetStrategyDto {
    @JsonProperty("EARLIEST")
    EARLIEST("EARLIEST"),
    @JsonProperty("LATEST")
    LATEST("LATEST"),
    @JsonProperty("UNCOMMITTED_EARLIEST")
    UNCOMMITTED_EARLIEST("UNCOMMITTED_EARLIEST"),
    @JsonProperty("UNCOMMITTED_LATEST")
    UNCOMMITTED_LATEST("UNCOMMITTED_LATEST");

    private final String name;

    FirstPoolOffsetStrategyDto(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

}
