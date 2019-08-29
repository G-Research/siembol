package uk.co.gresearch.nortem.common.utils;

import java.time.LocalDateTime;

public class TimeProvider {
    public int getDays() {
        return LocalDateTime.now().getDayOfYear();
    }

    public int getHour() {
        return LocalDateTime.now().getHour();
    }

    public long getCurrentTimeInMs() { return System.currentTimeMillis();}
}
