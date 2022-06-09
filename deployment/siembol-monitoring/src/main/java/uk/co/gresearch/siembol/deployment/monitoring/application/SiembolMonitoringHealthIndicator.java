package uk.co.gresearch.siembol.deployment.monitoring.application;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.ReactiveHealthIndicator;
import org.springframework.boot.actuate.health.SimpleStatusAggregator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import uk.co.gresearch.siembol.deployment.monitoring.heartbeat.HeartbeatConsumer;
import uk.co.gresearch.siembol.deployment.monitoring.heartbeat.HeartbeatProducer;


@Component
public class SiembolMonitoringHealthIndicator implements ReactiveHealthIndicator {
    private final SimpleStatusAggregator statusAggregator = new SimpleStatusAggregator();
    @Autowired
    private HeartbeatProducer heartbeatProducer;
    @Autowired
    private HeartbeatConsumer heartbeatConsumer;

    @Override
    public Mono<Health> health() {
        return checkDownstreamServiceHealth().onErrorResume(
                ex -> Mono.just(new Health.Builder().down(ex).build()));
    }

    private Mono<Health> checkDownstreamServiceHealth() {
        Status status = statusAggregator.getAggregateStatus(
                heartbeatProducer.checkHealth().getStatus(),
                heartbeatConsumer.checkHealth().getStatus());
        return Mono.just(new Health.Builder().status(status).build());
    }
}

