package uk.co.gresearch.siembol.common.metrics;

public interface SiembolMetricsRegistrar {
    SiembolCounter registerCounter(String name);

    SiembolGauge registerGauge(String name);

    default SiembolMetricsRegistrar cachedRegistrar() {
        return new SiembolMetricsCachedRegistrar(this);
    }
}
