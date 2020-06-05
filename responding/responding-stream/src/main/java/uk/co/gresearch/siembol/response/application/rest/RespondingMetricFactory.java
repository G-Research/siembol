package uk.co.gresearch.siembol.response.application.rest;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.gresearch.siembol.response.common.MetricCounter;
import uk.co.gresearch.siembol.response.common.MetricFactory;

@Service
public class RespondingMetricFactory implements MetricFactory {
    @Autowired
    private final MeterRegistry registry;

    public RespondingMetricFactory(MeterRegistry registry) {
        this.registry = registry;
    }

    @Override
    public MetricCounter createCounter(String name, String description) {
        return new RespondingCounter(Counter.builder(name).description(description).register(registry));
    }
}
