package uk.co.gresearch.siembol.alerts.correlationengine;

import java.util.EnumSet;
/**
 * An object that represents constant alert counter information
 *
 * <p>This object stores the constant alert counter metadata.
 * It is shared between all counters of the same type in order to save the space.
 *
 * @author  Marian Novotny
 * @see AlertCounter
 *
 */
public class AlertCounterMetadata {
    public enum Flags {
        MANDATORY
    }
    private final EnumSet<Flags> flags;
    private final int threshold;
    private final long extendedWindowSize;
    private final String alertName;

    public AlertCounterMetadata(String alertName, int threshold, long extendedWindowSize, EnumSet<Flags> flags) {
        this.alertName = alertName;
        this.threshold = threshold;
        this.extendedWindowSize = extendedWindowSize;
        this.flags = flags;
    }

    public int getThreshold() {
        return threshold;
    }

    public boolean isMandatory() {
        return flags.contains(Flags.MANDATORY);
    }

    public long getExtendedWindowSize() {
        return extendedWindowSize;
    }

    public String getAlertName() {
        return alertName;
    }

    public EnumSet<Flags> getFlags() {
        return flags;
    }
}
