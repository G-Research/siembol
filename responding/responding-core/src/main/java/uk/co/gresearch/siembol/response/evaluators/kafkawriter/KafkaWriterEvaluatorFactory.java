package uk.co.gresearch.siembol.response.evaluators.kafkawriter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.common.serialization.StringSerializer;
import uk.co.gresearch.siembol.common.jsonschema.SiembolJsonSchemaValidator;
import uk.co.gresearch.siembol.common.result.SiembolResult;
import uk.co.gresearch.siembol.response.common.ProvidedEvaluators;
import uk.co.gresearch.siembol.response.common.RespondingEvaluatorFactory;
import uk.co.gresearch.siembol.response.common.RespondingResult;
import uk.co.gresearch.siembol.response.model.KafkaWriterEvaluatorAttributesDto;
import uk.co.gresearch.siembol.response.model.KafkaWriterProperties;

import java.util.function.Function;

public class KafkaWriterEvaluatorFactory implements RespondingEvaluatorFactory {
    private static final ObjectReader JSON_ATTRIBUTES_READER = new ObjectMapper()
            .readerFor(KafkaWriterEvaluatorAttributesDto.class);
    private static final String MISSING_KAFKA_WRITER_PROPS_MSG = "Missing kafka writer evaluator properties";
    private final Producer<String, String> producer;
    private final SiembolJsonSchemaValidator attributesSchema;

    KafkaWriterEvaluatorFactory(KafkaWriterProperties kafkaWriterProperties,
                                Function<KafkaWriterProperties, Producer<String, String>> factory) throws Exception {
        attributesSchema = new SiembolJsonSchemaValidator(KafkaWriterEvaluatorAttributesDto.class);
        if (kafkaWriterProperties == null
                || kafkaWriterProperties.getProducer() == null) {
            throw new IllegalArgumentException(MISSING_KAFKA_WRITER_PROPS_MSG);
        }

        producer = factory.apply(kafkaWriterProperties);
    }

    public KafkaWriterEvaluatorFactory(KafkaWriterProperties kafkaWriterProperties) throws Exception {
        this(kafkaWriterProperties,
                x -> new KafkaProducer<>(x.getProducerProperties(), new StringSerializer(), new StringSerializer()));
    }

    @Override
    public RespondingResult createInstance(String attributes) {
        try {
            SiembolResult validationResult = attributesSchema.validate(attributes);
            if (validationResult.getStatusCode() != SiembolResult.StatusCode.OK) {
                return RespondingResult.fromSiembolResult(validationResult);
            }

            KafkaWriterEvaluatorAttributesDto attributesDto = JSON_ATTRIBUTES_READER.readValue(attributes);
            var evaluator = new KafkaWriterEvaluator(producer, attributesDto.getTopicName());
            return RespondingResult.fromEvaluator(evaluator);
        } catch (Exception e) {
            return RespondingResult.fromException(e);
        }
    }

    @Override
    public RespondingResult getType() {
        return RespondingResult.fromEvaluatorType(ProvidedEvaluators.KAFKA_WRITER_EVALUATOR.toString());
    }

    @Override
    public RespondingResult getAttributesJsonSchema() {
        return RespondingResult.fromAttributesSchema(
                attributesSchema.getJsonSchema().getAttributes().getJsonSchema());
    }
}
