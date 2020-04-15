package uk.co.gresearch.siembol.parsers.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.reinert.jjschema.Attributes;

@Attributes(title = "parser attributes", description = "Attributes for parser settings")
public class ParserAttributesDto {
    @JsonProperty("parser_type")
    @Attributes(required = true, description = "The type of the parser")
    private ParserTypeDto parserType;

    @JsonProperty("syslog_config")
    @Attributes(description = "The configuration for Syslog parser")
    private SyslogParserConfigDto syslogConfig;

    public SyslogParserConfigDto getSyslogConfig() {
        return syslogConfig;
    }

    public void setSyslogConfig(SyslogParserConfigDto syslogConfig) {
        this.syslogConfig = syslogConfig;
    }

    public ParserTypeDto getParserType() {
        return parserType;
    }

    public void setParserType(ParserTypeDto parserType) {
        this.parserType = parserType;
    }
}
