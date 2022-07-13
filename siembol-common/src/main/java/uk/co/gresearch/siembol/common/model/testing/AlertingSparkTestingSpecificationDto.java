package uk.co.gresearch.siembol.common.model.testing;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.reinert.jjschema.Attributes;

@Attributes(title = "alerting spark test specification", description = "Specification for spark testing alerting rules")
public class AlertingSparkTestingSpecificationDto {
    @JsonProperty("source_type")
    @Attributes(required = true, description = "Source type for testing logs")
    private String sourceType;
    @JsonProperty("max_result")
    @Attributes(required = true, description = "All matching counts will be displayed in the result. " +
            "The result array of matched events will be limited to this value", minimum = 1)
    private Integer maxResult = 100;
    @JsonProperty("from_date")
    @Attributes(required = true, description = "The start date of logs in IS0 format YYYY-MM-DD",
            pattern = "^\\d{4}-\\d\\d-\\d\\d$")
    private String fromDate;
    @JsonProperty(value = "to_date", required = true)
    @Attributes(required = true, description = "The end date of logs in ISO format YYYY-MM-DD",
            pattern = "^\\d{4}-\\d\\d-\\d\\d$")
    private String toDate;

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public Integer getMaxResult() {
        return maxResult;
    }

    public void setMaxResult(Integer maxResult) {
        this.maxResult = maxResult;
    }

    public String getFromDate() {
        return fromDate;
    }

    public void setFromDate(String fromDate) {
        this.fromDate = fromDate;
    }

    public String getToDate() {
        return toDate;
    }

    public void setToDate(String toDate) {
        this.toDate = toDate;
    }
}
