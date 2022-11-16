package uk.co.gresearch.siembol.parsers.syslog;

import org.apache.commons.lang3.tuple.Pair;
import org.joda.time.DateTimeZone;
import uk.co.gresearch.siembol.parsers.extractors.ParserDateFormat;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 * An object for parsing a syslog message
 *
 * <p>This class implements a fault-tolerant syslog parser complaint with RFC 3164 and RFC 5424.
 * It parses a raw syslog message into a SyslogMessage object.
 *
 * @author Marian Novotny
 * @see SyslogMessage
 *
 */
public class SyslogParser {
    private static final String BOM_SIGNATURE = "BOM";
    private static final int MAX_PRIORITY = 255;
    private static final int PRI_HEADER_INDEX = 0;
    private static final int TIMESTAMP_HEADER_INDEX = 1;
    private static final int HOSTNAME_HEADER_INDEX = 2;
    private static final int APPNAME_HEADER_INDEX = 3;
    private static final int PROCID_HEADER_INDEX = 4;
    private static final int MSGID_HEADER_INDEX = 5;
    private static final int OTHER_DATA_INDEX = 6;
    private static final int SYSLOG_FIELDS_NUMBER = OTHER_DATA_INDEX + 1;
    private static final char NIL_VALUE_CHAR = '-';
    private static final char SP_VALUE_CHAR = ' ';
    private static final char LEFT_BRACKET_SD_ELEMENT = '[';
    private static final char RIGHT_BRACKET_SD_ELEMENT = ']';
    private static final String SP_VALUE = " ";
    private static final String PRI_REGEXP = "^<\\d{1,3}+>";
    private static final Pattern PRI_PATTERN = Pattern.compile(PRI_REGEXP);

    public static final int RFC_5424_VERSION = 1;
    public static final int RFC_3164_VERSION = 0;

    private final DateTimeZone dateTimeZone;
    private final List<ParserDateFormat> dateFormats;

    /**
     * Creates a SyslogParser that is using default Syslog timestamp parsing
     *
     * @param timeZone Time zone string for default syslog timestamp parsing
     */
    public SyslogParser(String timeZone) {
        dateTimeZone = DateTimeZone.forID(timeZone);
        dateFormats = null;
    }

    /**
     * Creates a SyslogParser that is using custom timestamp parsers
     *
     * @param dateFormats List of custom timestamp parsers
     * @see ParserDateFormat
     */
    public SyslogParser(List<ParserDateFormat> dateFormats) {
        this.dateFormats = dateFormats;
        this.dateTimeZone = null;
    }

    private SyslogMessage parsePri(String str, SyslogMessage message) {
        int endOffset = str.indexOf('>');

        int priority = Integer.valueOf(str.substring(1, endOffset));
        if (priority > MAX_PRIORITY) {
            throw new IllegalStateException(String.format(
                    "wrong Priority value: %d", priority));
        }
        message.setPriority(Integer.valueOf(str.substring(1, endOffset)));
        int headerVersion = 0;
        if (str.length() > endOffset + 1)
            headerVersion = str.charAt(str.length() - 1) - '0';

        if (headerVersion > RFC_5424_VERSION) {
            throw new IllegalStateException(String.format(
                    "Unsupported SYSLOG version: %d", headerVersion));
        }
        message.setHeaderVersion(headerVersion);
        return message;
    }

    private SyslogMessage setMsgField(String msg, SyslogMessage message) {
        message.setMsg(
                msg != null && msg.startsWith(BOM_SIGNATURE)
                        ? msg.substring(BOM_SIGNATURE.length())
                        : msg);
        return message;
    }

    private SyslogMessage parseBsdMessage(String str, int priOffset) {
        SyslogMessage message = new SyslogMessage();
        message.setHeaderVersion(RFC_3164_VERSION);
        message = parsePri(str.substring(0, priOffset), message);

        int hostStartOffset = priOffset + SyslogDefaultTimeFormat.getBsdTimestampSize() + 1;
        if (hostStartOffset >= str.length()
                || str.charAt(hostStartOffset - 1) != SP_VALUE_CHAR ) {
            throw new IllegalStateException("Missing host field in SYSLOG RFC 3164 header");
        }

        message.setTimestamp(SyslogDefaultTimeFormat
                .parseBsdTimestamp(
                        str.substring(priOffset, hostStartOffset - 1),
                        dateTimeZone));

        int hostEndOffset = str.indexOf(SP_VALUE_CHAR, hostStartOffset);
        if (hostEndOffset == -1) {
            throw new IllegalStateException("Missing SP after host in SYSLOG RFC 3164 header");
        }

        message.setHostname(
                str.substring(hostStartOffset, hostEndOffset));

        message = setMsgField(str.substring(hostEndOffset + 1), message);
        return message;
    }

    private int indexOfEscaped(String str, char c, int from) {
        int numQuotes = 0;
        int i = from;

        while (i < str.length()) {
            char current = str.charAt(i);
            if (current == '\\') {
                i++;
            } else {
                if (current == '"') numQuotes++;
                if (current == c && (numQuotes % 2 == 0 || c == '"')) return i;
            }
            i++;
        }

        return -1;
    }

