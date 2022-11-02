package uk.co.gresearch.siembol.alerts.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static uk.co.gresearch.siembol.alerts.common.AlertingResult.StatusCode.OK;
/**
 * An object that combines of multiple alerting engines of the same type
 *
 * <p>This object implements AlertingEngine interface by combining list of AlertingEngine objects.
 *
 * @author  Marian Novotny
 * @see AlertingEngine
 * @see uk.co.gresearch.siembol.alerts.correlationengine.CorrelationEngineImpl
 * @see CompositeAlertingEngine
 *
 */
public class CompositeAlertingEngine implements AlertingEngine {
    private final List<AlertingEngine> alertingEngines;

    /**
     * Creates the composite alerting engine by using the list of already created alerting engines.
     *
     * @param alertingEngines List of underlying alerting engines
     */
    public CompositeAlertingEngine(List<AlertingEngine> alertingEngines) {
        this.alertingEngines = alertingEngines;
    }

    /**
     * Evaluates an event by underlying alerting engines and
     * returns alerting result with a matching result and additional attributes.
     *
     * @param event serialized event as json string
     * @return      alerting result after evaluation
     * @see         AlertingResult
     */
    @Override
    public AlertingResult evaluate(Map<String, Object> event) {
        List<Map<String, Object>> outputEvents = new ArrayList<>();
        List<Map<String, Object>> exceptionsEvents = new ArrayList<>();

        for (AlertingEngine engine: alertingEngines) {
            AlertingResult result = engine.evaluate(event);
            if (result.getStatusCode() != OK) {
                return result;
            }
            if (result.getAttributes().getOutputEvents() != null) {
                outputEvents.addAll(result.getAttributes().getOutputEvents());
            }

            if (result.getAttributes().getExceptionEvents() != null) {
                exceptionsEvents.addAll(result.getAttributes().getExceptionEvents());
            }
        }

        AlertingAttributes attributes = new AlertingAttributes();
        attributes.setEvaluationResult(outputEvents.isEmpty() ? EvaluationResult.NO_MATCH : EvaluationResult.MATCH);
        attributes.setOutputEvents(outputEvents.isEmpty() ? null : outputEvents);
        attributes.setExceptionEvents(exceptionsEvents.isEmpty() ? null : exceptionsEvents);
        return new AlertingResult(OK, attributes);
    }

    /**
     * Returns an alerting engine type of underlying alerting engines
     *
     * @return      alerting engine type
     * @see         AlertingEngineType
     */
    @Override
    public AlertingEngineType getAlertingEngineType() {
        return alertingEngines.get(0).getAlertingEngineType();
    }
}
