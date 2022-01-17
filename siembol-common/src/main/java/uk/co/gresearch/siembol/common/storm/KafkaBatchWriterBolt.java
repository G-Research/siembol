package uk.co.gresearch.siembol.common.storm;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.errors.AuthorizationException;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.gresearch.siembol.common.model.KafkaBatchWriterAttributesDto;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Map;
import java.util.Properties;

import static org.apache.storm.utils.TupleUtils.isTick;
import static org.apache.storm.utils.TupleUtils.putTickFrequencyIntoComponentConfig;

public class KafkaBatchWriterBolt extends BaseRichBolt {
    private static final long serialVersionUID = 1L;
    private static final Logger LOG =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final int ACK_INTERVAL_ACK_IN_SEC = 1;
    private static final String AUTH_EXCEPTION_MESSAGE =
            "Authorization exception {} during writing messages to the kafka";
    private static final String KAFKA_EXCEPTION_MESSAGE =
            "Exception {} during writing messages to the kafka";
    private static final String SENDING_MESSAGE_LOG =
            "Sending message: {} to the topic: {}";
    private static final String MISSING_MESSAGES_MSG =
            "Missing messages in tuple";

    private final Properties props;
    private final int batchSize;
    private final String fieldName;
    private final ArrayList<Pair<KafkaBatchWriterMessage, KafkaWriterAnchor>> messages = new ArrayList<>();
    private OutputCollector collector;
    private Producer<String, String> producer;

    public KafkaBatchWriterBolt(KafkaBatchWriterAttributesDto attributes, String fieldName) {
        this.props = new Properties();
        props.putAll(attributes.getProducerProperties().getRawMap());
        this.batchSize = attributes.getBatchSize();
        this.messages.ensureCapacity(this.batchSize);
        this.fieldName = fieldName;
    }

    @Override
    public void execute(Tuple tuple) {
        if (isTick(tuple)) {
            if (!messages.isEmpty()) {
                writeTuples();
            }
            return;
        }

        Object messagesObject = tuple.getValueByField(fieldName);
        if (!(messagesObject instanceof KafkaBatchWriterMessages)) {
            LOG.error(MISSING_MESSAGES_MSG);
            throw new IllegalStateException(MISSING_MESSAGES_MSG);
        }

        KafkaBatchWriterMessages currentMessages = (KafkaBatchWriterMessages)messagesObject;
        var anchor = new KafkaWriterAnchor(tuple);
        currentMessages.forEach(x -> {
            anchor.acquire();
            messages.add(Pair.of(x, anchor));
        });

        if (messages.size() >= batchSize) {
            writeTuples();
        }
    }

    @Override
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector) {
        this.collector = outputCollector;
        producer = new KafkaProducer<>(props, new StringSerializer(), new StringSerializer());
    }

    @Override
    public void cleanup() {
        producer.close();
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
    }

    @Override
    public Map<String, Object> getComponentConfiguration() {
        return putTickFrequencyIntoComponentConfig(null, ACK_INTERVAL_ACK_IN_SEC);
    }

    private Callback createProducerCallback(final KafkaWriterAnchor anchor) {
        return (x, e) -> {
            synchronized (collector) {
                if (e != null) {
                    LOG.error(KAFKA_EXCEPTION_MESSAGE, ExceptionUtils.getStackTrace(e));
                    collector.fail(anchor.getTuple());
                } else {
                    if (anchor.release()) {
                        collector.ack(anchor.getTuple());
                    }
                }
            }
        };
    }

    private void writeTuples() {
        try {
            messages.forEach(x -> {
                var message = x.getLeft();
                var callBack = createProducerCallback(x.getRight());

                LOG.debug(SENDING_MESSAGE_LOG, message.getMessage(), message.getTopic());
                producer.send(new ProducerRecord<>(message.getTopic(), message.getMessage()), callBack);
            });

        } catch (AuthorizationException e) {
            LOG.error(AUTH_EXCEPTION_MESSAGE, ExceptionUtils.getStackTrace(e));
            producer.close();
            throw new IllegalStateException(e);
        } catch (Exception e) {
            LOG.error(KAFKA_EXCEPTION_MESSAGE, ExceptionUtils.getStackTrace(e));
            messages.forEach(x -> collector.fail(x.getRight().getTuple()));
        } finally {
            messages.clear();
        }
    }
}
