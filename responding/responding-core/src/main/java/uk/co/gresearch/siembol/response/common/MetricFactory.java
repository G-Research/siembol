package uk.co.gresearch.siembol.response.common;

public interface MetricFactory {
    MetricCounter createCounter(String name, String description);
}
