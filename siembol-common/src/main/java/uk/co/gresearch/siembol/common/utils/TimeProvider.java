package uk.co.gresearch.siembol.common.utils;

import java.time.LocalDateTime;
/**
 * An object that implements a time provider
 *
 * <p>This class implements a time provider that allows easy testing of the code that requires the current time.
 *
 * @author  Marian Novotny
 */
public class TimeProvider {
    public int getDays() {
        return LocalDateTime.now().getDayOfYear();
    }

    public int getHour() {
        return LocalDateTime.now().getHour();
    }

    public long getCurrentTimeInMs() { return System.currentTimeMillis();}
}
