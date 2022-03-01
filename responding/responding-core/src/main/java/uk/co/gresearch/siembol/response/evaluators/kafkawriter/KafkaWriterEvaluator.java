package uk.co.gresearch.siembol.response.evaluators.kafkawriter;

import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import uk.co.gresearch.siembol.response.common.Evaluable;
import uk.co.gresearch.siembol.response.common.RespondingResult;
import uk.co.gresearch.siembol.response.common.ResponseAlert;
import uk.co.gresearch.siembol.response.common.ResponseEvaluationResult;

public class KafkaWriterEvaluator implements Evaluable {
    private final Producer<String, String> producer;
    private final String topicName;

    public KafkaWriterEvaluator(Producer<String, String> producer, String topicName) {
        this.producer = producer;
        this.topicName = topicName;
    }

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
