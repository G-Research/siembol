package uk.co.gresearch.siembol.alerts.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static uk.co.gresearch.siembol.alerts.common.AlertingResult.StatusCode.OK;

public class CompositeAlertingEngine implements AlertingEngine {
    private final List<AlertingEngine> alertingEngines;

    public CompositeAlertingEngine(List<AlertingEngine> alertingEngines) {
        this.alertingEngines = alertingEngines;
    }

    @Override
    public AlertingResult evaluate(Map<String, Object> event) {
        List<Map<String, Object>> outputEvents = new ArrayList<>();
        List<Map<String, Object>> exceptionsEvents = new ArrayList<>();

        for (AlertingEngine engine: alertingEngines) {
            AlertingResult result = engine.evaluate(event);
            if (result.getStatusCode() != OK) {
                return result;
            }
            outputEvents.addAll(result.getAttributes().getOutputEvents());
            exceptionsEvents.addAll(result.getAttributes().getExceptionEvents());
        }

        AlertingAttributes attributes = new AlertingAttributes();
        attributes.setEvaluationResult(outputEvents.isEmpty() ? EvaluationResult.NO_MATCH : EvaluationResult.MATCH);
        attributes.setOutputEvents(outputEvents);
        attributes.setExceptionEvents(exceptionsEvents);
        return new AlertingResult(OK, attributes);
    }

    @Override
    public AlertingEngineType getAlertingEngineType() {
        return alertingEngines.get(0).getAlertingEngineType();
    }
}
