package uk.co.gresearch.nortem.parsers.common;

import java.util.List;
import java.util.Map;

public interface NortemParser {

    List<Map<String, Object>> parse(byte[] message);

    default ParserResult parseToResult(byte[] message) {
        ParserResult result = new ParserResult();
        try {
            result.setParsedMessages(parse(message));
        } catch (Throwable e) {
            result.setException(e);
        }
        return result;
    }
}
