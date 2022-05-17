package uk.co.gresearch.siembol.response.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.reinert.jjschema.Attributes;

@Attributes(title = "time exclusion evaluator attributes",
        description = "Attributes for time exclusion evaluator")
public class TimeExclusionEvaluatorAttributesDto {
    @JsonProperty("timestamp_field")
    @Attributes(required = true,
            description = "The name of the milliseconds epoch timestamp field  which will be used for evaluating the exclusion")
    private String timestampField = "timestamp";
    @JsonProperty("time_zone")
    @Attributes(required = true,
            description = "Timezone which will be used for interpreting timestamp")
    private String timeZone = "Europe/London";

    @JsonProperty("months_of_year_pattern")
    @Attributes(required = true,
            description = "Months of year pattern, where months are numbers from 1 to 12")
    private String monthsOfYearPattern = ".*";
    @JsonProperty("days_of_week_pattern")
    @Attributes(required = true,
            description = "Days of week pattern, where days are numbers from 1 to 7")
    private String daysOfWeekPattern = ".*";

    @JsonProperty("hours_of_day_pattern")
    @Attributes(required = true,
            description = "Hours of day pattern, where hours are numbers from 0 to 23")
    private String hoursOfDayPattern = ".*";

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    public String getTimestampField() {
        return timestampField;
    }

    public void setTimestampField(String timestampField) {
        this.timestampField = timestampField;
    }

    public String getMonthsOfYearPattern() {
        return monthsOfYearPattern;
    }

    public void setMonthsOfYearPattern(String monthsOfYearPattern) {
        this.monthsOfYearPattern = monthsOfYearPattern;
    }

    public String getDaysOfWeekPattern() {
        return daysOfWeekPattern;
    }

    public void setDaysOfWeekPattern(String daysOfWeekPattern) {
        this.daysOfWeekPattern = daysOfWeekPattern;
    }

    public String getHoursOfDayPattern() {
        return hoursOfDayPattern;
    }

    public void setHoursOfDayPattern(String hoursOfDayPattern) {
        this.hoursOfDayPattern = hoursOfDayPattern;
    }
}
