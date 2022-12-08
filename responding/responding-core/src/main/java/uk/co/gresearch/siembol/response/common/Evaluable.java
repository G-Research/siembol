package uk.co.gresearch.siembol.response.common;
/**
 * An object for evaluating response alerts
 *
 * <p>This interface is for evaluating response alerts.
 *
 * @author  Marian Novotny
 */
public interface Evaluable {
    /**
     * Evaluates a response alert
     *
     * @param alert an alert for evaluation
     * @return RespondingResult with OK status and the result alert in attributes or
     *                          ERROR status code with an error message otherwise.
     */
    RespondingResult evaluate(ResponseAlert alert);
}
