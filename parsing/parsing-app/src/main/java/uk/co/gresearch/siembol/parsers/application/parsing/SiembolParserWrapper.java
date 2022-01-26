package uk.co.gresearch.siembol.parsers.application.parsing;

import uk.co.gresearch.siembol.parsers.common.SiembolParser;
import uk.co.gresearch.siembol.parsers.common.ParserResult;
import uk.co.gresearch.siembol.parsers.common.SerializableSiembolParser;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class SiembolParserWrapper implements SiembolParser, Serializable {
    private static final long serialVersionUID = 1L;
    private final RouterCondition condition;
    private final SerializableSiembolParser parser;
    private final String topic;

    public SiembolParserWrapper(RouterCondition condition, SerializableSiembolParser parser, String topic) {
        this.condition = condition;
        this.parser = parser;
        this.topic = topic;
    }

    public SiembolParserWrapper(SerializableSiembolParser parser, String topic) {
        this(x -> true, parser, topic);
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
