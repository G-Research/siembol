package uk.co.gresearch.siembol.parsers.application.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.reinert.jjschema.Attributes;
/**
 * A data transfer object for representing parser properties
 *
 * <p>This class is used for json (de)serialisation of parser properties and
 * for generating json schema from this class using annotations.
 *
 * @author  Marian Novotny
 *
 * @see com.github.reinert.jjschema.Attributes
 * @see com.fasterxml.jackson.annotation.JsonProperty
 */
@Attributes(title = "parser properties", description = "The settings of the parser")
public class ParserPropertiesDto {
    @JsonProperty("parser_name")
    @Attributes(description = "The name of the parser from parser configurations", required = true)
    private String parserName;

    @JsonProperty("output_topic")
    @Attributes(description = "The kafka topic for publishing parsed messages", required = true)
    private String outputTopic;

    public String getParserName() {
        return parserName;
    }

    public void setParserName(String parserName) {
        this.parserName = parserName;
    }

    public String getOutputTopic() {
        return outputTopic;
    }

    public void setOutputTopic(String outputTopic) {
        this.outputTopic = outputTopic;
    }
}
