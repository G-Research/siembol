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
import uk.co.gresearch.siembol.common.metrics.SiembolCounter;
import uk.co.gresearch.siembol.common.metrics.SiembolMetricsRegistrar;
import uk.co.gresearch.siembol.common.metrics.storm.StormMetricsRegistrarFactory;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

public abstract class KafkaWriterBoltBase extends BaseRichBolt {
    private static final long serialVersionUID = 1L;
    private static final Logger LOG =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final String AUTH_EXCEPTION_MESSAGE =
            "Authorization exception {} during writing messages to the kafka";
    private static final String KAFKA_EXCEPTION_MESSAGE =
            "Exception {} during writing messages to the kafka";
    private static final String SENDING_MESSAGE_LOG =
            "Sending message: {}, key :{} to the topic: {} ";

    private final Properties props;
    private final StormMetricsRegistrarFactory metricsFactory;
    private OutputCollector collector;
    private Producer<String, String> producer;
    private SiembolMetricsRegistrar metricsRegistrar;

    protected KafkaWriterBoltBase(Properties producerProperties, StormMetricsRegistrarFactory metricsFactory) {
        this.props = producerProperties;
        this.metricsFactory = metricsFactory;
    }

    @Override
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector) {
        this.collector = outputCollector;
        producer = new KafkaProducer<>(props, new StringSerializer(), new StringSerializer());
        metricsRegistrar = metricsFactory.createSiembolMetricsRegistrar(topologyContext);
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

    protected void writeMessages(List<KafkaWriterMessage> messages, List<String> countersNames, KafkaWriterAnchor anchor) {
        List<SiembolCounter> siembolCounters = countersNames.stream()
                .map(x -> metricsRegistrar.registerCounter(x))
                .collect(Collectors.toList());
        anchor.addSiembolCounters(siembolCounters);

        if (messages.isEmpty()) {
            acknowledgeWithoutWriting(anchor);
        } else {
            anchor.acquire(messages.size());
            messages.forEach(x -> writeMessage(x, anchor));
        }
    }

    private Callback createProducerCallback(final KafkaWriterAnchor anchor) {
        return (x, e) -> {
            synchronized (collector) {
                if (e != null) {
                    LOG.error(KAFKA_EXCEPTION_MESSAGE, ExceptionUtils.getStackTrace(e));
                    collector.fail(anchor.getTuple());
                } else {
                    if (anchor.release()) {
                        anchor.incrementSiembolCounters();
                        collector.ack(anchor.getTuple());
                    }
                }
            }
        };
    }

    private void acknowledgeWithoutWriting(KafkaWriterAnchor anchor) {
        anchor.incrementSiembolCounters();
        synchronized (collector) {
            collector.ack(anchor.getTuple());
        }
    }

    private void writeMessage(KafkaWriterMessage message, KafkaWriterAnchor anchor) {
        try {
            var callBack = createProducerCallback(anchor);
            LOG.debug(SENDING_MESSAGE_LOG, message.getMessage(), message.getKey(), message.getTopic());
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
