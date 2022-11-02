package uk.co.gresearch.siembol.alerts.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.reinert.jjschema.Attributes;

import java.util.List;
/**
 * A data transfer object for representing alerting correlation attributes
 *
 * <p>This class is used for json (de)serialisation of an alerting correlation attributes and
 * for generating json schema from this class using annotations.
 *
 * @author  Marian Novotny
 * @see com.github.reinert.jjschema.Attributes
 * @see com.fasterxml.jackson.annotation.JsonProperty
 * @see CorrelationRuleDto
 */
@Attributes(title = "correlation attributes", description = "Correlation attributes of real-time correlation alert matching")
public class CorrelationAttributesDto {
    @JsonProperty("time_unit")
    @Attributes(required = true, description = "Time unit for sliding window alert counting")
    private TimeUnitTypeDto timeUnit;

    @JsonProperty("time_window")
    @Attributes(required = true,
            description = "Window size in given time units for alert counting")
    private Integer timeWindow;

    @JsonProperty("time_computation_type")
    @Attributes(required = true,
            description = "The source of time for correlation - timestamp field in event_time or current time in processing_time")
    TimeComputationTypeDto timeComputationType = TimeComputationTypeDto.PROCESSING_TIME;

    @JsonProperty("max_time_lag_in_sec")
    @Attributes(minimum = 0, maximum = 300,
            description = "The event with timestamp older than the current time plus the lag (in seconds) will be discarded")
    Integer maxTimeLagInSec = 30;

    @JsonProperty("alerts_threshold")
    @Attributes(description = "Threshold for alerts that need to match if provided, otherwise all the alerts need to match",
            minimum = 1)
    private Integer alertsThreshold;

    @JsonProperty("alerts")
    @Attributes(required = true, description = "The alerts used for the correlation", minItems = 1)
    private List<CorrelationAlertDto> alerts;

    @JsonProperty("fields_to_send")
    @Attributes(description = "The list of fields of correlated alerts " +
            "that will be included in the triggered alert", minItems = 1)
    private List<String> fieldsToSend;


    public Integer getTimeWindow() {
        return timeWindow;
    }

    public void setTimeWindow(Integer timeWindow) {
        this.timeWindow = timeWindow;
    }

    public Integer getAlertsThreshold() {
        return alertsThreshold;
    }

    public void setAlertsThreshold(Integer alertsThreshold) {
        this.alertsThreshold = alertsThreshold;
    }

    public List<CorrelationAlertDto> getAlerts() {
        return alerts;
    }

    public void setAlerts(List<CorrelationAlertDto> alerts) {
        this.alerts = alerts;
    }

    public TimeUnitTypeDto getTimeUnit() {
        return timeUnit;
    }

    public void setTimeUnit(TimeUnitTypeDto timeUnit) {
        this.timeUnit = timeUnit;
    }

    public TimeComputationTypeDto getTimeComputationType() {
        return timeComputationType;
    }

    public void setTimeComputationType(TimeComputationTypeDto timeComputationType) {
        this.timeComputationType = timeComputationType;
    }

    public Integer getMaxTimeLagInSec() {
        return maxTimeLagInSec;
    }

    public void setMaxTimeLagInSec(Integer maxTimeLagInSec) {
        this.maxTimeLagInSec = maxTimeLagInSec;
    }

    public List<String> getFieldsToSend() {
        return fieldsToSend;
    }

    public void setFieldsToSend(List<String> fieldsToSend) {
        this.fieldsToSend = fieldsToSend;
    }
}
