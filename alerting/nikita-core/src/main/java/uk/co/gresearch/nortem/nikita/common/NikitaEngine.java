package uk.co.gresearch.nortem.nikita.common;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

import java.util.Map;

public interface NikitaEngine {
     ObjectReader JSON_READER = new ObjectMapper()
            .readerFor(new TypeReference<Map<String, Object>>() { });

    default NikitaResult evaluate(String event) {
        try {
            Map<String, Object> eventMap = JSON_READER.readValue(event);
            return evaluate(eventMap);
        } catch (Exception e) {
            return NikitaResult.fromException(e);
        }
    }

    NikitaResult evaluate(Map<String, Object> event);

    default void clean() {}
}
