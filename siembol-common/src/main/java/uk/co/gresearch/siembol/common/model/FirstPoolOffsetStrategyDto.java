package uk.co.gresearch.siembol.common.model;

import com.fasterxml.jackson.annotation.JsonProperty;
/**
 * An enum for representing a first pool offset strategy used in storm kafka reader
 *
 * <p>This enum is used for json (de)serialisation of a first pool offset strategy used in storm kafka reader.
 *
 * @author  Marian Novotny
 * @see com.fasterxml.jackson.annotation.JsonProperty
 * @see #EARLIEST
 * @see #LATEST
 * @see #UNCOMMITTED_EARLIEST
 * @see #UNCOMMITTED_LATEST
 */
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
