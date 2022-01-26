package uk.co.gresearch.siembol.parsers.application.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.reinert.jjschema.Attributes;

@Attributes(title = "topic routing parser properties", description = "The properties of topic routing parser")
public class TopicRoutingParserPropertiesDto {
    @JsonProperty("topic_name")
    @Attributes(description = "The name of the topic for selecting the parser", required = true)
    private String topicName;

    @JsonProperty("parser_properties")
    @Attributes(description = "The properties of the selected parser", required = true)
    private ParserPropertiesDto parserProperties;

    public String getTopicName() {
        return topicName;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }

    public ParserPropertiesDto getParserProperties() {
        return parserProperties;
    }

    public void setParserProperties(ParserPropertiesDto parserProperties) {
        this.parserProperties = parserProperties;
    }
}
