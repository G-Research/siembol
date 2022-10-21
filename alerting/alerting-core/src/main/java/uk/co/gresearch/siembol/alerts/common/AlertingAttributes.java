package uk.co.gresearch.siembol.alerts.common;
import java.util.List;
import java.util.Map;
/**
 * An object that collects attributes that can be returned by alerting components
 *
 * <p>This bean object collects attributes and it is included in an Alerting result object.
 *
 * @author  Marian Novotny
 * @see AlertingResult
 *
 */
public class AlertingAttributes {
    private EvaluationResult evaluationResult;
    private String exception;
    private String message;
    private String rulesSchema;
    private Map<String, Object> event;
    private List<Map<String, Object>> outputEvents;
    private List<Map<String, Object>> exceptionEvents;
    private AlertingEngine engine;
    private Integer hourlyMatches;
    private Integer dailyMatches;

    public EvaluationResult getEvaluationResult() {
        return evaluationResult;
    }

    public void setEvaluationResult(EvaluationResult evaluationResult) {
        this.evaluationResult = evaluationResult;
    }

    public String getException() {
        return exception;
    }

    public void setException(String exception) {
        this.exception = exception;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Map<String, Object> getEvent() {
        return event;
    }

    public void setEvent(Map<String, Object> event) {
        this.event = event;
    }

    public String getRulesSchema() {
        return rulesSchema;
    }

    public void setRulesSchema(String rulesSchema) {
        this.rulesSchema = rulesSchema;
    }

    public AlertingEngine getEngine() {
        return engine;
    }

    public void setEngine(AlertingEngine engine) {
        this.engine = engine;
    }

    public List<Map<String, Object>> getOutputEvents() {
        return outputEvents;
    }

    public void setOutputEvents(List<Map<String, Object>> outputEvents) {
        this.outputEvents = outputEvents;
    }

    public List<Map<String, Object>> getExceptionEvents() {
        return exceptionEvents;
    }

    public void setExceptionEvents(List<Map<String, Object>> exceptionEvents) {
        this.exceptionEvents = exceptionEvents;
    }

    public Integer getHourlyMatches() {
        return hourlyMatches;
    }

    public void setHourlyMatches(Integer hourlyMatches) {
        this.hourlyMatches = hourlyMatches;
    }

    public Integer getDailyMatches() {
        return dailyMatches;
    }

    public void setDailyMatches(Integer dailyMatches) {
        this.dailyMatches = dailyMatches;
    }
}
