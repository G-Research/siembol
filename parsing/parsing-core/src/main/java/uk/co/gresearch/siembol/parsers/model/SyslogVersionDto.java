package uk.co.gresearch.siembol.parsers.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.reinert.jjschema.Attributes;
/**
 * An enum for representing a syslog version
 *
 * <p>This enum is used for json (de)serialisation of case type.
 *
 * @author  Marian Novotny
 * @see com.fasterxml.jackson.annotation.JsonProperty
 * @see #RFC_3164_RFC_5424
 * @see #RFC_3164
 * @see #RFC_5424
 *
 */
@Attributes(title = "syslog version", description = "Version of syslog message format")
public enum SyslogVersionDto {
    @JsonProperty("RFC_3164, RFC_5424") RFC_3164_RFC_5424("RFC_3164, RFC_5424"),
    @JsonProperty("RFC_3164") RFC_3164("RFC_3164"),
    @JsonProperty("RFC_5424") RFC_5424("RFC_5424");

    private final String name;
    SyslogVersionDto(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
