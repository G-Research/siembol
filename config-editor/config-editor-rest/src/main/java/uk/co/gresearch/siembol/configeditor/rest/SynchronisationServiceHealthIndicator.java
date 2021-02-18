package uk.co.gresearch.siembol.configeditor.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import uk.co.gresearch.siembol.configeditor.sync.service.StormApplicationProvider;
import uk.co.gresearch.siembol.configeditor.sync.service.SynchronisationService;

@Component
@ConditionalOnProperty(prefix = "config-editor", value = "synchronisation")
public class SynchronisationServiceHealthIndicator implements ReactiveHealthIndicator {
    private final SimpleStatusAggregator statusAggregator = new SimpleStatusAggregator();
    @Autowired
    private StormApplicationProvider stormApplicationProvider;
    @Autowired
    private SynchronisationService synchronisationService;

    @Override
    public Mono<Health> health() {
        return checkDownstreamServiceHealth().onErrorResume(
                ex -> Mono.just(new Health.Builder().down(ex).build()));
    }

    private Mono<Health> checkDownstreamServiceHealth() {
        Status status = statusAggregator.getAggregateStatus(
                stormApplicationProvider.checkHealth().getStatus(),
                synchronisationService.checkHealth().getStatus());
        return Mono.just(new Health.Builder().status(status).build());
    }
}