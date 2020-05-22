package uk.co.gresearch.siembol.response.engine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.gresearch.siembol.common.testing.InactiveTestingLogger;
import uk.co.gresearch.siembol.common.testing.TestingLogger;
import uk.co.gresearch.siembol.response.common.*;

import java.lang.invoke.MethodHandles;
import java.util.List;

import static uk.co.gresearch.siembol.response.common.RespondingResult.StatusCode.OK;

public class RulesEngine implements ResponseEngine {
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final String MISSING_ATTRIBUTES = "Missing response rule engine attributes";
    private static final String NO_RULE_MATCHES_THE_ALERT = "No rule matches the alert %s";

    private final List<? extends Evaluable> rules;
    private final MetricCounter messagesCounter;
    private final MetricCounter filtersCounter;
    private final MetricCounter errorsCounter;
    private final TestingLogger logger;
    private final RespondingResultAttributes metadataAttributes;

    public RulesEngine(Builder builder) {
        this.rules = builder.rules;
        this.logger = builder.logger;
        this.messagesCounter = builder.messagesCounter;
        this.filtersCounter = builder.filtersCounter;
        this.errorsCounter = builder.errorsCounter;
        this.metadataAttributes = builder.metadataAttributes;

    }

    @Override
    public RespondingResult evaluate(ResponseAlert alert) {
        messagesCounter.increment();

        for (Evaluable rule: rules) {
            RespondingResult currentResult = rule.evaluate(alert);
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

        errorsCounter.increment();
        String message = String.format(NO_RULE_MATCHES_THE_ALERT, alert.toString());
        logger.appendMessage(message);
        LOG.error(message);
        RespondingResultAttributes attributes = new RespondingResultAttributes();
        attributes.setMessage(message);
        return new RespondingResult(RespondingResult.StatusCode.ERROR, attributes);
    }

    @Override
    public RespondingResult getRulesMetadata() {
        return new RespondingResult(OK, metadataAttributes);
    }

    public static class Builder {
        private List<? extends Evaluable> rules;
        private TestingLogger logger = new InactiveTestingLogger();
        private MetricFactory metricFactory;
        private MetricCounter messagesCounter;
        private MetricCounter filtersCounter;
        private MetricCounter errorsCounter;
        private RespondingResultAttributes metadataAttributes;

        public Builder metricFactory(MetricFactory metricFactory) {
            this.metricFactory = metricFactory;
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
                    || metricFactory == null
                    || metadataAttributes == null) {
                throw new IllegalArgumentException(MISSING_ATTRIBUTES);
            }

            messagesCounter = metricFactory.createCounter(MetricNames.ENGINE_PROCESSED_MESSAGES.getName(),
                    MetricNames.ENGINE_PROCESSED_MESSAGES.getDescription());
            filtersCounter = metricFactory.createCounter(MetricNames.ENGINE_FILTERED_MESSAGES.getName(),
                    MetricNames.ENGINE_FILTERED_MESSAGES.getDescription());
            errorsCounter = metricFactory.createCounter(MetricNames.ENGINE_ERROR_MESSAGES.getName(),
                    MetricNames.ENGINE_ERROR_MESSAGES.getDescription());

            return new RulesEngine(this);
        }
    }
}
