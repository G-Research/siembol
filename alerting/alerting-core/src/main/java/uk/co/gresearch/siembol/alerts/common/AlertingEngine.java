package uk.co.gresearch.siembol.alerts.common;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

import java.util.Map;

public interface AlertingEngine {
     ObjectReader JSON_READER = new ObjectMapper()
            .readerFor(new TypeReference<Map<String, Object>>() { });

    default AlertingResult evaluate(String event) {
        try {
            Map<String, Object> eventMap = JSON_READER.readValue(event);
            return evaluate(eventMap);
        } catch (Exception e) {
            return AlertingResult.fromException(e);
        }
    }

    AlertingResult evaluate(Map<String, Object> event);

    AlertingEngineType getAlertingEngineType();

    default void clean() {}
}
