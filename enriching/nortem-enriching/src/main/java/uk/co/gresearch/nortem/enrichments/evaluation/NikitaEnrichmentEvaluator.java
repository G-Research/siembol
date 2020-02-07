package uk.co.gresearch.nortem.enrichments.evaluation;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.gresearch.nortem.enrichments.common.EnrichmentAttributes;
import uk.co.gresearch.nortem.enrichments.common.EnrichmentCommand;
import uk.co.gresearch.nortem.enrichments.common.EnrichmentResult;
import uk.co.gresearch.nortem.nikita.common.EvaluationResult;
import uk.co.gresearch.nortem.nikita.common.NikitaEngine;
import uk.co.gresearch.nortem.nikita.common.NikitaResult;
import uk.co.gresearch.nortem.nikita.engine.NikitaEngineImpl;
import uk.co.gresearch.nortem.nikita.engine.Rule;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class NikitaEnrichmentEvaluator implements EnrichmentEvaluator {
    private static final String RULES_EXCEPTION_LOG = "Enrichment rule engine exception: {} on event: {}";
    private static final String MISSING_ENRICHMENTS_COMMAND = "Missing enrichment command in events %s";
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private NikitaEngine nikitaEngine;

    private NikitaEnrichmentEvaluator(Builder builder) {
        this.nikitaEngine = builder.nikitaEngine;
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

    @Override
    public EnrichmentResult evaluate(String event) {
        EnrichmentAttributes attr = new EnrichmentAttributes();

        NikitaResult result = nikitaEngine.evaluate(event);
        if (result.getStatusCode() == NikitaResult.StatusCode.ERROR) {
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

    public static class Builder {
        private static final String MISSING_RULES_ATTRIBUTES = "Missing enrichment rules";
        private NikitaEngine nikitaEngine;
        private List<Pair<String, Rule>> rules;

        public Builder rules(List<Pair<String, Rule>> rules) {
            this.rules = rules;
            return this;
        }

        public NikitaEnrichmentEvaluator build() {
            if (nikitaEngine != null) {
                return new NikitaEnrichmentEvaluator(this);
            }

            if (rules == null) {
                throw new IllegalArgumentException(MISSING_RULES_ATTRIBUTES);
            }

            nikitaEngine = new NikitaEngineImpl.Builder()
                    .constants(new ArrayList<>())
                    .protections(new ArrayList<>())
                    .rules(rules)
                    .build();
            return new NikitaEnrichmentEvaluator(this);
        }

        Builder nikitaEngine(NikitaEngine nikitaEngine) {
            this.nikitaEngine = nikitaEngine;
            return this;
        }
    }
}
