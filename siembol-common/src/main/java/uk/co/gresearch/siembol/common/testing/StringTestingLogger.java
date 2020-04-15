package uk.co.gresearch.siembol.common.testing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.util.Map;

public class StringTestingLogger implements TestingLogger {
    private static final ObjectWriter JSON_WRITER = new ObjectMapper()
            .writerFor(new TypeReference<Map<String, Object>>() { })
            .with(SerializationFeature.INDENT_OUTPUT);
    private static final String UNKNOWN_MAP = "UNKNOWN";

    private final StringBuilder stringBuilder = new StringBuilder();

    @Override
    public void appendMessage(String msg) {
        if (stringBuilder.length() > 0) {
            stringBuilder.append("\n");
        }
        stringBuilder.append(msg);
    }

    @Override
    public String getLog() {
        return stringBuilder.toString();
    }

    @Override
    public boolean isActive() {
        return true;
    }

    @Override
    public void appendMap(Map<String, Object> map) {
        appendMessage(writeMapToString(map));
    }

    private String writeMapToString(Map<String, Object> map) {
        try {
            return JSON_WRITER.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            return UNKNOWN_MAP;
        }
    }
}
