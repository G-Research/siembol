package uk.co.gresearch.nortem.response.common;

public class TestMetricCounter implements MetricCounter {
    private int counter = 0;
    private final String name;
    private final String description;

    public TestMetricCounter(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public void increment() {
        counter++;
    }

    public int getValue() {
        return counter;
    }
}
