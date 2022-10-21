package uk.co.gresearch.siembol.alerts.common;

/**
 * An enum of matching results returned in AlertingResult after engine evaluation of an event.
 *
 * @author  Marian Novotny
 * @see #MATCH
 * @see #NO_MATCH
 *
 */
public enum EvaluationResult {
    /*
     * The event matched during the evaluation.
     */
    MATCH,
    /*
     * The event not matched during the evaluation.
     */
    NO_MATCH;

    /**
     * Returns negated evaluation result
     *
     * @param result evaluation result to be negated
     * @return       negated evaluation result
     */
    public static EvaluationResult negate(EvaluationResult result) {
        return result == MATCH ? NO_MATCH : MATCH;
    }
}
