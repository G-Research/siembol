package uk.co.gresearch.siembol.response.engine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.gresearch.siembol.common.metrics.SiembolCounter;
import uk.co.gresearch.siembol.common.metrics.SiembolMetrics;
import uk.co.gresearch.siembol.common.metrics.SiembolMetricsRegistrar;
import uk.co.gresearch.siembol.common.testing.InactiveTestingLogger;
import uk.co.gresearch.siembol.common.testing.TestingLogger;
import uk.co.gresearch.siembol.response.common.*;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;

public class ResponseRule implements Evaluable {
    private static final Logger LOG = LoggerFactory
            .getLogger(MethodHandles.lookup().lookupClass());
    private static final String FULL_RULE_NAME_FORMAT_MSG = "%s_v%d";
    private static final String MISSING_ATTRIBUTES = "Missing response rule attributes";

    private final String ruleName;
    private final String fullRuleName;
    private final List<Evaluable> evaluators;
    private final SiembolCounter matchesCounter;
    private final SiembolCounter filtersCounter;
    private final SiembolCounter errorsCounter;
    private final TestingLogger logger;

    private ResponseRule(Builder builder) {
        this.ruleName = builder.ruleName;
        this.fullRuleName = builder.fullRuleName;
        this.evaluators = builder.evaluators;
        this.matchesCounter = builder.matchesCounter;
        this.filtersCounter = builder.filtersCounter;
        this.errorsCounter = builder.errorsCounter;
        this.logger = builder.logger;
    }

    @Override
    public RespondingResult evaluate(ResponseAlert alert) {
        ResponseAlert currentAlert = (ResponseAlert)alert.clone();
        LOG.debug("Trying to evaluate rule {} with alert {}", fullRuleName, alert.toString());
        currentAlert.put(ResponseFields.RULE_NAME.toString(), ruleName);
        currentAlert.put(ResponseFields.FULL_RULE_NAME.toString(), fullRuleName);

        for (Evaluable evaluator: evaluators) {
            try {
                RespondingResult result = evaluator.evaluate(currentAlert);
                if (result.getStatusCode() != RespondingResult.StatusCode.OK) {
                    LOG.error("Error match of the rule {} with message {}",
                            fullRuleName,
                            result.getAttributes().getMessage());
                    errorsCounter.increment();
                    result.getAttributes().setRuleName(fullRuleName);
                    return result;
                }
                switch (result.getAttributes().getResult()) {
                    case FILTERED:
                        filtersCounter.increment();
                        return RespondingResult.fromEvaluationResult(ResponseEvaluationResult.FILTERED, currentAlert);
                    case NO_MATCH:
                        return RespondingResult.fromEvaluationResult(ResponseEvaluationResult.NO_MATCH, alert);
                    case MATCH:
                        //NOTE: try the next evaluator
                }
                currentAlert = result.getAttributes().getAlert();
            } catch (Exception e) {
                LOG.error("Exception {} during evaluating the rule {}", e, fullRuleName);
                errorsCounter.increment();
                RespondingResult ret = RespondingResult.fromException(e);
                ret.getAttributes().setRuleName(fullRuleName);
                return ret;
            }
        }

        matchesCounter.increment();
        String msg = String.format("the rule: %s matched", fullRuleName);
        LOG.info(msg);
        logger.appendMessage(msg);
        return RespondingResult.fromEvaluationResult(ResponseEvaluationResult.MATCH, currentAlert);
    }

    public static class Builder {
        private String ruleName;
        private String fullRuleName;
        private Integer ruleVersion;
        private SiembolMetricsRegistrar metricsRegistrar;
        private SiembolCounter matchesCounter;
        private SiembolCounter filtersCounter;
        private SiembolCounter errorsCounter;
        private List<Evaluable> evaluators = new ArrayList<>();
        private TestingLogger logger = new InactiveTestingLogger();

        public Builder metricsRegistrar(SiembolMetricsRegistrar metricsRegistrar) {
            this.metricsRegistrar = metricsRegistrar;
            return this;
        }

        public Builder ruleName(String ruleName) {
            this.ruleName = ruleName;
            return this;
        }

        public Builder ruleVersion(Integer ruleVersion) {
            this.ruleVersion = ruleVersion;
            return this;
        }

        public Builder addEvaluator(Evaluable evaluator) {
            evaluators.add(evaluator);
            return this;
        }

        public Builder logger(TestingLogger logger) {
            this.logger = logger;
            return this;
        }

        public ResponseRule build() {
            if (ruleName == null
                    || ruleVersion == null
                    || metricsRegistrar == null) {
                throw new IllegalArgumentException(MISSING_ATTRIBUTES);
            }

            fullRuleName = String.format(FULL_RULE_NAME_FORMAT_MSG, ruleName, ruleVersion);
            this.matchesCounter = metricsRegistrar
                    .registerCounter(SiembolMetrics.RESPONSE_RULE_MATCHES.getMetricName(ruleName));

            this.filtersCounter = metricsRegistrar
                    .registerCounter(SiembolMetrics.RESPONSE_RULE_FILTERED_ALERTS.getMetricName(ruleName));

            this.errorsCounter = metricsRegistrar
                    .registerCounter(SiembolMetrics.RESPONSE_RULE_ERROR_MATCHES.getMetricName(ruleName));

            return new ResponseRule(this);
        }
    }
}
