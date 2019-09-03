package uk.co.gresearch.nortem.nikita.storm;
import java.lang.invoke.MethodHandles;
import java.util.*;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.errors.AuthorizationException;
import org.apache.kafka.common.errors.OutOfOrderSequenceException;
import org.apache.kafka.common.errors.ProducerFencedException;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.gresearch.nortem.common.error.ErrorMessage;
import uk.co.gresearch.nortem.common.error.ErrorType;
import uk.co.gresearch.nortem.nikita.common.NikitaResult;
import uk.co.gresearch.nortem.nikita.protection.RuleProtectionSystem;
import uk.co.gresearch.nortem.nikita.protection.RuleProtectionSystemImpl;


import static org.apache.kafka.clients.CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG;
import static org.apache.kafka.clients.CommonClientConfigs.SECURITY_PROTOCOL_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.*;

public class KafkaWriterBolt extends BaseRichBolt {
    private static final Logger LOG =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final String errorSensorType;
    private final Properties props;
    private final String errorTopic;
    private final String outputTopic;
    private final String correlationTopic;
    private OutputCollector collector;
    private Producer<String, String> producer;
    private RuleProtectionSystem ruleProtection;

    public KafkaWriterBolt(NikitaStormAttributes attributes) {
        this.props = new Properties();
        props.put(KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        props.put(VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        props.put(BOOTSTRAP_SERVERS_CONFIG, attributes.getBootstrapServers());
        props.put(ProducerConfig.CLIENT_ID_CONFIG, attributes.getClientId());
        props.put(SECURITY_PROTOCOL_CONFIG, attributes.getSecurityProtocol());

        this.outputTopic = attributes.getNikitaOutputTopic();
        this.errorTopic = attributes.getKafkaErrorTopic();
        this.correlationTopic = attributes.getNikitaCorrelationOutputTopic();
        NikitaEngineType engineType = NikitaEngineType.valueOfName(attributes.getNikitaEngine());
        errorSensorType = engineType.toString();
    }

    @Override
    public void execute(Tuple tuple) {
        ArrayList<NikitaAlert> matches = (ArrayList<NikitaAlert>)tuple.getValueByField(
                TupleFieldNames.NIKITA_MATCHES.toString());

        ArrayList<String> exceptions = (ArrayList<String>)tuple.getValueByField(
                TupleFieldNames.NIKITA_EXCEPTIONS.toString());

        try {
            if (matches != null) {
                for (NikitaAlert match : matches) {
                    NikitaResult matchesInfo = ruleProtection.incrementRuleMatches(match.getFullRuleName());
                    int hourlyMatches = matchesInfo.getAttributes().getHourlyMatches();
                    int dailyMatches = matchesInfo.getAttributes().getDailyMatches();

                    if (match.getMaxHourMatches().intValue() < hourlyMatches
                            || match.getMaxDayMatches().intValue() < dailyMatches) {
                        String msg = String.format(
                                "The rule: %s reaches the limit\n hourly matches: %d, daily matches: %d, alert: %s",
                                match.getFullRuleName(), hourlyMatches, dailyMatches, match.getAlertJson());
                        LOG.debug(msg);
                        exceptions = exceptions == null ? new ArrayList<>() : exceptions;
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
                producer.flush();
            }

            if (exceptions != null) {
                for (String errorMsg :  exceptions) {
                    String errorMsgToSend = getErrorMessageToSend(errorMsg);
                    LOG.debug("Sending message {}\n to error topic", errorMsgToSend);
                    producer.send(new ProducerRecord<>(errorTopic,
                            String.valueOf(errorMsgToSend.hashCode()),
                            errorMsgToSend));
                }
                producer.flush();
            }

            } catch (ProducerFencedException | OutOfOrderSequenceException | AuthorizationException e) {
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

    @Override
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector) {
        this.collector = outputCollector;
        ruleProtection = new RuleProtectionSystemImpl();
        producer = new KafkaProducer<>(props);
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
