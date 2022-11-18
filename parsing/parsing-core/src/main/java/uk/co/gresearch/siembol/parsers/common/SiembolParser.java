package uk.co.gresearch.siembol.parsers.common;

import uk.co.gresearch.siembol.common.constants.SiembolMessageFields;

import java.util.List;
import java.util.Map;
/**
 * An object for parsing a log
 *
 * <p>This interface for parsing a log and providing metadata for a caller such as
 * the parser source type.
 *
 * @author  Marian Novotny
 *
 */
public interface SiembolParser {
    /**
     * Parses the message along with metadata
     *
     * @param metadata Metadata about the message as a json string
     * @param message Message as a byte array. Both binary and text logs are supported.
     * @return list of parsed messages - maps of Strings to Objects
     */
    default List<Map<String, Object>> parse(String metadata, byte[] message) {
        return parse(message);
    }

    /**
     * Parses the message along with metadata
     *
     * @param message Message as a byte array. Both binary and text logs are supported.
     * @return list of parsed messages - maps of Strings to Objects
     */
    List<Map<String, Object>> parse(byte[] message);

    /**
     * Parses the message along with metadata into a ParserResult structure
     *
     * @param metadata Metadata about the message as a json string
     * @param message Message as a byte array. Both binary and text logs are supported.
     * @return parser result with list of parsed messages and metadata about the parsing result
     * @see ParserResult
     */
    default ParserResult parseToResult(String metadata, byte[] message) {
        ParserResult result = new ParserResult();
        result.setSourceType(getSourceType());
        try {
            List<Map<String, Object>> parsed = parse(metadata, message);
            parsed.forEach(x -> x.put(SiembolMessageFields.SENSOR_TYPE.toString(), getSourceType()));
            result.setParsedMessages(parsed);
        } catch (Throwable e) {
            result.setException(e);
        }
        return result;
    }

    /**
     * Provides the name of the parser source type
     *
     * @return parser source type
     */
    default String getSourceType() {
        return "unknown";
    }
}
