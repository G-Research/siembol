package uk.co.gresearch.nortem.parsers.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.reinert.jjschema.Attributes;

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
