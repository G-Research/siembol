package uk.co.gresearch.siembol.parsers.application.model;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.reinert.jjschema.Attributes;

@Attributes(title = "parsing application", description = "Parser application specification")
public class ParsingApplicationDto {
    @JsonProperty("parsing_app_name")
    @Attributes(required = true, description = "The name of the parsing application")
    private String parsingApplicationName;

    @JsonProperty("parsing_app_version")
    @Attributes(required = true, description = "The version of the parsing application")
    private Integer parsingApplicationVersion;

    @JsonProperty("parsing_app_author")
    @Attributes(required = true, description = "The author of the parsing application")
    private String author;

    @JsonProperty("parsing_app_description")
    @Attributes(description = "Description of the parsing application")
    private String description;

    @JsonProperty("parsing_app_settings")
    @Attributes(description = "Parsing application settings", required = true)
    private ParsingApplicationSettingsDto parsingApplicationSettingsDto;

    @JsonProperty("parsing_settings")
    @Attributes(description = "Parsing settings", required = true)
    private ParsingSettingsDto parsingSettingsDto;

    public String getParsingApplicationName() {
        return parsingApplicationName;
    }

    public void setParsingApplicationName(String parsingApplicationName) {
        this.parsingApplicationName = parsingApplicationName;
    }

    public Integer getParsingApplicationVersion() {
        return parsingApplicationVersion;
    }

    public void setParsingApplicationVersion(Integer parsingApplicationVersion) {
        this.parsingApplicationVersion = parsingApplicationVersion;
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

    public ParsingApplicationSettingsDto getParsingApplicationSettingsDto() {
        return parsingApplicationSettingsDto;
    }

    public void setParsingApplicationSettingsDto(ParsingApplicationSettingsDto parsingApplicationSettingsDto) {
        this.parsingApplicationSettingsDto = parsingApplicationSettingsDto;
    }

    public ParsingSettingsDto getParsingSettingsDto() {
        return parsingSettingsDto;
    }

    public void setParsingSettingsDto(ParsingSettingsDto parsingSettingsDto) {
        this.parsingSettingsDto = parsingSettingsDto;
    }
}
