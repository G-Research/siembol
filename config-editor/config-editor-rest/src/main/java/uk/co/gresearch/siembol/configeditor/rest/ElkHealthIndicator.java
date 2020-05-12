package uk.co.gresearch.siembol.configeditor.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.ReactiveHealthIndicator;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import uk.co.gresearch.siembol.configeditor.service.elk.ElkService;

@Component
public class ElkHealthIndicator implements ReactiveHealthIndicator {
    @Autowired
    @Qualifier("elkService")
    private final ElkService elkService;

    public ElkHealthIndicator(
            @Qualifier("elkService") ElkService elkService) {
        this.elkService = elkService;
    }

    @Override
    public Mono<Health> health() {
        return checkDownstreamServiceHealth().onErrorResume(
                ex -> Mono.just(new Health.Builder().down(ex).build()));
    }

    private Mono<Health> checkDownstreamServiceHealth() {
        return Mono.just(elkService.checkHealth());
    }
}
