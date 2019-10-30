package uk.co.gresearch.nortem.parsers.common;

import java.util.List;
import java.util.Map;

public interface NortemParser {

    default List<Map<String, Object>> parse(String metadata, byte[] message) {
        return parse(message);
    }

    List<Map<String, Object>> parse(byte[] message);

    @Deprecated
    default ParserResult parseToResult(byte[] message) {
        ParserResult result = new ParserResult();
        result.setSourceType(getSourceType());
        try {
            List<Map<String, Object>> parsed = parse(message);
            parsed.forEach(x -> x.put(ParserFields.SENSOR_TYPE.toString(), getSourceType()));
            result.setParsedMessages(parsed);
        } catch (Throwable e) {
            result.setException(e);
        }
        return result;
    }

    default ParserResult parseToResult(String metadata, byte[] message) {
        ParserResult result = new ParserResult();
        result.setSourceType(getSourceType());
        try {
            List<Map<String, Object>> parsed = parse(metadata, message);
            parsed.forEach(x -> x.put(ParserFields.SENSOR_TYPE.toString(), getSourceType()));
            result.setParsedMessages(parsed);
        } catch (Throwable e) {
            result.setException(e);
        }
        return result;
    }

    default String getSourceType() {
        return "unknown";
    }
}
