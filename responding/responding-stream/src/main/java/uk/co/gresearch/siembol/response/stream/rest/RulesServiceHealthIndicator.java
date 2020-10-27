package uk.co.gresearch.siembol.response.stream.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.ReactiveHealthIndicator;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import uk.co.gresearch.siembol.response.stream.ruleservice.RulesService;

@Component
public class RulesServiceHealthIndicator implements ReactiveHealthIndicator {
    @Autowired
    private final RulesService rulesService;

    public RulesServiceHealthIndicator(RulesService rulesService) {
        this.rulesService = rulesService;
    }

    @Override
    public Mono<Health> health() {
        return checkDownstreamServiceHealth().onErrorResume(
                ex -> Mono.just(new Health.Builder().down(ex).build()));
    }

    private Mono<Health> checkDownstreamServiceHealth() {
        return rulesService.checkHealth();
    }
}