package uk.co.gresearch.siembol.parsers.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.reinert.jjschema.Attributes;

import java.util.List;
/**
 * A data transfer object for representing a syslog parser configuration
 *
 * <p>This class is used for json (de)serialisation of a syslog parser configuration and
 * for generating json schema from this class using annotations.
 *
 * @author  Marian Novotny
 * @see com.github.reinert.jjschema.Attributes
 * @see com.fasterxml.jackson.annotation.JsonProperty
 * @see TimeFormatDto
 */
@Attributes(title = "syslog config", description = "The specification of syslog parser")
public class SyslogParserConfigDto {
    @JsonProperty("syslog_version")
    @Attributes(description = "Expected version of syslog message")
    private SyslogVersionDto syslogVersion = SyslogVersionDto.RFC_3164_RFC_5424;

    @JsonProperty("merge_sd_elements")
    @Attributes(description = "Merge SD elements into one parsed object")
    private Boolean mergeSdElements = false;
    @JsonProperty("time_formats")
    @Attributes(description = "Time formats used for time formatting. If not provided syslog default time formats are used")
    private List<TimeFormatDto> timeFormats;

    @JsonProperty("timezone")
    @Attributes(description = "Timezone used in syslog default time formats. Not applicable for custom time_formats.")
    private String timezone =  "UTC";



    public SyslogVersionDto getSyslogVersion() {
        return syslogVersion;
    }

    public void setSyslogVersion(SyslogVersionDto syslogVersion) {
        this.syslogVersion = syslogVersion;
    }

    public Boolean getMergeSdElements() {
        return mergeSdElements;
    }

    public void setMergeSdElements(Boolean mergeSdElements) {
        this.mergeSdElements = mergeSdElements;
    }

    public List<TimeFormatDto> getTimeFormats() {
        return timeFormats;
    }

    public void setTimeFormats(List<TimeFormatDto> timeFormats) {
        this.timeFormats = timeFormats;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }
}
