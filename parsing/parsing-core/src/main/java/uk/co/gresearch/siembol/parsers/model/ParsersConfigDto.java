package uk.co.gresearch.siembol.parsers.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.reinert.jjschema.Attributes;

import java.util.List;
/**
 * A data transfer object for representing attributes for a parsers configurations
 *
 * <p>This class is used for json (de)serialisation of parsers configurations and
 * for generating json schema from this class using annotations.
 *
 * @author  Marian Novotny
 * @see com.github.reinert.jjschema.Attributes
 * @see com.fasterxml.jackson.annotation.JsonProperty
 * @see ParserConfigDto
 */
@Attributes(title = "parsers config", description = "Parsers configuration")
public class ParsersConfigDto {
    @JsonProperty("parsers_version")
    @Attributes(required = true, description = "Version of the parsers config")
    private Integer parserVersion;

    @JsonProperty("parsers_configurations")
    @Attributes(required = true, description = "List of parser configurations", minItems = 1)
    private List<ParserConfigDto> parserConfigurations;

    public Integer getParserVersion() {
        return parserVersion;
    }

    public void setParserVersion(Integer parserVersion) {
        this.parserVersion = parserVersion;
    }

    public List<ParserConfigDto> getParserConfigurations() {
        return parserConfigurations;
    }

    public void setParserConfigurations(List<ParserConfigDto> parserConfigurations) {
        this.parserConfigurations = parserConfigurations;
    }


}
