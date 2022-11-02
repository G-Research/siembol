package uk.co.gresearch.siembol.alerts.storm;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;

import org.apache.storm.tuple.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.gresearch.siembol.alerts.common.AlertingEngineType;
import uk.co.gresearch.siembol.common.error.ErrorMessage;
import uk.co.gresearch.siembol.common.error.ErrorType;
import uk.co.gresearch.siembol.alerts.common.AlertingResult;
import uk.co.gresearch.siembol.alerts.protection.RuleProtectionSystem;
import uk.co.gresearch.siembol.alerts.protection.RuleProtectionSystemImpl;
import uk.co.gresearch.siembol.alerts.storm.model.*;
import uk.co.gresearch.siembol.common.metrics.SiembolMetrics;
import uk.co.gresearch.siembol.common.metrics.storm.StormMetricsRegistrarFactory;
import uk.co.gresearch.siembol.common.model.AlertingStormAttributesDto;
import uk.co.gresearch.siembol.common.storm.KafkaWriterAnchor;
import uk.co.gresearch.siembol.common.storm.KafkaWriterBoltBase;
import uk.co.gresearch.siembol.common.storm.KafkaWriterMessage;
import uk.co.gresearch.siembol.common.zookeeper.ZooKeeperConnector;

/**
 * An object for evaluating alerting protection matches and writing alerts to a kafka topic
 *
 * <p>This class extends a Storm BaseRichBolt class in order to implement a Storm bolt, that
 *  evaluates events using an engine initialised from the rules cached in the ZooKeeper,
 *  watches for the rules update in ZooKeeper and updates the rules without need to restart the topology or the bolt and
 *  emits alerts and exceptions after matching.
 *
 * @author Marian Novotny
 * @see KafkaWriterBoltBase
 * @see ZooKeeperConnector
 *
 */
public class AlertingKafkaWriterBolt extends KafkaWriterBoltBase {
    private static final long serialVersionUID = 1L;
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final String WRONG_ALERTS_FIELD_MESSAGE = "Wrong alerts type in tuple";
    private static final String WRONG_EXCEPTION_FIELD_MESSAGE = "Wrong exceptions type in tuple";
    private static final String RULE_PROTECTION_ERROR_MESSAGE =
            "The rule: %s reaches the limit\n hourly matches: %d, daily matches: %d, alert: %s";
    private static final String SEND_MSG_LOG = "Sending message {}\n to {} topic";
    private static final String MISSING_CORRELATION_KEY_MSG = "Missing key in correlation alert %s";

    private final String errorSensorType;
    private final String errorTopic;
    private final String outputTopic;
    private final String correlationTopic;
    private RuleProtectionSystem ruleProtection;

    public AlertingKafkaWriterBolt(AlertingStormAttributesDto attributes,  StormMetricsRegistrarFactory metricsFactory) {
        super(attributes.getKafkaProducerProperties().getProperties(), metricsFactory);
        this.outputTopic = attributes.getOutputTopic();
        this.errorTopic = attributes.getKafkaErrorTopic();
        this.correlationTopic = attributes.getCorrelationOutputTopic();
        AlertingEngineType engineType = AlertingEngineType.valueOfName(attributes.getAlertingEngine());
        errorSensorType = engineType.toString();
    }

