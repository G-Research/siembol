package uk.co.gresearch.siembol.common.model.testing;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.reinert.jjschema.Attributes;

@Attributes(title = "parserconfig test specification", description = "Specification for testing parser configurations")
public class ParserConfingTestSpecificationDto {
    @Attributes(description = "The encoding of the log for parsing")
    private LogEncodingDto encoding = LogEncodingDto.UTF8_STRING;
    @Attributes(description = "The metadata used for parsing")
    private String metadata;
    @JsonProperty("log")
    @Attributes(required = true, description = "Log for parsing")
    private String log;

    public String getLog() {
        return log;
    }

    public void setLog(String log) {
        this.log = log;
    }

    public LogEncodingDto getEncoding() {
        return encoding;
    }

    public void setEncoding(LogEncodingDto encoding) {
        this.encoding = encoding;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }
}
