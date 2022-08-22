package uk.co.gresearch.siembol.deployment.monitoring.model;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.LinkedHashMap;
import java.util.Map;

public class HeartbeatMessage {
    @JsonProperty("event_time")
    private String eventTime;
    @JsonProperty("siembol_heartbeat")
    private Boolean siembolHeartbeat = true;
    @JsonProperty("producer_name")
    private String producerName;

    private Map<String, Object> message = new LinkedHashMap<>();

    @JsonAnySetter
    public void setMessage(String key, Object value) {
        message.put(key, value);
    }

    @JsonAnyGetter
    public Map<String, Object> getMessage() {
        return message;
    }

    public Boolean getSiembolHeartbeat() {
        return siembolHeartbeat;
    }

    public void setSiembolHeartbeat(Boolean siembolHeartbeat) {
        this.siembolHeartbeat = siembolHeartbeat;
    }

    public String getEventTime() {
        return eventTime;
    }

    public void setEventTime(String eventTime) {
        this.eventTime = eventTime;
    }

    public String getProducerName() {
        return producerName;
    }

    public void setProducerName(String producerName) {
        this.producerName = producerName;
    }
}
