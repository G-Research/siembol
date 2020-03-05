package uk.co.gresearch.nortem.parsers.syslog;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.joda.time.format.DateTimeParser;

import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

/**
 * The class provides default time formatting for syslog messages
 */
public class SyslogDefaultTimeFormat {
    private static final int WEEK_MILLISECONDS =
            7 * 24 * 3600 * 1000;
    private static final String ISODateFormat =
            "yyyy-MM-dd'T'HH:mm:ss";
    private static final String ISODateFormatRegexp =
            "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}$";
    private static final DateTimeFormatter ISODateFormatter =
            DateTimeFormat.forPattern(ISODateFormat);
    private static final Pattern ISODateFormatPattern =
            Pattern.compile(ISODateFormatRegexp);

    private static final String ISODateFormatZ = ISODateFormat + "Z";
    private static final DateTimeFormatter ISODateFormatterZ =
            DateTimeFormat.forPattern(ISODateFormatZ);
    private static final String ISODateFormatZRegexp =
            "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}[+-]\\d{2}:\\d{2}$";
    private static final Pattern ISODateFormatZPattern =
            Pattern.compile(ISODateFormatZRegexp);

    private static final String ISODateFormatMS =
            "yyyy-MM-dd'T'HH:mm:ss.SSSSSS";
    private static final String ISODateFormatMSRegexp =
            "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{6}$";
    private static final DateTimeFormatter ISODateFormatterMS =
            DateTimeFormat.forPattern(ISODateFormatMS);
    private static final Pattern ISODateFormatPatternMS =
            Pattern.compile(ISODateFormatMSRegexp);

    private static final String ISODateFormatMSZ = ISODateFormatMS + 'Z';
    private static final String ISODateFormatMSZRegexp =
            "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{6}[+-]\\d{2}:\\d{2}$";
    private static final DateTimeFormatter ISODateFormatterMSZ =
            DateTimeFormat.forPattern(ISODateFormatMSZ);
    private static final Pattern ISODateFormatPatternMSZ =
            Pattern.compile(ISODateFormatMSZRegexp);

    private static final String BSDTimestampFormat1 =
            "MMM dd HH:mm:ss";
    private static final String BSDTimestampFormat2 =
            "MMM  d HH:mm:ss";

    private static final DateTimeFormatter BSDTimestampFormatter1 =
            DateTimeFormat.forPattern(BSDTimestampFormat1);
    private static final DateTimeFormatter BSDTimestampFormatter2 =
            DateTimeFormat.forPattern(BSDTimestampFormat2);

    private final static DateTimeFormatter BSDTimestampFormatter =
            new DateTimeFormatterBuilder()
                    .append(null, new DateTimeParser[]{
                            BSDTimestampFormatter1.getParser(),
                            BSDTimestampFormatter2.getParser()})
                    .toFormatter();

    public static int getBsdTimestampSize() {
        return BSDTimestampFormat1.length();
    }

    public static long parseBsdTimestamp(String message, DateTimeZone dateTimeZone) {
        try {
            ZonedDateTime currentDate = ZonedDateTime.now();
            int currentYear = currentDate.getYear();
            long timestamp = BSDTimestampFormatter
                    .withDefaultYear(currentYear)
                    .withZone(dateTimeZone)
                    .parseDateTime(message)
                    .getMillis();

            //NOTE: detection of using the next year assuming that we do not have a week old logs
            if (System.currentTimeMillis() + WEEK_MILLISECONDS < timestamp) {
                //we use previous year
                timestamp = BSDTimestampFormatter
                        .withDefaultYear(currentYear - 1)
                        .withZone(dateTimeZone)
                        .parseDateTime(message)
                        .getMillis();
            }
            return timestamp;
        } catch (DateTimeParseException e) {
            throw new IllegalStateException(
                    String.format("unsupported syslog RFC316 timestamp: %s",
                            message));
        }
    }

    public static long parseTimestamp(String message, DateTimeZone dateTimeZone) {
        DateTime dateTime;
        try {
            if (ISODateFormatZPattern.matcher(message).matches()) {
                dateTime = DateTime.parse(message, ISODateFormatterZ.withZone(dateTimeZone));
            } else if (ISODateFormatPattern.matcher(message).matches()) {
                dateTime = DateTime.parse(message, ISODateFormatter.withZone(dateTimeZone));
            } else if (ISODateFormatPatternMS.matcher(message).matches()) {
                dateTime = DateTime.parse(message, ISODateFormatterMS.withZone(dateTimeZone));
            } else if (ISODateFormatPatternMSZ.matcher(message).matches()) {
                dateTime = DateTime.parse(message, ISODateFormatterMSZ.withZone(dateTimeZone));
            } else {
                //NOTE: unssupported default time format from RFC 5424
                throw new IllegalStateException(
                        String.format("unsupported syslog RFC 5424 timestamp: %s",
                                message));
            }
            return dateTime.getMillis();
        } catch (Exception e) {
            throw new IllegalStateException(
                    String.format("unsupported syslog RFC 5424 timestamp: %s",
                            message));
        }
    }
}
