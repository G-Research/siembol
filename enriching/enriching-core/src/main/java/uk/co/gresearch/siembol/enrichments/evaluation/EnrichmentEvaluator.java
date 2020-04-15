package uk.co.gresearch.siembol.enrichments.evaluation;

import uk.co.gresearch.siembol.enrichments.common.EnrichmentResult;

public interface EnrichmentEvaluator {
    EnrichmentResult evaluate(String event);
}
