package uk.co.gresearch.siembol.alerts.common;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

import java.util.*;

/**
 * An object that evaluates events using its internal state including rules
 *
 * <p>This interface has two main implementations for evaluating events using
 * standard and correlation rules.
 *
 *
 * @author  Marian Novotny
 * @see uk.co.gresearch.siembol.alerts.engine.AlertingEngineImpl
 * @see uk.co.gresearch.siembol.alerts.correlationengine.CorrelationEngineImpl
 * @see CompositeAlertingEngine
 *
 */

public interface AlertingEngine {
     ObjectReader JSON_READER = new ObjectMapper()
            .readerFor(new TypeReference<Map<String, Object>>() { });

    /**
     * Evaluates event and returns alerting result with a matching result and additional attributes
     *
     * @param event serialized event as json string
     * @return      alerting result after evaluation
     * @see         AlertingResult
     */
    default AlertingResult evaluate(String event) {
        try {
            Map<String, Object> eventMap = JSON_READER.readValue(event);
            return evaluate(eventMap);
        } catch (Exception e) {
            return AlertingResult.fromException(e);
        }
    }

    /**
     * Evaluates event and returns alerting result with a matching result and additional attributes
     *
     * @param event deserialized event as map of string to object
     * @return      alerting result after evaluation
     * @see         AlertingResult
     */
    AlertingResult evaluate(Map<String, Object> event);

    /**
     * Returns an alerting engine type
     *
     * @return      alerting engine type
     * @see         AlertingEngineType
     */
    AlertingEngineType getAlertingEngineType();

    /**
     * Removes unused old internal state.
     */
    default void clean() {}
}
