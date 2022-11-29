package uk.co.gresearch.siembol.enrichments.evaluation;

import uk.co.gresearch.siembol.enrichments.common.EnrichmentResult;
/**
 * An object that evaluates enrichment rules
 *
 * <p>This interface provides functionality for evaluating enrichment rules.
 *
 * @author  Marian Novotny
 */
public interface EnrichmentEvaluator {
    /**
     * Evaluates enrichment rules on an event
     *
     * @param event a json string with an event after parsing
     * @return an enrichment result with a list of enrichment commands
     * @see EnrichmentResult
     */
    EnrichmentResult evaluate(String event);
}
