package uk.co.gresearch.siembol.deployment.monitoring.heartbeat;

import com.fasterxml.jackson.annotation.JsonAnySetter;

import java.util.LinkedHashMap;
import java.util.Map;

public class HeartbeatMessage {
    private long timestamp; // ISO format

    private Boolean siembolHeartbeat;

    private String name; // producerName or parsingClusterName

    private Map<String, Object> message = new LinkedHashMap<>();

    @JsonAnySetter
    void setMessage(String key, Object value) {
        message.put(key, value);
    }

    public Map<String, Object> getMessage() {
        return message;
    }

    public Boolean getSiembolHeartbeat() {
        return siembolHeartbeat;
    }

    public void setSiembolHeartbeat(Boolean siembolHeartbeat) {
        this.siembolHeartbeat = siembolHeartbeat;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
