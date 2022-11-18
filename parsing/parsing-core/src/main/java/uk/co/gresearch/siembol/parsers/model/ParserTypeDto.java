package uk.co.gresearch.siembol.parsers.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.reinert.jjschema.Attributes;
/**
 * An enum for representing a parser type
 *
 * <p>This enum is used for json (de)serialisation of parser type.
 *
 * @author  Marian Novotny
 * @see com.fasterxml.jackson.annotation.JsonProperty
 * @see #GENERIC
 * @see #SYSLOG
 * @see #NETFLOW
 *
 */
@Attributes(title = "parser type", description = "Type of parser to be used")
public enum ParserTypeDto {
    @JsonProperty("generic") GENERIC("generic"),
    @JsonProperty("syslog") SYSLOG("syslog"),
    @JsonProperty("netflow") NETFLOW("netflow");

    private final String name;

    ParserTypeDto(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
