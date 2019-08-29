package uk.co.gresearch.nortem.parsers.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.reinert.jjschema.Attributes;

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
