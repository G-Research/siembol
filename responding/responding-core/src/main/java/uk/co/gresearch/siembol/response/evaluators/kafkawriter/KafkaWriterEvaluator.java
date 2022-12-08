package uk.co.gresearch.siembol.response.evaluators.kafkawriter;

import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import uk.co.gresearch.siembol.response.common.Evaluable;
import uk.co.gresearch.siembol.response.common.RespondingResult;
import uk.co.gresearch.siembol.response.common.ResponseAlert;
import uk.co.gresearch.siembol.response.common.ResponseEvaluationResult;
/**
 * An object for evaluating response alerts
 *
 * <p>This class implements Evaluable interface, and it is used in a response rule.
 * The kafka writer evaluator writes a message from the alert to a kafka topic.
 *
 * @author  Marian Novotny
 * @see Evaluable
 */
public class KafkaWriterEvaluator implements Evaluable {
    private final Producer<String, String> producer;
    private final String topicName;

    public KafkaWriterEvaluator(Producer<String, String> producer, String topicName) {
        this.producer = producer;
        this.topicName = topicName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RespondingResult evaluate(ResponseAlert alert) {
        try {
            producer.send(new ProducerRecord<>(topicName, alert.toString())).get();
        } catch (Exception e) {
            return RespondingResult.fromException(e);
        }

        return RespondingResult.fromEvaluationResult(ResponseEvaluationResult.MATCH, alert);
    }
}
