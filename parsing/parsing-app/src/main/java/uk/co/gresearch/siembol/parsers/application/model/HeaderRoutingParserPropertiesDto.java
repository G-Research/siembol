package uk.co.gresearch.siembol.parsers.application.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.reinert.jjschema.Attributes;

@Attributes(title = "header routing parser properties", description = "The properties of header routing parser")
public class HeaderRoutingParserPropertiesDto {
    @JsonProperty("source_header_value")
    @Attributes(description = "The value in the header for selecting the parser", required = true)
    private String sourceHeaderValue;

    @JsonProperty("parser_properties")
    @Attributes(description = "The properties of the selected parser", required = true)
    private ParserPropertiesDto parserProperties;

    public String getSourceHeaderValue() {
        return sourceHeaderValue;
    }

    public void setSourceHeaderValue(String sourceHeaderValue) {
        this.sourceHeaderValue = sourceHeaderValue;
    }

    public ParserPropertiesDto getParserProperties() {
        return parserProperties;
    }

    public void setParserProperties(ParserPropertiesDto parserProperties) {
        this.parserProperties = parserProperties;
    }
}
