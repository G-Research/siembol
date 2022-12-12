package uk.co.gresearch.siembol.common.metrics;
/**
 * An object for registering metrics
 *
 * <p>This interface is for registering metrics such as counters and gauges.
 *
 * @author  Marian Novotny
 *
 */
public interface SiembolMetricsRegistrar {
    /**
     * Registers a counter by its name
     * @param name the name of the counter
     * @return a siembol counter that has been created and registered
     */
    SiembolCounter registerCounter(String name);

    /**
     * Registers a counter by its name
     * @param name the name of the counter
     * @return a siembol counter that has been created and registered
     */
    SiembolGauge registerGauge(String name);

    /**
     * Gets a cached registrar
     *
     * @return a cached registrar
     */
    default SiembolMetricsRegistrar cachedRegistrar() {
        return new SiembolMetricsCachedRegistrar(this);
    }
}
