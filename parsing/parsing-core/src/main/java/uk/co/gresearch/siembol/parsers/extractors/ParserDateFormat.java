package uk.co.gresearch.siembol.parsers.extractors;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalQueries;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
/**
 * An object that formats a string timestamps into a millisecond epoch time
 *
 * <p>This object validates, parses and formats a string timestamp into a millisecond epoch time.
 * It supports list of formatters that are executed in a chain until the first formatter is successful.
 *
 * @author  Marian Novotny
 */
public class ParserDateFormat {
    private static final String DEFAULT_TIMEZONE = "UTC";
    private final Pattern dateTimePattern;
    private final DateTimeFormatter dateFormatter;
    private final ZoneOffset zoneOffSet;

    public ParserDateFormat(String dateFormat) {
        this(dateFormat, Optional.empty(), Optional.empty());
    }

    public ParserDateFormat(String dateFormat, Optional<String> timeZone, Optional<String> regexPattern) {
        ZoneId zone = ZoneId.of(timeZone.orElse(DEFAULT_TIMEZONE));

        LocalDateTime now = LocalDateTime.now();
        zoneOffSet = zone.getRules().getOffset(now);

        dateFormatter = DateTimeFormatter.ofPattern(dateFormat).withZone(zone);
        dateTimePattern = regexPattern.isPresent()
                ? Pattern.compile(regexPattern.get())
                : null;
    }

    public Optional<Long> parse(String message) {
        if (dateTimePattern != null
                && !dateTimePattern.matcher(message).matches()) {
            return Optional.empty();
        }

        try {
            TemporalAccessor temporalAccessor = dateFormatter.parse(message);
            LocalDateTime dateTime = LocalDateTime.from(temporalAccessor);
            ZoneOffset currentOffset = temporalAccessor.query(TemporalQueries.offset());
            if (currentOffset == null) {
                currentOffset = zoneOffSet;
            }

            return Optional.of(dateTime.toInstant(currentOffset).toEpochMilli());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public static Optional<Long> parse(List<ParserDateFormat> dateFormats, String message) {
        for (ParserDateFormat dateFormat : dateFormats) {
            Optional<Long> ret = dateFormat.parse(message);
            if (ret.isPresent()) {
                return ret;
            }
        }
        return Optional.empty();
    }
}
