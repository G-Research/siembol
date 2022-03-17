package uk.co.gresearch.siembol.response.engine;

import uk.co.gresearch.siembol.common.metrics.SiembolCounter;
import uk.co.gresearch.siembol.common.metrics.SiembolMetrics;
import uk.co.gresearch.siembol.common.metrics.SiembolMetricsRegistrar;
import uk.co.gresearch.siembol.common.testing.InactiveTestingLogger;
import uk.co.gresearch.siembol.common.testing.TestingLogger;
import uk.co.gresearch.siembol.response.common.*;

import java.util.List;

import static uk.co.gresearch.siembol.response.common.RespondingResult.StatusCode.OK;

public class RulesEngine implements ResponseEngine {
    private static final String MISSING_ATTRIBUTES = "Missing response rule engine attributes";
    private static final String NO_RULE_MATCHES_THE_ALERT = "No rule matches the alert %s";

    private final List<? extends Evaluable> rules;
    private final SiembolCounter messagesCounter;
    private final SiembolCounter filtersCounter;
    private final SiembolCounter errorsCounter;
    private final SiembolCounter noMatchesCounter;
    private final TestingLogger logger;
    private final RespondingResultAttributes metadataAttributes;

    public RulesEngine(Builder builder) {
        this.rules = builder.rules;
        this.logger = builder.logger;
        this.messagesCounter = builder.messagesCounter;
        this.filtersCounter = builder.filtersCounter;
        this.errorsCounter = builder.errorsCounter;
        this.noMatchesCounter = builder.noMatchesCounter;
        this.metadataAttributes = builder.metadataAttributes;
    }

    @Override
    public RespondingResult evaluate(ResponseAlert alert) {
        messagesCounter.increment();

        for (Evaluable rule: rules) {
            ResponseAlert current = (ResponseAlert)alert.clone();
            RespondingResult currentResult = rule.evaluate(current);
            if (currentResult.getStatusCode() != OK) {
                errorsCounter.increment();
                return currentResult;
            }

            if (currentResult.getAttributes().getResult() == ResponseEvaluationResult.FILTERED) {
                filtersCounter.increment();
                return currentResult;
            }

            if (currentResult.getAttributes().getResult() == ResponseEvaluationResult.MATCH) {
                return currentResult;
            }
        }

        noMatchesCounter.increment();
        RespondingResult result = RespondingResult.fromEvaluationResult(ResponseEvaluationResult.NO_MATCH, alert);
        String message = String.format(NO_RULE_MATCHES_THE_ALERT, alert.toString());
        logger.appendMessage(message);
        result.getAttributes().setMessage(message);
        return result;
    }

    @Override
    public RespondingResult getRulesMetadata() {
        return new RespondingResult(OK, metadataAttributes);
    }

    public static class Builder {
        private List<? extends Evaluable> rules;
        private TestingLogger logger = new InactiveTestingLogger();
        private SiembolMetricsRegistrar metricsRegistrar;
        private SiembolCounter messagesCounter;
        private SiembolCounter filtersCounter;
        private SiembolCounter errorsCounter;
        private SiembolCounter noMatchesCounter;
        private RespondingResultAttributes metadataAttributes;

        public Builder metricsRegistrar(SiembolMetricsRegistrar metricsRegistrar) {
            this.metricsRegistrar = metricsRegistrar;
            return this;
        }

        public Builder metadata(RespondingResultAttributes metadataAttributes) {
            this.metadataAttributes = metadataAttributes;
            return this;
        }

        public Builder rules(List<? extends Evaluable> rules) {
            this.rules = rules;
            return this;
        }

        public Builder testingLogger(TestingLogger logger) {
            this.logger = logger;
            return this;
        }

        public RulesEngine build() {
            if (rules == null || rules.isEmpty()
                    || metricsRegistrar == null
                    || metadataAttributes == null) {
                throw new IllegalArgumentException(MISSING_ATTRIBUTES);
            }

            messagesCounter = metricsRegistrar
                    .registerCounter(SiembolMetrics.RESPONSE_ENGINE_PROCESSED.getMetricName());
            filtersCounter = metricsRegistrar
                    .registerCounter(SiembolMetrics.RESPONSE_ENGINE_FILTERED.getMetricName());
            errorsCounter = metricsRegistrar
                    .registerCounter(SiembolMetrics.RESPONSE_ENGINE_ERRORS.getMetricName());
            noMatchesCounter = metricsRegistrar
                    .registerCounter(SiembolMetrics.RESPONSE_ENGINE_NO_MATCHES.getMetricName());

            return new RulesEngine(this);
        }
    }
}
