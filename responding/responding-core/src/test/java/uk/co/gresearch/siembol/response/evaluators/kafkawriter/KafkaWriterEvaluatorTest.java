package uk.co.gresearch.siembol.response.evaluators.kafkawriter;

import org.apache.kafka.clients.producer.MockProducer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import uk.co.gresearch.siembol.response.common.RespondingResult;
import uk.co.gresearch.siembol.response.common.ResponseAlert;

import static uk.co.gresearch.siembol.response.common.ResponseEvaluationResult.MATCH;

public class KafkaWriterEvaluatorTest {
    private String topicName = "siembol.topic";
    private MockProducer<String, String> producer;
    private ResponseAlert alert = new ResponseAlert();
    private KafkaWriterEvaluator evaluator;

    @Before
    public void setUp() {
        producer = new MockProducer<>(true, new StringSerializer(), new StringSerializer());
        evaluator = new KafkaWriterEvaluator(producer, topicName);

        alert = new ResponseAlert();
        alert.put("is_alert", true);
        alert.put("product", "secret");
    }

    @Test
    public void writeOk() {
        var result = evaluator.evaluate(alert);
        Assert.assertEquals(RespondingResult.StatusCode.OK, result.getStatusCode());
        Assert.assertEquals(MATCH, result.getAttributes().getResult());
        Assert.assertNotNull(result.getAttributes());
        Assert.assertNotNull(result.getAttributes().getAlert());

        Assert.assertEquals(1, producer.history().size());
        Assert.assertEquals(topicName, producer.history().get(0).topic());
        Assert.assertTrue(producer.history().get(0).value().contains("{\"product\":\"secret\",\"is_alert\":true}"));
        Assert.assertNull(producer.history().get(0).key());
    }

    @Test
    public void writeException() {
        producer = new MockProducer<>();
        evaluator = new KafkaWriterEvaluator(producer, topicName);

        var result = evaluator.evaluate(alert);
        Assert.assertEquals(RespondingResult.StatusCode.ERROR, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes());
        Assert.assertNotNull(result.getAttributes().getMessage());

    }
}
