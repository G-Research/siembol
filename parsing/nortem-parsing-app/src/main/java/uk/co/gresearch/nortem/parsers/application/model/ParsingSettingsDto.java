package uk.co.gresearch.nortem.parsers.application.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.reinert.jjschema.Attributes;

@Attributes(title = "parsing settings", description = "Settings of parsing")
public class ParsingSettingsDto {
    @JsonProperty("routing_parser")
    @Attributes(description = "The settings of the routing parsing")
    private RoutingParserDto routingParser;

    @JsonProperty("single_parser")
    @Attributes(title = "single parser", description = "The settings of the single parser parsing", minItems = 1)
    private ParserPropertiesDto singleParser;

    public RoutingParserDto getRoutingParser() {
        return routingParser;
    }

    public void setRoutingParser(RoutingParserDto routingParser) {
        this.routingParser = routingParser;
    }

    public ParserPropertiesDto getSingleParser() {
        return singleParser;
    }

    public void setSingleParser(ParserPropertiesDto singleParser) {
        this.singleParser = singleParser;
    }
}
