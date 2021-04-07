package uk.co.gresearch.siembol.deployment.storm.application;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.ReactiveHealthIndicator;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import uk.co.gresearch.siembol.deployment.storm.service.TopologyManagerService;

@Component
public class TopologyManagerHealthIndicator implements ReactiveHealthIndicator {
    @Autowired
    private final TopologyManagerService topologyManagerService;

    public TopologyManagerHealthIndicator(TopologyManagerService topologyManagerService) {
        this.topologyManagerService = topologyManagerService;
    }

    @Override
    public Mono<Health> health() {
        return checkDownstreamServiceHealth().onErrorResume(
                ex -> Mono.just(new Health.Builder().down(ex).build()));
    }

    private Mono<Health> checkDownstreamServiceHealth() {
        return Mono.just(topologyManagerService.checkHealth());
    }
}
