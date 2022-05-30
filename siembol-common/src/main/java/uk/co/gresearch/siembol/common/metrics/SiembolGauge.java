package uk.co.gresearch.siembol.common.metrics;


import com.google.common.util.concurrent.AtomicDouble;

public class SiembolGauge {
    private final AtomicDouble value = new AtomicDouble(0);

    public void setValue(double value) {
        this.value.set(value);
    }

    public double getValue() {
        return value.doubleValue();
    }
}
