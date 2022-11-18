package uk.co.gresearch.siembol.parsers.application.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.reinert.jjschema.Attributes;

import java.util.List;
/**
 * A data transfer object for representing topic routing parser
 *
 * <p>This class is used for json (de)serialisation of topic routing parser and
 * for generating json schema from this class using annotations.
 *
 * @author  Marian Novotny
 *
 * @see com.github.reinert.jjschema.Attributes
 * @see com.fasterxml.jackson.annotation.JsonProperty
 * @see ParserPropertiesDto
 * @see TopicRoutingParserPropertiesDto
 */
@Attributes(title = "topic routing parser", description = "The specification for the topic routing parser")
public class TopicRoutingParserDto {
    @JsonProperty("parsers")
    @Attributes(description = "The list of parsers for the further parsing", required = true, minItems = 1)
    private List<TopicRoutingParserPropertiesDto> parsers;

    @JsonProperty("default_parser")
    @Attributes(title = "default parser",
            description = "The parser that should be used if no other parsers will be selected", required = true)
    private ParserPropertiesDto defaultParser;

    public List<TopicRoutingParserPropertiesDto> getParsers() {
        return parsers;
    }

    public void setParsers(List<TopicRoutingParserPropertiesDto> parsers) {
        this.parsers = parsers;
    }

    public ParserPropertiesDto getDefaultParser() {
        return defaultParser;
    }

    public void setDefaultParser(ParserPropertiesDto defaultParser) {
        this.defaultParser = defaultParser;
    }
}
