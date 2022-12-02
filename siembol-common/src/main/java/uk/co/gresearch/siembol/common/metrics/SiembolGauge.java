package uk.co.gresearch.siembol.common.metrics;


import com.google.common.util.concurrent.AtomicDouble;
/**
 * An object for representing a gauge
 *
 * <p>This class is for representing a gauge with initial value of 0 used in Siembol metrics.
 *
 * @author  Marian Novotny
 *
 */
public class SiembolGauge {
    private final AtomicDouble value = new AtomicDouble(0);

    /**
     * Sets the value
     * @param value a value to be set in the gauge
     */
    public void setValue(double value) {
        this.value.set(value);
    }

    /**
     * Gets the value
     * @return a value of the gauge
     */
    public double getValue() {
        return value.doubleValue();
    }
}
