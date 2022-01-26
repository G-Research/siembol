package uk.co.gresearch.siembol.parsers.application.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.reinert.jjschema.Attributes;

@Attributes(title = "parsing application type", description = "The type of parsing application")
public enum ParsingApplicationTypeDto {
    @JsonProperty("router_parsing") ROUTER_PARSING("router_parsing"),
    @JsonProperty("single_parser") SINGLE_PARSER("single_parser"),
    @JsonProperty("topic_routing_parsing") TOPIC_ROUTING_PARSING("topic_routing_parsing"),
    @JsonProperty("header_routing_parsing") HEADER_ROUTING_PARSING("header_routing_parsing");

    private final String name;
    ParsingApplicationTypeDto(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
