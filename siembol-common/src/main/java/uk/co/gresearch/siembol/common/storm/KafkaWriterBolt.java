package uk.co.gresearch.siembol.common.storm;

import org.apache.storm.tuple.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.gresearch.siembol.common.metrics.storm.StormMetricsRegistrarFactory;
import uk.co.gresearch.siembol.common.model.KafkaBatchWriterAttributesDto;

import java.lang.invoke.MethodHandles;

public class KafkaWriterBolt extends KafkaWriterBoltBase {
    private static final long serialVersionUID = 1L;
    private static final Logger LOG =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final String MISSING_MESSAGES_MSG = "Missing messages in tuple";
    private static final String MISSING_COUNTERS_MSG = "Missing counters in tuple";
    private final String fieldName;
    private final String countersFieldName;

    public KafkaWriterBolt(KafkaBatchWriterAttributesDto attributes,
                           String fieldName,
                           String countersFieldName,
                           StormMetricsRegistrarFactory metricsFactory) {
        super(attributes.getProducerProperties().getProperties(), metricsFactory);
        this.fieldName = fieldName;
        this.countersFieldName = countersFieldName;
    }

    @Override
    public void execute(Tuple tuple) {
        Object messagesObject = tuple.getValueByField(fieldName);
        if (!(messagesObject instanceof KafkaWriterMessages)) {
            LOG.error(MISSING_MESSAGES_MSG);
            throw new IllegalStateException(MISSING_MESSAGES_MSG);
        }
        KafkaWriterMessages currentMessages = (KafkaWriterMessages)messagesObject;
        if (currentMessages.isEmpty()) {
            LOG.error(MISSING_MESSAGES_MSG);
            throw new IllegalStateException(MISSING_MESSAGES_MSG);
        }

        Object countersObject = tuple.getValueByField(countersFieldName);
        if (!(countersObject instanceof SiembolMetricsCounters)) {
            LOG.error(MISSING_COUNTERS_MSG);
            throw new IllegalStateException(MISSING_COUNTERS_MSG);
        }
        var counters = (SiembolMetricsCounters)countersObject;

        var anchor = new KafkaWriterAnchor(tuple);
        writeMessages(currentMessages, counters, anchor);
    }
}
