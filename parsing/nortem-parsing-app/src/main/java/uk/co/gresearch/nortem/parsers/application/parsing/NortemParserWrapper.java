package uk.co.gresearch.nortem.parsers.application.parsing;

import uk.co.gresearch.nortem.parsers.common.NortemParser;
import uk.co.gresearch.nortem.parsers.common.ParserFields;
import uk.co.gresearch.nortem.parsers.common.ParserResult;
import uk.co.gresearch.nortem.parsers.common.SerializableNortemParser;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class NortemParserWrapper implements NortemParser, Serializable {
    private final RouterCondition condition;
    private final SerializableNortemParser parser;
    private final String topic;

    public NortemParserWrapper(RouterCondition condition, SerializableNortemParser parser, String topic) {
        this.condition = condition;
        this.parser = parser;
        this.topic = topic;
    }

    boolean checkCondition(String message) {
        return condition.apply(message);
    }

    @Override
    public List<Map<String, Object>> parse(byte[] message) {
        return parser.parse(message);
    }

    @Override
    public ParserResult parseToResult(String metadata, byte[] message) {
        ParserResult result = parser.parseToResult(metadata, message);
        result.setSourceType(getSourceType());
        result.setTopic(topic);
        return result;
    }

    @Override
    public String getSourceType() {
        return parser.getSourceType();
    }
}
