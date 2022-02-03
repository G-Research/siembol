package uk.co.gresearch.siembol.common.storm;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.common.errors.AuthorizationException;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.Map;
import java.util.Properties;

public abstract class KafkaWriterBoltBase extends BaseRichBolt {
    private static final long serialVersionUID = 1L;
    private static final Logger LOG =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final String AUTH_EXCEPTION_MESSAGE =
            "Authorization exception {} during writing messages to the kafka";
    private static final String KAFKA_EXCEPTION_MESSAGE =
            "Exception {} during writing messages to the kafka";
    private static final String SENDING_MESSAGE_LOG =
            "Sending message: {} to the topic: {}";

    private final Properties props;
    private OutputCollector collector;
    private Producer<String, String> producer;

    protected KafkaWriterBoltBase(Properties producerProperties) {
        this.props = producerProperties;
    }

    @Override
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector) {
        this.collector = outputCollector;
        producer = new KafkaProducer<>(props, new StringSerializer(), new StringSerializer());
        prepareInternally();
    }

   protected void prepareInternally() {
   }

    @Override
    public void cleanup() {
        producer.close();
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
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

    protected void writeMessage(KafkaWriterMessage message, KafkaWriterAnchor anchor) {
        try {
            anchor.acquire();
            var callBack = createProducerCallback(anchor);
            LOG.debug(SENDING_MESSAGE_LOG, message.getMessage(), message.getTopic());
            producer.send(message.getProducerRecord(), callBack);
        } catch (AuthorizationException e) {
            LOG.error(AUTH_EXCEPTION_MESSAGE, ExceptionUtils.getStackTrace(e));
            producer.close();
            throw new IllegalStateException(e);
        } catch (Exception e) {
            LOG.error(KAFKA_EXCEPTION_MESSAGE, ExceptionUtils.getStackTrace(e));
            collector.fail(anchor.getTuple());
        }
    }
}
