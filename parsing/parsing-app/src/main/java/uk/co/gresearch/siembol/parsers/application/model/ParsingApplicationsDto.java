package uk.co.gresearch.siembol.parsers.application.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.reinert.jjschema.Attributes;

import java.util.List;

@Attributes(title = "parsing applications", description = "Parsing applications")
public class ParsingApplicationsDto {
    @JsonProperty("parsing_applications_version")
    @Attributes(required = true, description = "Version of the parser application")
    private Integer parsingApplicationsVersion;

    @JsonProperty("parsing_applications")
    @Attributes(required = true, description = "List of parsing applications", minItems = 1)
    private List<ParsingApplicationDto> parsingApplications;

    public Integer getParsingApplicationsVersion() {
        return parsingApplicationsVersion;
    }

    public void setParsingApplicationsVersion(Integer parsingApplicationsVersion) {
        this.parsingApplicationsVersion = parsingApplicationsVersion;
    }

    public List<ParsingApplicationDto> getParsingApplications() {
        return parsingApplications;
    }

    public void setParsingApplications(List<ParsingApplicationDto> parsingApplications) {
        this.parsingApplications = parsingApplications;
    }
}
