package uk.co.gresearch.siembol.parsers.application.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.reinert.jjschema.Attributes;

import java.util.List;
/**
 * A data transfer object for representing header routing parser
 *
 * <p>This class is used for json (de)serialisation of a header routing parser and
 * for generating json schema from this class using annotations.
 *
 * @author  Marian Novotny
 *
 * @see com.github.reinert.jjschema.Attributes
 * @see com.fasterxml.jackson.annotation.JsonProperty
 * @see HeaderRoutingParserPropertiesDto
 * @see ParserPropertiesDto
 */
@Attributes(title = "header routing parser", description = "The specification for the topic routing parser")
public class HeaderRoutingParserDto {
    @JsonProperty("header_name")
    @Attributes(description = "The name of the header used for routing", required = true)
    private String headerName;

    @JsonProperty("parsers")
    @Attributes(description = "The list of parsers for the further parsing", required = true, minItems = 1)
    private List<HeaderRoutingParserPropertiesDto> parsers;

    @JsonProperty("default_parser")
    @Attributes(title = "default parser",
            description = "The parser that should be used if no other parsers will be selected", required = true)
    private ParserPropertiesDto defaultParser;

    public String getHeaderName() {
        return headerName;
    }

    public void setHeaderName(String headerName) {
        this.headerName = headerName;
    }

    public List<HeaderRoutingParserPropertiesDto> getParsers() {
        return parsers;
    }

    public void setParsers(List<HeaderRoutingParserPropertiesDto> parsers) {
        this.parsers = parsers;
    }

    public ParserPropertiesDto getDefaultParser() {
        return defaultParser;
    }

    public void setDefaultParser(ParserPropertiesDto defaultParser) {
        this.defaultParser = defaultParser;
    }
}
