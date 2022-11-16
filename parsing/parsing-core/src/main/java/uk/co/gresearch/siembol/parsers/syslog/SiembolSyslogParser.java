package uk.co.gresearch.siembol.parsers.syslog;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.gresearch.siembol.parsers.common.SiembolParser;
import uk.co.gresearch.siembol.common.constants.SiembolMessageFields;
import uk.co.gresearch.siembol.parsers.extractors.ParserDateFormat;
import uk.co.gresearch.siembol.parsers.extractors.ParserExtractor;
import uk.co.gresearch.siembol.parsers.transformations.Transformation;
import uk.co.gresearch.siembol.parsers.transformations.TransformationsLibrary;
import java.util.*;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;
import static uk.co.gresearch.siembol.common.constants.SiembolMessageFields.ORIGINAL;
/**
 * An object for syslog parsing
 *
 * <p>This class is an implementation of SiembolParser interface.
 * It is used for parsing a log message using RFC 3164 or RFC 5424 compliant fault-tolerant syslog parser.
 *
 * It evaluates chain of extractors and transformations if registered.
 * @author  Marian Novotny
 * @see SiembolParser
 *
 */
public class SiembolSyslogParser implements SiembolParser {
    public enum Flags {
        EXPECT_RFC_3164_VERSION,
        EXPECT_RFC_RFC5424_VERSION,
        MERGE_SD_ELEMENTS,
    }
    private static final Logger LOG = LoggerFactory.getLogger(SiembolSyslogParser.class);
    public static final String SYSLOG_HEADER_VERSION = "syslog_version";
    public static final String SYSLOG_HEADER_FACILITY = "syslog_facility";
    public static final String SYSLOG_HEADER_SEVERITY = "syslog_severity";
    public static final String SYSLOG_HEADER_PRIORITY = "syslog_priority";
    public static final String SYSLOG_HEADER_TIMESTAMP = SiembolMessageFields.TIMESTAMP.toString();
    public static final String SYSLOG_HEADER_HOSTNAME = "syslog_hostname";
    public static final String SYSLOG_HEADER_APP_NAME = "syslog_appname";
    public static final String SYSLOG_HEADER_PROC_ID = "syslog_proc_id";
    public static final String SYSLOG_HEADER_MSG_ID = "syslog_msg_id";
    public static final String SYSLOG_SD_ID = "syslog_sd_id";
    public static final String SYSLOG_MSG = "syslog_msg";
    public static final String SYSLOG_TIMESTAMP_STR = "syslog_timestamp";

    private final List<ParserExtractor> extractors;
    private final List<Transformation> transformations;
    private final SyslogParser parser;
    private final EnumSet<Flags> flags;

