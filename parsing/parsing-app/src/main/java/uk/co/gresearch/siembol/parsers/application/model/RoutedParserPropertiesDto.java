package uk.co.gresearch.siembol.parsers.application.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.reinert.jjschema.Attributes;

@Attributes(title = "routed parser properties", description = "The properties of routed parser")
public class RoutedParserPropertiesDto {
    @JsonProperty("routing_field_pattern")
    @Attributes(description = "The pattern for selecting the parser", required = true)
    private String routingFieldPattern;

    @JsonProperty("parser_properties")
    @Attributes(description = "The properties of the selected parser", required = true)
    private ParserPropertiesDto parserProperties;

    public String getRoutingFieldPattern() {
        return routingFieldPattern;
    }

    public void setRoutingFieldPattern(String routingFieldPattern) {
        this.routingFieldPattern = routingFieldPattern;
    }

    public ParserPropertiesDto getParserProperties() {
        return parserProperties;
    }

    public void setParserProperties(ParserPropertiesDto parserProperties) {
        this.parserProperties = parserProperties;
    }
}
