package uk.co.gresearch.siembol.alerts.engine;

import uk.co.gresearch.siembol.alerts.common.EvaluationResult;

import java.util.Map;

/**
 * An object for matching an event
 *
 * <p>This interface for matching an event and providing metadata for a caller such us
 * the matcher is negated, or whether it can modify the event.
 *
 *
 * @author  Marian Novotny
 * @see uk.co.gresearch.siembol.alerts.engine.AlertingEngineImpl
 * @see Rule
 * @see BasicMatcher
 * @see CompositeMatcher
 */
public interface Matcher {
    /**
     * Matches the event and returns evaluation result.
     *
     * @param event map of string to object
     * @return      the evaluation result after evaluation
     * @see         EvaluationResult
     */
    EvaluationResult match(Map<String, Object> event);
    /**
     * Provides information whether the matcher can modify the event and
     * the caller needs to consider it before matching.
     *
     * @return true if the matcher can modify the event, otherwise false
     */
    boolean canModifyEvent();
    /**
     * Provides information whether the matcher is negated
     *
     * @return true if the matcher is negated, otherwise false
     */
    boolean isNegated();
}
