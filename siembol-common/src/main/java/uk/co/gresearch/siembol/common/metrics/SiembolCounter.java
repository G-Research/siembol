package uk.co.gresearch.siembol.common.metrics;
/**
 * An object for representing a counter
 *
 * <p>This interface is for representing a counter used in Siembol metrics.
 *
 * @author  Marian Novotny
 *
 */
public interface SiembolCounter {
    /**
     * Increments the counter by one
     */
    void increment();

    /**
     * Increments the counter by a value
     *
     * @param value a value to be added to the counter
     */
    void increment(int value);
}
