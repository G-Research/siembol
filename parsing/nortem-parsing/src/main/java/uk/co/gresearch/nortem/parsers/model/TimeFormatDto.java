package uk.co.gresearch.nortem.parsers.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.reinert.jjschema.Attributes;

@Attributes(title = "time format", description = "Time format for timestamp parsing")
public class TimeFormatDto {
    @JsonProperty("validation_regex")
    @Attributes(description = "validation regular expression for checking format of the timestamp")
    private String validationRegex;
    @JsonProperty("time_format")
    @Attributes(required = true, description = "Time format used by Java DateTimeFormatter")
    private String timeFormat;
    @JsonProperty("timezone")
    @Attributes(description = "Timezone used by the time formatter")
    private String timezone = "UTC";

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public String getTimeFormat() {
        return timeFormat;
    }

    public void setTimeFormat(String timeFormat) {
        this.timeFormat = timeFormat;
    }

    public String getValidationRegex() {
        return validationRegex;
    }

    public void setValidationRegex(String validationRegex) {
        this.validationRegex = validationRegex;
    }
}
