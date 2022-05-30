package uk.co.gresearch.siembol.deployment.monitoring.application;

import java.util.Map;

public class HeartbeatProperties {
    private int heartbeatIntervalSeconds = 60;

    private Map<String, HeartbeatProducerProperties> heartbeatProducers;
    private Map<String, HeartbeatConsumerProperties> heartbeatConsumers;
    private Map<String, Object> message;

    public int getHeartbeatIntervalSeconds() {
        return heartbeatIntervalSeconds;
    }

    public void setHeartbeatIntervalSeconds(int heartbeatIntervalSeconds) {
        this.heartbeatIntervalSeconds = heartbeatIntervalSeconds;
    }

    public Map<String, HeartbeatProducerProperties> getHeartbeatProducers() {
        return heartbeatProducers;
    }

    public void setHeartbeatProducers(Map<String, HeartbeatProducerProperties> heartbeatProducers) {
        this.heartbeatProducers = heartbeatProducers;
    }

    public Map<String, HeartbeatConsumerProperties> getHeartbeatConsumers() {
        return heartbeatConsumers;
    }

    public void setHeartbeatConsumers(Map<String, HeartbeatConsumerProperties> heartbeatConsumers) {
        this.heartbeatConsumers = heartbeatConsumers;
    }

    public Map<String, Object> getMessage() {
        return message;
    }

    public void setMessage(Map<String, Object> message) {
        this.message = message;
    }
}
