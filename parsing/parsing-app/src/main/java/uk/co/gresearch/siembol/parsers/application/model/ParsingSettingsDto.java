package uk.co.gresearch.siembol.parsers.application.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.reinert.jjschema.Attributes;
/**
 * A data transfer object for representing parsing settings
 *
 * <p>This class is used for json (de)serialisation of parsing settings and
 * for generating json schema from this class using annotations.
 *
 * @author  Marian Novotny
 *
 * @see com.github.reinert.jjschema.Attributes
 * @see com.fasterxml.jackson.annotation.JsonProperty
 * @see RoutingParserDto
 * @see ParserPropertiesDto
 * @see TopicRoutingParserDto
 * @see HeaderRoutingParserDto
 *
 */
@Attributes(title = "parsing settings", description = "Settings of parsing")
public class ParsingSettingsDto {
    @JsonProperty("routing_parser")
    @Attributes(description = "The settings of the routing parsing")
    private RoutingParserDto routingParser;

    @JsonProperty("single_parser")
    @Attributes(title = "single parser", description = "The settings of the single parser parsing", minItems = 1)
    private ParserPropertiesDto singleParser;

    @JsonProperty("topic_routing_parser")
    @Attributes(description = "The settings of the topic name routing parsing")
    private TopicRoutingParserDto topicRoutingParserDto;


    @JsonProperty("header_routing_parser")
    @Attributes(description = "The settings of the header routing parsing")
    private HeaderRoutingParserDto headerRoutingParserDto;

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

    public TopicRoutingParserDto getTopicRoutingParserDto() {
        return topicRoutingParserDto;
    }

    public void setTopicRoutingParserDto(TopicRoutingParserDto topicRoutingParserDto) {
        this.topicRoutingParserDto = topicRoutingParserDto;
    }

    public HeaderRoutingParserDto getHeaderRoutingParserDto() {
        return headerRoutingParserDto;
    }

    public void setHeaderRoutingParserDto(HeaderRoutingParserDto headerRoutingParserDto) {
        this.headerRoutingParserDto = headerRoutingParserDto;
    }
}
