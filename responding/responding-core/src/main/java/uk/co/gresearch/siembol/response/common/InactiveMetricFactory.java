package uk.co.gresearch.siembol.response.common;

public class InactiveMetricFactory implements MetricFactory {
    private final MetricCounter inactiveCounter = () -> {};
    @Override
    public MetricCounter createCounter(String name, String description) {
        return inactiveCounter;
    }
}
