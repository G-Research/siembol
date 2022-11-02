package uk.co.gresearch.siembol.alerts.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.reinert.jjschema.Attributes;
/**
 * A data transfer object for representing correlation alerts used in correlation attributes
 *
 * <p>This class is used for json (de)serialisation of a correlation alert and
 * for generating json schema from this class using annotations.
 *
 * @author  Marian Novotny
 * @see com.github.reinert.jjschema.Attributes
 * @see com.fasterxml.jackson.annotation.JsonProperty
 * @see CorrelationAttributesDto
 */
@Attributes(title = "correlation alert", description = "Correlation alert specification")
public class CorrelationAlertDto {
    @JsonProperty("alert")
    @Attributes(required = true,
            description = "The alert name used for correlation")
    private String alert;

    @JsonProperty("threshold")
    @Attributes(required = true, description = "The threshold for alert matches", minimum = 1)
    private Integer alertsThreshold;

    @JsonProperty("mandatory")
    @Attributes(description = "For the rule to match it must pass the threshold")
    private Boolean mandatory = false;

    public String getAlert() {
        return alert;
    }

    public void setAlert(String alert) {
        this.alert = alert;
    }

    public Integer getAlertsThreshold() {
        return alertsThreshold;
    }

    public void setAlertsThreshold(Integer alertsThreshold) {
        this.alertsThreshold = alertsThreshold;
    }

    public Boolean getMandatory() {
        return mandatory;
    }

    public void setMandatory(Boolean mandatory) {
        this.mandatory = mandatory;
    }
}
