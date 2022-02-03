package uk.co.gresearch.siembol.common.storm;

import org.apache.storm.tuple.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.gresearch.siembol.common.model.KafkaBatchWriterAttributesDto;

import java.lang.invoke.MethodHandles;

public class KafkaWriterBolt extends KafkaWriterBoltBase {
    private static final long serialVersionUID = 1L;
    private static final Logger LOG =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final String MISSING_MESSAGES_MSG = "Missing messages in tuple";
    private final String fieldName;

    public KafkaWriterBolt(KafkaBatchWriterAttributesDto attributes, String fieldName) {
        super(attributes.getProducerProperties().getProperties());
        this.fieldName = fieldName;
    }

    @Override
    public void execute(Tuple tuple) {
        Object messagesObject = tuple.getValueByField(fieldName);
        if (!(messagesObject instanceof KafkaWriterMessages)) {
            LOG.error(MISSING_MESSAGES_MSG);
            throw new IllegalStateException(MISSING_MESSAGES_MSG);
        }

        KafkaWriterMessages currentMessages = (KafkaWriterMessages)messagesObject;
        var anchor = new KafkaWriterAnchor(tuple);
        currentMessages.forEach(x -> writeMessage(x, anchor));
    }
}
