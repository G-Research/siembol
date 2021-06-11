package uk.co.gresearch.siembol.alerts.storm;
import java.lang.invoke.MethodHandles;
import java.util.*;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.errors.AuthorizationException;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
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
import uk.co.gresearch.siembol.common.model.AlertingStormAttributesDto;

public class KafkaWriterBolt extends BaseRichBolt {
    private static final long serialVersionUID = 1L;
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final String WRONG_ALERTS_FIELD_MESSAGE = "Wrong alerts type in tuple";
    private static final String WRONG_EXCEPTION_FIELD_MESSAGE = "Wrong exceptions type in tuple";

    private final String errorSensorType;
    private final Properties props;
    private final String errorTopic;
    private final String outputTopic;
    private final String correlationTopic;
    private OutputCollector collector;
    private Producer<String, String> producer;
    private RuleProtectionSystem ruleProtection;

    public KafkaWriterBolt(AlertingStormAttributesDto attributes) {
        this.props = new Properties();
        attributes.getKafkaProducerProperties().getRawMap().entrySet().forEach(x -> props.put(x.getKey(), x.getValue()));
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

        try {
            for (AlertMessage match : matches) {
                AlertingResult matchesInfo = ruleProtection.incrementRuleMatches(match.getFullRuleName());
                int hourlyMatches = matchesInfo.getAttributes().getHourlyMatches();
                int dailyMatches = matchesInfo.getAttributes().getDailyMatches();

                if (match.getMaxHourMatches().intValue() < hourlyMatches
                        || match.getMaxDayMatches().intValue() < dailyMatches) {
                    String msg = String.format(
                            "The rule: %s reaches the limit\n hourly matches: %d, daily matches: %d, alert: %s",
                            match.getFullRuleName(), hourlyMatches, dailyMatches, match.getAlertJson());
                    LOG.debug(msg);
                    exceptions.add(msg);
                    continue;
                }

                if (match.isVisibleAlert()) {
                    LOG.debug("Sending message {}\n to output topic", match.getAlertJson());
                    producer.send(new ProducerRecord<>(outputTopic,
                            String.valueOf(match.getAlertJson().hashCode()),
                            match.getAlertJson()));
                }

                if (match.isCorrelationAlert()) {
                    LOG.debug("Sending message {}\n to correlation alerts topic", match.getAlertJson());
                    producer.send(new ProducerRecord<>(correlationTopic,
                            match.getCorrelationKey().get(),
                            match.getAlertJson()));
                }
            }

            for (String errorMsg : exceptions) {
                String errorMsgToSend = getErrorMessageToSend(errorMsg);
                LOG.debug("Sending message {}\n to error topic", errorMsgToSend);
                producer.send(new ProducerRecord<>(errorTopic,
                        String.valueOf(errorMsgToSend.hashCode()),
                        errorMsgToSend));
            }

            producer.flush();
        } catch (AuthorizationException e) {
                LOG.error("Exception {} during writing messages to the kafka",
                        ExceptionUtils.getStackTrace(e));
                producer.close();
                throw new IllegalStateException(e);
            } catch (KafkaException e) {
            LOG.error("KafkaException {} during writing messages to the kafka",
                    ExceptionUtils.getStackTrace(e));
                collector.fail(tuple);
                return;
            }

        LOG.debug("Acking tuple");
        collector.ack(tuple);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector) {
        this.collector = outputCollector;
        ruleProtection = new RuleProtectionSystemImpl();
        producer = new KafkaProducer<>(props, new StringSerializer(), new StringSerializer());
    }

    @Override
    public void cleanup() {
        producer.close();
    }

    private String getErrorMessageToSend(String errorMsg) {
        ErrorMessage error = new ErrorMessage();
        error.setErrorType(ErrorType.ALERTING_ERROR);
        error.setFailedSensorType(errorSensorType);
        error.setMessage(errorMsg);
        return error.toString();
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
    }
}
