package uk.co.gresearch.nortem.parsers.storm;
import java.lang.invoke.MethodHandles;
import java.util.*;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.errors.AuthorizationException;
import org.apache.kafka.common.errors.OutOfOrderSequenceException;
import org.apache.kafka.common.errors.ProducerFencedException;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.gresearch.nortem.parsers.application.parsing.ParsingApplicationResult;


import static org.apache.kafka.clients.CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG;
import static org.apache.kafka.clients.CommonClientConfigs.SECURITY_PROTOCOL_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.*;

public class ParsingApplicationWriterBolt extends BaseRichBolt {
    private static final Logger LOG =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final Properties props;
    private OutputCollector collector;
    private Producer<String, String> producer;

    public ParsingApplicationWriterBolt(StormParsingApplicationAttributes attributes) {
        this.props = new Properties();
        props.put(KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(BOOTSTRAP_SERVERS_CONFIG, attributes.getBootstrapServers());
        props.put(CLIENT_ID_CONFIG, attributes.getClientId());
        props.put(SECURITY_PROTOCOL_CONFIG, attributes.getSecurityProtocol());
        if (attributes.getWriterCompressionType() != null) {
            props.put(COMPRESSION_TYPE_CONFIG, attributes.getWriterCompressionType());
        }
    }

    @Override
    public void execute(Tuple tuple) {
        ArrayList<ParsingApplicationResult> results = (ArrayList<ParsingApplicationResult>)tuple.getValueByField(
                ParsingApplicationTuples.PARSING_RESULTS.toString());
        try {
            for (ParsingApplicationResult result : results) {
                for (String message : result.getMessages()) {
                    LOG.debug("Sending message {}\n to the topic {}", message, result.getTopic());
                    producer.send(new ProducerRecord<>(result.getTopic(),
                            String.valueOf(message.hashCode()),
                            message));
                }
            }
            producer.flush();
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
        producer = new KafkaProducer<>(props);
    }

    @Override
    public void cleanup() {
        producer.close();
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
    }
}
