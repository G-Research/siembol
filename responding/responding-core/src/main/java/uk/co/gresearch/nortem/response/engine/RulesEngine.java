package uk.co.gresearch.nortem.response.engine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.gresearch.nortem.common.testing.InactiveTestingLogger;
import uk.co.gresearch.nortem.common.testing.TestingLogger;
import uk.co.gresearch.nortem.response.common.*;

import java.lang.invoke.MethodHandles;
import java.util.List;

public class RulesEngine implements ResponseEngine {
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final String MISSING_ATTRIBUTES = "Missing response rule engine attributes";
    private static final String NO_RULE_MATCHES_THE_ALERT = "No rule matches the alert %s";

    private final List<? extends Evaluable> rules;
    private final MetricCounter messagesCounter;
    private final MetricCounter filtersCounter;
    private final MetricCounter errorsCounter;
    private final TestingLogger logger;

    public RulesEngine(Builder builder) {
        this.rules = builder.rules;
        this.logger = builder.logger;
        this.messagesCounter = builder.messagesCounter;
        this.filtersCounter = builder.filtersCounter;
        this.errorsCounter = builder.errorsCounter;
    }

    @Override
    public RespondingResult evaluate(ResponseAlert alert) {
        messagesCounter.increment();

        for (Evaluable rule: rules) {
            RespondingResult currentResult = rule.evaluate(alert);
            if (currentResult.getStatusCode() != RespondingResult.StatusCode.OK) {
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

    public static class Builder {
        private List<? extends Evaluable> rules;
        private TestingLogger logger = new InactiveTestingLogger();
        private MetricFactory metricFactory;
        private MetricCounter messagesCounter;
        private MetricCounter filtersCounter;
        private MetricCounter errorsCounter;

        public Builder metricFactory(MetricFactory metricFactory) {
            this.metricFactory = metricFactory;
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
                    || metricFactory == null) {
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
