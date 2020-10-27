package uk.co.gresearch.siembol.response.stream.ruleservice;

import org.springframework.boot.actuate.health.Health;
import reactor.core.publisher.Mono;
import uk.co.gresearch.siembol.response.common.RespondingResult;

public interface RulesService {
    RespondingResult getRulesMetadata();
    Mono<Health> checkHealth();
    default void close() {}
}
