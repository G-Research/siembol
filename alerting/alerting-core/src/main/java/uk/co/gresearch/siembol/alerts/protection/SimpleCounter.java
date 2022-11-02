package uk.co.gresearch.siembol.alerts.protection;

import uk.co.gresearch.siembol.common.utils.TimeProvider;
/**
 * An object that counts hourly and daily matches
 *
 * <p>This object counts hourly and daily matches using simple counting and
 * current time provided by TimeProvider.
 *
 * @author  Marian Novotny
 *
 */
public class SimpleCounter {
    private final TimeProvider timeProvider;
    private int lastDay = -1;
    private int lastHour = -1;
    private int dailyMatches = 0;
    private int hourlyMatches = 0;

    public SimpleCounter() {
        this.timeProvider = new TimeProvider();
    }

    SimpleCounter(TimeProvider timeProvider) {
        this.timeProvider = timeProvider;
    }

    public void updateAndIncrement() {
        int currentHour = timeProvider.getHour();
        int currentDay = timeProvider.getDays();

        if (lastDay != currentDay) {
            lastDay = currentDay;
            lastHour = currentHour;
            dailyMatches = 0;
            hourlyMatches = 0;
        } else if (lastHour != currentHour) {
            lastHour = currentHour;
            hourlyMatches = 0;
        }

        dailyMatches++;
        hourlyMatches++;
    }

    public int getDailyMatches() {
        return dailyMatches;
    }

    public int getHourlyMatches() {
        return hourlyMatches;
    }
}
