package uk.co.gresearch.siembol.common.model.testing;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.reinert.jjschema.Attributes;
import uk.co.gresearch.siembol.common.jsonschema.JsonRawStringDto;

/**
 * A data transfer object for representing a parser configuration test specification
 *
 * <p>This class is used for json (de)serialisation of a parser configuration test specification and
 * for generating json schema from this class using annotations.
 *
 * @author  Marian Novotny
 * @see com.github.reinert.jjschema.Attributes
 * @see com.fasterxml.jackson.annotation.JsonProperty
 * @see LogEncodingDto
 */
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
