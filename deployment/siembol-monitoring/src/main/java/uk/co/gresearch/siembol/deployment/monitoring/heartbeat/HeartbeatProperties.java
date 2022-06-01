package uk.co.gresearch.siembol.deployment.monitoring.heartbeat;

import uk.co.gresearch.siembol.deployment.monitoring.heartbeat.HeartbeatConsumerProperties;
import uk.co.gresearch.siembol.deployment.monitoring.heartbeat.HeartbeatProducerProperties;

import java.util.Map;

public class HeartbeatProperties {
    private int heartbeatIntervalSeconds = 60;

    private Map<String, HeartbeatProducerProperties> heartbeatProducers;

    private HeartbeatConsumerProperties heartbeatConsumer;

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

    public HeartbeatConsumerProperties getHeartbeatConsumer() {
        return heartbeatConsumer;
    }

    public void setHeartbeatConsumer(HeartbeatConsumerProperties heartbeatConsumer) {
        this.heartbeatConsumer = heartbeatConsumer;
    }

    public Map<String, Object> getMessage() {
        return message;
    }

    public void setMessage(Map<String, Object> message) {
        this.message = message;
    }
}
