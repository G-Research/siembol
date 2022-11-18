package uk.co.gresearch.siembol.parsers.syslog;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.joda.time.format.DateTimeParser;

import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * A class with static methods for default syslog timestamp parsing
 *
 * <p>This class exposes static methods for default timestamp syslog parsing.
 *
 * @author  Marian Novotny
 * @see SyslogParser
 *
 */
public class SyslogDefaultTimeFormat {
    private static final int WEEK_MILLISECONDS = 7 * 24 * 3600 * 1000;
    private static final String ISO_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
    private static final String ISO_DATE_FORMAT_REGEX = "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}$";
    private static final DateTimeFormatter ISO_DATE_FORMATTER = DateTimeFormat.forPattern(ISO_DATE_FORMAT);
    private static final Pattern ISO_DATE_FORMAT_PATTERN = Pattern.compile(ISO_DATE_FORMAT_REGEX);
    private static final String ISO_DATE_FORMAT_Z = ISO_DATE_FORMAT + "Z";
    private static final DateTimeFormatter ISO_DATE_FORMATTER_Z = DateTimeFormat.forPattern(ISO_DATE_FORMAT_Z);
    private static final String ISO_DATE_FORMAT_Z_REGEX =
            "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}[+-]\\d{2}:\\d{2}$";
    private static final Pattern ISO_DATE_FORMAT_Z_PATTERN = Pattern.compile(ISO_DATE_FORMAT_Z_REGEX);
    private static final String ISO_DATE_FORMAT_MS = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS";
    private static final String ISO_DATE_FORMAT_MS_REGEX = "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{6}$";
    private static final DateTimeFormatter ISO_DATE_FORMATTER_MS = DateTimeFormat.forPattern(ISO_DATE_FORMAT_MS);
    private static final Pattern ISO_DATE_FORMAT_PATTERN_MS = Pattern.compile(ISO_DATE_FORMAT_MS_REGEX);

    private static final String ISO_DATE_FORMAT_MS_Z = ISO_DATE_FORMAT_MS + 'Z';
    private static final String ISO_DATE_FORMAT_MS_Z_REGEX =
            "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{6}[+-]\\d{2}:\\d{2}$";
    private static final DateTimeFormatter ISO_DATE_FORMATTER_MS_Z = DateTimeFormat.forPattern(ISO_DATE_FORMAT_MS_Z);
    private static final Pattern ISO_DATE_FORMAT_PATTERN_MS_Z = Pattern.compile(ISO_DATE_FORMAT_MS_Z_REGEX);

    private static final String BSD_TIMESTAMP_FORMAT_1 = "MMM dd HH:mm:ss";
    private static final String BSD_TIMESTAMP_FORMAT_2 = "MMM  d HH:mm:ss";
    private final static DateTimeFormatter BSD_TIMESTAMP_FORMATTER = new DateTimeFormatterBuilder()
            .append(null, new DateTimeParser[]{
                    DateTimeFormat.forPattern(BSD_TIMESTAMP_FORMAT_1).getParser(),
                    DateTimeFormat.forPattern(BSD_TIMESTAMP_FORMAT_2).getParser()})
            .toFormatter();

    public static int getBsdTimestampSize() {
        return BSD_TIMESTAMP_FORMAT_1.length();
    }

    public static long parseBsdTimestamp(String message, DateTimeZone dateTimeZone) {
        try {
            ZonedDateTime currentDate = ZonedDateTime.now();
            int currentYear = currentDate.getYear();
            long timestamp = BSD_TIMESTAMP_FORMATTER
                    .withLocale(Locale.ENGLISH)
                    .withDefaultYear(currentYear)
                    .withZone(dateTimeZone)
                    .parseDateTime(message)
                    .getMillis();

            //NOTE: detection of using the next year assuming that we do not have a week old logs
            if (System.currentTimeMillis() + WEEK_MILLISECONDS < timestamp) {
                //we use previous year
                timestamp = BSD_TIMESTAMP_FORMATTER
                        .withLocale(Locale.ENGLISH)
                        .withDefaultYear(currentYear - 1)
                        .withZone(dateTimeZone)
                        .parseDateTime(message)
                        .getMillis();
            }
            return timestamp;
        } catch (DateTimeParseException e) {
            throw new IllegalStateException(
                    String.format("unsupported syslog RFC316 timestamp: %s", message));
        }
    }

    public static long parseTimestamp(String message, DateTimeZone dateTimeZone) {
        DateTime dateTime;
        try {
            if (ISO_DATE_FORMAT_Z_PATTERN.matcher(message).matches()) {
                dateTime = DateTime.parse(message, ISO_DATE_FORMATTER_Z.withZone(dateTimeZone));
            } else if (ISO_DATE_FORMAT_PATTERN.matcher(message).matches()) {
                dateTime = DateTime.parse(message, ISO_DATE_FORMATTER.withZone(dateTimeZone));
            } else if (ISO_DATE_FORMAT_PATTERN_MS.matcher(message).matches()) {
                dateTime = DateTime.parse(message, ISO_DATE_FORMATTER_MS.withZone(dateTimeZone));
            } else if (ISO_DATE_FORMAT_PATTERN_MS_Z.matcher(message).matches()) {
                dateTime = DateTime.parse(message, ISO_DATE_FORMATTER_MS_Z.withZone(dateTimeZone));
            } else {
                //NOTE: unssupported default time format from RFC 5424
                throw new IllegalStateException(
                        String.format("unsupported syslog RFC 5424 timestamp: %s", message));
            }
            return dateTime.getMillis();
        } catch (Exception e) {
            throw new IllegalStateException(
                    String.format("unsupported syslog RFC 5424 timestamp: %s", message));
        }
    }
}
