package uk.co.gresearch.siembol.enrichments.evaluation;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.gresearch.siembol.alerts.common.AlertingEngine;
import uk.co.gresearch.siembol.alerts.engine.AlertingEngineImpl;
import uk.co.gresearch.siembol.enrichments.common.EnrichmentAttributes;
import uk.co.gresearch.siembol.enrichments.common.EnrichmentCommand;
import uk.co.gresearch.siembol.enrichments.common.EnrichmentResult;
import uk.co.gresearch.siembol.alerts.common.EvaluationResult;
import uk.co.gresearch.siembol.alerts.common.AlertingResult;
import uk.co.gresearch.siembol.alerts.engine.Rule;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
/**
 * An object that evaluates enrichment rules
 *
 * <p>This class implements EnrichmentEvaluator interface. It provides functionality for evaluating enrichment rules.
 * It is using the alerting engine with a custom implementation of an enriching rule in order to
 * implement the enrichment evaluation.
 *
 * @author  Marian Novotny
 * @see EnrichmentEvaluator
 * @see EnrichingRule
 * @see AlertingEngine
 */
public class AlertingEnrichmentEvaluator implements EnrichmentEvaluator {
    private static final String RULES_EXCEPTION_LOG = "Enrichment rule engine exception: {} on event: {}";
    private static final String MISSING_ENRICHMENTS_COMMAND = "Missing enrichment command in events %s";
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private AlertingEngine alertingEngine;

    private AlertingEnrichmentEvaluator(Builder builder) {
        this.alertingEngine = builder.alertingEngine;
    }

    private EnrichmentCommand createFromEvent(Map<String, Object> event) {
        Object ret = event.get(EnrichmentFields.ENRICHMENT_COMMAND.toString());
        if (!(ret instanceof EnrichmentCommand)) {
            String errorMsg = String.format(MISSING_ENRICHMENTS_COMMAND, event.toString());
            LOG.error(errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }

        return (EnrichmentCommand)ret;
    }

    /**
     * Evaluates enrichment rules on an event using alerting engine with enriching rules
     *
     * @param event a json string with an event after parsing
     * @return an enrichment result with a list of enrichment commands
     * @see EnrichmentResult
     * @see EnrichmentCommand
     */
    @Override
    public EnrichmentResult evaluate(String event) {
        EnrichmentAttributes attr = new EnrichmentAttributes();

        AlertingResult result = alertingEngine.evaluate(event);
        if (result.getStatusCode() == AlertingResult.StatusCode.ERROR) {
            LOG.error(RULES_EXCEPTION_LOG, result.getAttributes().getException(), event);
            attr.setMessage(result.getAttributes().getException());
            return new EnrichmentResult(EnrichmentResult.StatusCode.ERROR, attr);
        }

        if (result.getAttributes().getEvaluationResult() != EvaluationResult.MATCH
                || result.getAttributes().getOutputEvents() == null
                || result.getAttributes().getOutputEvents().isEmpty()) {
            return new EnrichmentResult(EnrichmentResult.StatusCode.OK, attr);
        }

        try {
            ArrayList<EnrichmentCommand> ret = result.getAttributes().getOutputEvents().stream()
                    .map(this::createFromEvent)
                    .collect(Collectors.toCollection(ArrayList::new));
            attr.setEnrichmentCommands(ret);
            return new EnrichmentResult(EnrichmentResult.StatusCode.OK, attr);
        } catch (Exception e) {
            LOG.error(RULES_EXCEPTION_LOG, ExceptionUtils.getStackTrace(e), event);
            attr.setMessage(ExceptionUtils.getMessage(e));
            return new EnrichmentResult(EnrichmentResult.StatusCode.ERROR, attr);
        }
    }

    /**
     * An object for building AlertingEnrichmentEvaluator instance. It is using the builder design pattern.
     *
     * @author  Marian Novotny
     *
     */
    public static class Builder {
        private static final String MISSING_RULES_ATTRIBUTES = "Missing enrichment rules";
        private AlertingEngine alertingEngine;
        private List<Pair<String, Rule>> rules;

        /**
         * Sets enriching rules that should be prepared in advance
         *
         * @param rules a list of pairs of the source type and the enriching rule
         * @return this builder
         */
        public Builder rules(List<Pair<String, Rule>> rules) {
            this.rules = rules;
            return this;
        }

        /**
         * Builds the alerting enrichment evaluator
         *
         * @return the alerting enrichment evaluator built from the builder state
         * @throws IllegalArgumentException in case of wrong arguments
         */
        public AlertingEnrichmentEvaluator build() {
            if (alertingEngine != null) {
                return new AlertingEnrichmentEvaluator(this);
            }

            if (rules == null) {
                throw new IllegalArgumentException(MISSING_RULES_ATTRIBUTES);
            }

            alertingEngine = new AlertingEngineImpl.Builder()
                    .constants(new ArrayList<>())
                    .protections(new ArrayList<>())
                    .rules(rules)
                    .build();
            return new AlertingEnrichmentEvaluator(this);
        }

        Builder AlertingEngine(AlertingEngine AlertingEngine) {
            this.alertingEngine = AlertingEngine;
            return this;
        }
    }
}