    private SyslogMessage parseSdElement(String str, SyslogMessage message){
        List<Pair<String, String>> sdParameters = new ArrayList<>();

        int offset = str.indexOf(SP_VALUE_CHAR);
        if (offset == -1) {
            //NOTE: SD-PARAM are optional
            message.adSdElement(str, sdParameters);
            return message;
        }

        String sdElement = str.substring(0, offset);
        while (offset < str.length()) {
            int delimiter = str.indexOf('=', offset + 1);
            int endParam = indexOfEscaped(str, '"', delimiter + 2);
            if (str.charAt(offset) != SP_VALUE_CHAR ||
                    delimiter == -1 ||
                    endParam == -1 ||
                    str.charAt(delimiter + 1) != '"'){
                throw new IllegalStateException(String.format(
                        "Wrong SD-PARAM in sd element: %s",str));
            }

            String key = str.substring(offset + 1, delimiter);
            String value = str.substring(delimiter + 2, endParam);
            sdParameters.add(Pair.of(key, value));
            offset = endParam + 1;
        }
        message.adSdElement(sdElement, sdParameters);
        return message;
    }

    /**
     * Parses a syslog message
     *
     * @param str a raw syslog message
     * @return the parsed syslog message on success
     * @throws IllegalStateException if the parser reaches an invalid state during parsing a message
     * @see SyslogMessage
     */
    public SyslogMessage parse(String str) {
        Matcher priMatcher = PRI_PATTERN.matcher(str);
        if (!priMatcher.find()) {
            throw new IllegalStateException("invalid PRI header field in SYSLOG message");
        }

        int offset = priMatcher.end();
        if (!Character.isDigit(str.charAt(offset))) {
            //RFC 3164 - BSD SYSLOG TIMESTAMP starts after >
            return parseBsdMessage(str, offset);
        }

        SyslogMessage message = new SyslogMessage();
        String[] syslogFields = str.split(SP_VALUE, SYSLOG_FIELDS_NUMBER);
        if (syslogFields.length != SYSLOG_FIELDS_NUMBER) {
            throw new IllegalStateException("wrong number of fields in SYSLOG header");
        }

        message = parsePri(syslogFields[PRI_HEADER_INDEX], message);
        if (message.getHeaderVersion() > RFC_5424_VERSION) {
            throw new IllegalStateException(
                    String.format("unsupported version of SYSLOG protocol ver: %d",
                            message.getHeaderVersion()));

        }

        if (dateFormats != null) {
            Optional<Long> timeStamp = ParserDateFormat.parse(
                    dateFormats,
                    syslogFields[TIMESTAMP_HEADER_INDEX]);
            if (!timeStamp.isPresent()) {
                message.setTimestampStr(syslogFields[TIMESTAMP_HEADER_INDEX]);
            } else {
                message.setTimestamp(timeStamp.get());
            }
        } else {
            try {
                message.setTimestamp(SyslogDefaultTimeFormat.parseTimestamp(
                        syslogFields[TIMESTAMP_HEADER_INDEX],
                        dateTimeZone));
            } catch(Exception e) {
                message.setTimestampStr(syslogFields[TIMESTAMP_HEADER_INDEX]);
            }
        }

        message.setHostname(syslogFields[HOSTNAME_HEADER_INDEX]);
        message.setAppName(syslogFields[APPNAME_HEADER_INDEX]);
        message.setProcId(syslogFields[PROCID_HEADER_INDEX]);
        message.setMsgId(syslogFields[MSGID_HEADER_INDEX]);

        String data = syslogFields[OTHER_DATA_INDEX];
        int dataOffset = 0;
        if (data.length() == 0) {
            throw new IllegalStateException("missing SD elements in RFC5424 SYSLOG message");
        }

        if (data.charAt(dataOffset) == NIL_VALUE_CHAR) {
            //empty SD elements
            dataOffset++;
        } else {
            boolean validSdElements = false;
            while (dataOffset < data.length()) {

                if (data.charAt(dataOffset) != LEFT_BRACKET_SD_ELEMENT) {
                    throw new IllegalStateException("missing left bracket in a RFC5424 SYSLOG message");
                }
                int nextOffset = indexOfEscaped(data, RIGHT_BRACKET_SD_ELEMENT, dataOffset);

                message = parseSdElement(data.substring(dataOffset + 1, nextOffset), message);
                dataOffset  = nextOffset + 1;

                if (dataOffset == data.length()
                        || data.charAt(dataOffset) == SP_VALUE_CHAR) {
                    validSdElements = true;
                    break;
                }
            }
            if (!validSdElements) {
                throw new IllegalStateException("invalid SD Elements in RFC5424 SYSLOG message");
            }
        }

        if (dataOffset == data.length()) {
            //NOTE: MSG is optional
            return message;
        }

        if (data.charAt(dataOffset) != SP_VALUE_CHAR) {
            throw new IllegalStateException("invalid MSG field in RFC5424 SYSLOG message");
        }

        message = setMsgField(data.substring(dataOffset + 1), message);
        return message;
    }
}
