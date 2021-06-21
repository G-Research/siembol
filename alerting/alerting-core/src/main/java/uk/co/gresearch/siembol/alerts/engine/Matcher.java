package uk.co.gresearch.siembol.alerts.engine;

import uk.co.gresearch.siembol.alerts.common.EvaluationResult;

import java.util.Map;

public interface Matcher {
    EvaluationResult match(Map<String, Object> log);
    boolean canModifyEvent();
}
