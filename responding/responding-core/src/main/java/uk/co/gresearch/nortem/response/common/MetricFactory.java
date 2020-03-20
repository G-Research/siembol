package uk.co.gresearch.nortem.response.common;

public interface MetricFactory {
    MetricCounter createCounter(String name, String description);
}
