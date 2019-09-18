package uk.co.gresearch.nortem.parsers.common;

import java.util.List;
import java.util.Map;

public interface NortemParser {

    List<Map<String, Object>> parse(byte[] message);

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

    default String getSourceType() {
        return "unknown";
    }
}