    private SiembolSyslogParser(Builder builder) {
        extractors = builder.extractors;
        transformations = builder.transformations;
        parser = builder.parser;
        flags = builder.flags;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Map<String, Object>> parse(byte[] bytes) {

        String originalMessage = new String(bytes, UTF_8);
        try {
            List<Map<String, Object>> ret = new ArrayList<>();
            Map<String, Object> syslogObject = new HashMap<>();

            SyslogMessage syslogMessage = parser.parse(originalMessage);
            if (!((flags.contains(Flags.EXPECT_RFC_3164_VERSION)
                    && syslogMessage.getHeaderVersion() == SyslogParser.RFC_3164_VERSION)
                    || (flags.contains(Flags.EXPECT_RFC_RFC5424_VERSION)
                    && syslogMessage.getHeaderVersion() == SyslogParser.RFC_5424_VERSION))){
                throw new IllegalStateException("Unexpected syslog version");
            }

            syslogObject.put(SYSLOG_HEADER_VERSION, syslogMessage.getHeaderVersion());
            syslogObject.put(SYSLOG_HEADER_FACILITY, syslogMessage.getFacility());
            syslogObject.put(SYSLOG_HEADER_SEVERITY, syslogMessage.getSeverity());
            syslogObject.put(SYSLOG_HEADER_PRIORITY, syslogMessage.getPriority());
            syslogObject.put(SYSLOG_HEADER_TIMESTAMP, syslogMessage.getTimestamp());
            syslogObject.put(ORIGINAL.getName(), originalMessage);

            if (syslogMessage.getHostname().isPresent()) {
                syslogObject.put(SYSLOG_HEADER_HOSTNAME,
                        syslogMessage.getHostname().get());
            }

            if (syslogMessage.getMsg().isPresent()) {
                syslogObject.put(SYSLOG_MSG, syslogMessage.getMsg().get());
            }

            if (syslogMessage.getHeaderVersion() != SyslogParser.RFC_3164_VERSION) {
                if (syslogMessage.getAppName().isPresent()) {
                    syslogObject.put(SYSLOG_HEADER_APP_NAME, syslogMessage.getAppName().get());
                }

                if (syslogMessage.getProcId().isPresent()) {
                    syslogObject.put(SYSLOG_HEADER_PROC_ID, syslogMessage.getProcId().get());
                }

                if (syslogMessage.getMsgId().isPresent()) {
                    syslogObject.put(SYSLOG_HEADER_MSG_ID, syslogMessage.getMsgId().get());
                }

                if (syslogMessage.getTimestampStr().isPresent()) {
                    syslogObject.put(SYSLOG_TIMESTAMP_STR, syslogMessage.getTimestampStr().get());
                }
            }

            int i = 0;
            for (Pair<String, List<Pair<String, String>>> sdElement : syslogMessage.getSdElements()) {
                Map<String, Object> currentElements = new HashMap<>();
                sdElement.getValue().forEach(item -> currentElements.put(item.getKey(), item.getValue()));
                if (flags.contains(Flags.MERGE_SD_ELEMENTS)) {
                    //we merge sd elements into syslogObject that will be returned
                    currentElements.put(String.format("%s_%d", SYSLOG_SD_ID, i++),
                            sdElement.getKey());
                    syslogObject.putAll(currentElements);
                } else {
                    //we add syslogObject that contains header and msg into sdElements
                    currentElements.put(SYSLOG_SD_ID, sdElement.getKey());
                    currentElements.putAll(syslogObject);
                    ret.add(currentElements);
                }
            }

            if (ret.isEmpty()) {
                ret.add(syslogObject);
            }

            return ret.stream()
                    .map(x -> extractAndTransform(x))
                    .filter(x -> !x.isEmpty())
                    .collect(Collectors.toList());
        } catch (Exception e) {
            String errorMessage = String.format("Unable to parse message: %s", originalMessage);
            LOG.debug(errorMessage, e);
            throw new IllegalStateException(errorMessage, e);
        }
    }

    private Map<String, Object> extractAndTransform(Map<String, Object> message) {
        Map<String, Object> ret = message;
        if (extractors != null) {
            ret = ParserExtractor.extract(extractors, ret);
        }

        if (transformations != null) {
            ret = TransformationsLibrary.transform(transformations, ret);
        }
        return ret;
    }

    public static class Builder {
        private List<ParserExtractor> extractors;
        private List<Transformation> transformations;
        private SyslogParser parser;
        private EnumSet<Flags> flags;
        private List<ParserDateFormat> dateFormats;
        private String timezone;

        public Builder extractors(List<ParserExtractor> extractors) {
            this.extractors = extractors;
            return this;
        }

        public Builder transformations(List<Transformation> transformations) {
            this.transformations = transformations;
            return this;
        }

        public Builder dateFormats(List<ParserDateFormat> dateFormats) {
            this.dateFormats = dateFormats;
            return this;
        }

        public Builder timezone(String timezone) {
            this.timezone = timezone;
            return this;
        }

        public Builder flags(EnumSet<Flags> flags) {
            this.flags = flags;
            return this;
        }

        public SiembolSyslogParser build() {
            parser = dateFormats != null
                    ? new SyslogParser(dateFormats)
                    : new SyslogParser(timezone);

            return new SiembolSyslogParser(this);
        }
    }
}
