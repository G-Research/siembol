package uk.co.gresearch.siembol.parsers.common;

import uk.co.gresearch.siembol.common.constants.SiembolMessageFields;

import java.util.List;
import java.util.Map;

public interface SiembolParser {

    default List<Map<String, Object>> parse(String metadata, byte[] message) {
        return parse(message);
    }

    List<Map<String, Object>> parse(byte[] message);

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

    default String getSourceType() {
        return "unknown";
    }
}
