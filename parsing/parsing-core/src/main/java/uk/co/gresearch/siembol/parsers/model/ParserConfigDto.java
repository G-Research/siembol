package uk.co.gresearch.siembol.parsers.model;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.reinert.jjschema.Attributes;
import java.util.List;
/**
 * A data transfer object for representing attributes for a parser configuration
 *
 * <p>This class is used for json (de)serialisation of a parser configuration and
 * for generating json schema from this class using annotations.
 *
 * @author  Marian Novotny
 * @see com.github.reinert.jjschema.Attributes
 * @see com.fasterxml.jackson.annotation.JsonProperty
 * @see ParserAttributesDto
 * @see ParserExtractorDto
 * @see TransformationDto
 */
@Attributes(title = "parser config", description = "Parser specification")
public class ParserConfigDto {
    @JsonProperty("parser_name")
    @Attributes(required = true, description = "Name of the parser", pattern = "^[a-zA-Z0-9_\\-]+$")
    private String parserName;

    @JsonProperty("parser_version")
    @Attributes(required = true, description = "Version of the parser")
    private Integer parserVersion;

    @JsonProperty("parser_author")
    @Attributes(required = true, description = "Author of the parser")
    private String author;

    @JsonProperty("parser_description")
    @Attributes(description = "Description of the parser")
    private String description;

    @JsonProperty("parser_attributes")
    @Attributes(required = true, description = "Attributes for parser settings")
    private ParserAttributesDto parserAttributes;

    @JsonProperty("parser_extractors")
    @Attributes(description = "Specification of parser extractors", minItems = 1)
    private List<ParserExtractorDto> parserExtractors;

    @JsonProperty("transformations")
    @Attributes(description = "Specification of parser transformations applied after parsing", minItems = 1)
    private List<TransformationDto> parserTransformations;

    public String getParserName() {
        return parserName;
    }

    public void setParserName(String parserName) {
        this.parserName = parserName;
    }

    public Integer getParserVersion() {
        return parserVersion;
    }

    public void setParserVersion(Integer parserVersion) {
        this.parserVersion = parserVersion;
    }

    public List<ParserExtractorDto> getParserExtractors() {
        return parserExtractors;
    }

    public void setParserExtractors(List<ParserExtractorDto> parserExtractors) {
        this.parserExtractors = parserExtractors;
    }

    public List<TransformationDto> getParserTransformations() {
        return parserTransformations;
    }

    public void setParserTransformations(List<TransformationDto> parserTransformations) {
        this.parserTransformations = parserTransformations;
    }

    public ParserAttributesDto getParserAttributes() {
        return parserAttributes;
    }

    public void setParserAttributes(ParserAttributesDto parserAttributes) {
        this.parserAttributes = parserAttributes;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
