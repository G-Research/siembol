package uk.co.gresearch.siembol.common.metrics;

public interface SiembolCounter {
    void increment();

    void increment(int value);
}