    @Override
    public void execute(Tuple tuple) {
        Object matchesObject = tuple.getValueByField(TupleFieldNames.ALERTING_MATCHES.toString());
        if (!(matchesObject instanceof AlertMessages)) {
            LOG.error(WRONG_ALERTS_FIELD_MESSAGE);
            throw new IllegalStateException(WRONG_ALERTS_FIELD_MESSAGE);
        }
        AlertMessages matches = (AlertMessages)matchesObject;

        Object exceptionsObject = tuple.getValueByField(TupleFieldNames.ALERTING_EXCEPTIONS.toString());
        if (!(exceptionsObject instanceof ExceptionMessages)) {
            LOG.error(WRONG_EXCEPTION_FIELD_MESSAGE);
            throw new IllegalStateException(WRONG_EXCEPTION_FIELD_MESSAGE);
        }
        ExceptionMessages exceptions = (ExceptionMessages)exceptionsObject;

        var messages = new ArrayList<KafkaWriterMessage>();
        var counters = new ArrayList<String>();
        exceptions.forEach(x -> counters.add(SiembolMetrics.ALERTING_ENGINE_ERROR_MATCHES.getMetricName()));

        for (var match : matches) {
            AlertingResult matchesInfo = ruleProtection.incrementRuleMatches(match.getFullRuleName());
            int hourlyMatches = matchesInfo.getAttributes().getHourlyMatches();
            int dailyMatches = matchesInfo.getAttributes().getDailyMatches();
            int hourlyMatchesDiffWithMax = hourlyMatches - match.getMaxHourMatches().intValue();
            int dailyMatchesDiffWithMax = dailyMatches - match.getMaxDayMatches().intValue();

            if (hourlyMatchesDiffWithMax > 0 || dailyMatchesDiffWithMax > 0) {
                String msg = String.format(RULE_PROTECTION_ERROR_MESSAGE,
                        match.getFullRuleName(), hourlyMatches, dailyMatches, match.getAlertJson());
                LOG.debug(msg);

                if ((hourlyMatchesDiffWithMax > 0  && isPowerOfTwo(hourlyMatchesDiffWithMax))
                        || (dailyMatchesDiffWithMax > 0 && isPowerOfTwo(dailyMatchesDiffWithMax))) {
                    //NOTE: sending message about an alert filtered by rule protection is sampled exponentially
                    exceptions.add(msg);
                }

                counters.add(SiembolMetrics.ALERTING_ENGINE_RULE_PROTECTION.getMetricName());
                counters.add(SiembolMetrics.ALERTING_RULE_PROTECTION.getMetricName(match.getRuleName()));
                continue;
            }

            if (match.isVisibleAlert()) {
                LOG.debug(SEND_MSG_LOG, match.getAlertJson(), outputTopic);
                messages.add(new KafkaWriterMessage(outputTopic, match.getAlertJson()));
                counters.add(SiembolMetrics.ALERTING_ENGINE_MATCHES.getMetricName());
                counters.add(SiembolMetrics.ALERTING_RULE_MATCHES.getMetricName(match.getRuleName()));
            }

            if (match.isCorrelationAlert()) {
                if (match.getCorrelationKey().isEmpty()) {
                    String errorMsg = String.format(MISSING_CORRELATION_KEY_MSG, match.getAlertJson());
                    LOG.error(errorMsg);
                    throw new IllegalStateException(errorMsg);
                }

                LOG.debug(SEND_MSG_LOG, match.getAlertJson(), correlationTopic);
                messages.add(new KafkaWriterMessage(correlationTopic,
                        match.getCorrelationKey().get(),
                        match.getAlertJson()));
                counters.add(SiembolMetrics.ALERTING_ENGINE_CORRELATION_MATCHES.getMetricName());
                counters.add(SiembolMetrics.ALERTING_RULE_CORRELATION_MATCHES.getMetricName(match.getRuleName()));
            }
        }

        for (var exception : exceptions) {
            String errorMsgToSend = getErrorMessageToSend(exception);
            LOG.debug(SEND_MSG_LOG, errorMsgToSend, errorTopic);
            messages.add(new KafkaWriterMessage(errorTopic, errorMsgToSend));
        }

        var anchor = new KafkaWriterAnchor(tuple);
        super.writeMessages(messages, counters, anchor);
    }

    @Override
    public void prepareInternally() {
        ruleProtection = new RuleProtectionSystemImpl();
    }

    private String getErrorMessageToSend(String errorMsg) {
        ErrorMessage error = new ErrorMessage();
        error.setErrorType(ErrorType.ALERTING_ERROR);
        error.setFailedSensorType(errorSensorType);
        error.setMessage(errorMsg);
        return error.toString();
    }

    private boolean isPowerOfTwo(int n) {
        return (n & n - 1) == 0;
    }

}
