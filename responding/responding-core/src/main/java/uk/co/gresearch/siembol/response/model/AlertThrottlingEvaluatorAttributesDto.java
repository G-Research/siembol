package uk.co.gresearch.siembol.response.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.reinert.jjschema.Attributes;

@Attributes(title = "alert throttling evaluator attributes", description = "Attributes for suppressing alerts")
public class AlertThrottlingEvaluatorAttributesDto {
    @JsonProperty("suppressing_key")
    @Attributes(required = true, description = "The key for suppressing alerts in specified time window")
    private String suppressingKey;

    @JsonProperty("time_unit_type")
    @Attributes(required = true, description = "The type of time units")
    private TimeUnitTypeDto timeUnitType = TimeUnitTypeDto.MINUTES;

    @JsonProperty("suppression_time")
    @Attributes(required = true, description = "The time for alert to be suppressed", minimum = 1)
    private Integer suppressionTime;

    public String getSuppressingKey() {
        return suppressingKey;
    }

    public void setSuppressingKey(String suppressingKey) {
        this.suppressingKey = suppressingKey;
    }

    public TimeUnitTypeDto getTimeUnitType() {
        return timeUnitType;
    }

    public void setTimeUnitType(TimeUnitTypeDto timeUnitType) {
        this.timeUnitType = timeUnitType;
    }

    public Integer getSuppressionTime() {
        return suppressionTime;
    }

    public void setSuppressionTime(Integer suppressionTime) {
        this.suppressionTime = suppressionTime;
    }
}
