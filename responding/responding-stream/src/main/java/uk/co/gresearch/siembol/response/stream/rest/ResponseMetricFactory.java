package uk.co.gresearch.siembol.response.stream.rest;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.gresearch.siembol.response.common.MetricCounter;
import uk.co.gresearch.siembol.response.common.MetricFactory;

@Service
public class ResponseMetricFactory implements MetricFactory {
    @Autowired
    private final MeterRegistry registry;

    public ResponseMetricFactory(MeterRegistry registry) {
        this.registry = registry;
    }

    @Override
    public MetricCounter createCounter(String name, String description) {
        return new ResponseCounter(Counter.builder(name).description(description).register(registry));
    }
}
