package uk.co.gresearch.siembol.response.evaluators.kafkawriter;

import org.apache.kafka.clients.producer.MockProducer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import uk.co.gresearch.siembol.response.common.ProvidedEvaluators;
import uk.co.gresearch.siembol.response.common.RespondingResult;
import uk.co.gresearch.siembol.response.model.KafkaWriterProperties;

import java.util.HashMap;

public class KafkaWriterEvaluatorFactoryTest {
    private final String attributes = """
            {
              "topic_name" : "siembol"
            }
            """;
    private KafkaWriterEvaluatorFactory factory;
    private final MockProducer<String, String> producer = new MockProducer<>(true,
            new StringSerializer(),
            new StringSerializer());

    @Before
    public void setUp() throws Exception {
        var kafkaWriterProperties = new KafkaWriterProperties();
        kafkaWriterProperties.setProducer(new HashMap<>());
        factory = new KafkaWriterEvaluatorFactory(kafkaWriterProperties, x -> producer);
    }

    @Test
    public void testGetType() {
        RespondingResult result = factory.getType();
        Assert.assertEquals(RespondingResult.StatusCode.OK, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes());
        Assert.assertEquals(ProvidedEvaluators.KAFKA_WRITER_EVALUATOR.toString(),
                result.getAttributes().getEvaluatorType());
    }

    @Test
    public void testGetAttributesJsonSchema() {
        RespondingResult result = factory.getAttributesJsonSchema();
        Assert.assertEquals(RespondingResult.StatusCode.OK, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes());
        Assert.assertNotNull(result.getAttributes().getAttributesSchema());
    }

    @Test
    public void testCreateInstance() {
        RespondingResult result = factory.createInstance(attributes);
        Assert.assertEquals(RespondingResult.StatusCode.OK, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes());
        Assert.assertNotNull(result.getAttributes().getRespondingEvaluator());
    }

    @Test
    public void testValidateAttributesOk() {
        RespondingResult result = factory.validateAttributes(attributes);
        Assert.assertEquals(RespondingResult.StatusCode.OK, result.getStatusCode());
    }

    @Test
    public void testValidateAttributesInvalidJson() {
        RespondingResult result = factory.validateAttributes("INVALID");
        Assert.assertEquals(RespondingResult.StatusCode.ERROR, result.getStatusCode());
    }

    @Test
    public void testValidateAttributesInvalid() {
        RespondingResult result = factory.validateAttributes(
                attributes.replace("\"topic_name\"", "\"unsupported\""));
        Assert.assertEquals(RespondingResult.StatusCode.ERROR, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes().getMessage());
    }

    @Test(expected = IllegalArgumentException.class)
    public void missingKafkaEvaluatorProperties() throws Exception {
        new KafkaWriterEvaluatorFactory(null, x -> producer);
    }
}
