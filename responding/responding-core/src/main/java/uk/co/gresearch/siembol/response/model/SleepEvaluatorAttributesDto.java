package uk.co.gresearch.siembol.response.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.reinert.jjschema.Attributes;
/**
 * A data transfer object for representing sleep evaluator attributes
 *
 * <p>This class is used for json (de)serialisation of sleep evaluator attributes and
 * for generating json schema from this class using annotations.
 *
 * @author  Marian Novotny
 * @see com.github.reinert.jjschema.Attributes
 * @see com.fasterxml.jackson.annotation.JsonProperty
 * @see SleepTimeUnitTypeDto
 */
@Attributes(title = "sleep evaluator attributes", description = "Attributes for sleep evaluator")
public class SleepEvaluatorAttributesDto {

    @JsonProperty("time_unit_type")
    @Attributes(required = true, description = "The type of time units")
    private SleepTimeUnitTypeDto timeUnitType = SleepTimeUnitTypeDto.SECONDS;

    @JsonProperty("sleeping_time")
    @Attributes(required = true, description = "The time of sleeping in time units", minimum = 1)
    private Integer sleepingTime;

    public SleepTimeUnitTypeDto getTimeUnitType() {
        return timeUnitType;
    }

    public void setTimeUnitType(SleepTimeUnitTypeDto timeUnitType) {
        this.timeUnitType = timeUnitType;
    }

    public Integer getSleepingTime() {
        return sleepingTime;
    }

    public void setSleepingTime(Integer sleepingTime) {
        this.sleepingTime = sleepingTime;
    }
}
