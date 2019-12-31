package uk.co.gresearch.nortem.enrichments.evaluation;

import uk.co.gresearch.nortem.enrichments.common.EnrichmentResult;

public interface EnrichmentEvaluator {
    EnrichmentResult evaluateRules(String event);
}
