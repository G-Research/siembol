package uk.co.gresearch.siembol.deployment.monitoring.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.co.gresearch.siembol.common.constants.SiembolConstants;

public class HeartbeatProcessedMessage extends HeartbeatMessage {
    @JsonProperty(SiembolConstants.TIMESTAMP)
    private Number timestamp;

    @JsonProperty(SiembolConstants.PARSING_TIME)
    private Number parsingTime;

    @JsonProperty(SiembolConstants.ENRICHING_TIME)
    private Number enrichingTime;

    @JsonProperty(SiembolConstants.RESPONSE_TIME)
    private Number responseTime;

    public Number getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Number timestamp) {
        this.timestamp = timestamp;
    }

    public Number getParsingTime() {
        return parsingTime;
    }

    public void setParsingTime(Number parsingTime) {
        this.parsingTime = parsingTime;
    }

    public Number getEnrichingTime() {
        return enrichingTime;
    }

    public void setEnrichingTime(Number enrichingTime) {
        this.enrichingTime = enrichingTime;
    }

    public Number getResponseTime() {
        return responseTime;
    }

    public void setResponseTime(Number responseTime) {
        this.responseTime = responseTime;
    }
}
