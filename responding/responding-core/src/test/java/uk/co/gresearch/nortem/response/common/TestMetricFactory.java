package uk.co.gresearch.nortem.response.common;
import java.util.HashMap;
import java.util.Map;

public class TestMetricFactory implements MetricFactory {

    private final Map<String, TestMetricCounter> counters = new HashMap<>();

    @Override
    public MetricCounter createCounter(String name, String description) {
        TestMetricCounter newCounter = new TestMetricCounter(name, description);
        counters.put(name, newCounter);
        return newCounter;
    }

    public TestMetricCounter getCounter(String name) {
        return counters.get(name);
    }
}
