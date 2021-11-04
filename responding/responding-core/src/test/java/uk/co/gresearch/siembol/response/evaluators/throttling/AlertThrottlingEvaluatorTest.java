package uk.co.gresearch.siembol.response.evaluators.throttling;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import uk.co.gresearch.siembol.response.common.RespondingResult;
import uk.co.gresearch.siembol.response.common.ResponseAlert;
import uk.co.gresearch.siembol.response.model.AlertThrottlingEvaluatorAttributesDto;

import java.io.IOException;
import static uk.co.gresearch.siembol.response.common.ResponseEvaluationResult.FILTERED;
import static uk.co.gresearch.siembol.response.common.ResponseEvaluationResult.MATCH;

public class AlertThrottlingEvaluatorTest {
    private static final ObjectReader JSON_ATTRIBUTES_READER = new ObjectMapper()
            .readerFor(AlertThrottlingEvaluatorAttributesDto.class);
    private final String attributes = """
            {
              "suppressing_key": "${host}_${port}",
              "time_unit_type": "seconds",
              "suppression_time": 1
            }
            """;

    private AlertThrottlingEvaluator evaluator;
    private ResponseAlert alert1 = new ResponseAlert();
    private ResponseAlert alert2 = new ResponseAlert();
    private AlertThrottlingEvaluatorAttributesDto attributesDto;

    @Before
    public void setUp() throws IOException {
        alert1 = new ResponseAlert();
        alert1.put("host", "secret");
        alert1.put("port", 1234);

        alert2 = (ResponseAlert) alert1.clone();
        alert2.put("host", "different");
        attributesDto = JSON_ATTRIBUTES_READER.readValue(attributes);
        evaluator = new AlertThrottlingEvaluator(attributesDto);
    }

    @Test
    public void testTwoDifferent() throws IOException, InterruptedException {
        RespondingResult result1 = evaluator.evaluate(alert1);
        RespondingResult result2 = evaluator.evaluate(alert2);

        Assert.assertEquals(RespondingResult.StatusCode.OK, result1.getStatusCode());
        Assert.assertEquals(MATCH, result1.getAttributes().getResult());
        Assert.assertNotNull(result1.getAttributes());
        Assert.assertNotNull(result1.getAttributes().getAlert());

        Assert.assertEquals(RespondingResult.StatusCode.OK, result2.getStatusCode());
        Assert.assertEquals(MATCH, result2.getAttributes().getResult());
        Assert.assertNotNull(result2.getAttributes());
        Assert.assertNotNull(result2.getAttributes().getAlert());

        for (int i = 0; i < 10; i++) {
            result1 = evaluator.evaluate(alert1);
            result2 = evaluator.evaluate(alert2);

            Assert.assertEquals(RespondingResult.StatusCode.OK, result1.getStatusCode());
            Assert.assertEquals(FILTERED, result1.getAttributes().getResult());
            Assert.assertNotNull(result1.getAttributes());
            Assert.assertNotNull(result1.getAttributes().getAlert());

            Assert.assertEquals(RespondingResult.StatusCode.OK, result2.getStatusCode());
            Assert.assertEquals(FILTERED, result2.getAttributes().getResult());
            Assert.assertNotNull(result2.getAttributes());
            Assert.assertNotNull(result2.getAttributes().getAlert());
        }

        Thread.sleep(2000);
        result1 = evaluator.evaluate(alert1);
        result2 = evaluator.evaluate(alert2);

        Assert.assertEquals(RespondingResult.StatusCode.OK, result1.getStatusCode());
        Assert.assertEquals(MATCH, result1.getAttributes().getResult());
        Assert.assertNotNull(result1.getAttributes());
        Assert.assertNotNull(result1.getAttributes().getAlert());

        Assert.assertEquals(RespondingResult.StatusCode.OK, result2.getStatusCode());
        Assert.assertEquals(MATCH, result2.getAttributes().getResult());
        Assert.assertNotNull(result2.getAttributes());
        Assert.assertNotNull(result2.getAttributes().getAlert());
    }

    @Test
    public void testNoKeySuppress() {
        alert1.remove("host");

        RespondingResult result1 = evaluator.evaluate(alert1);
        Assert.assertEquals(RespondingResult.StatusCode.OK, result1.getStatusCode());
        Assert.assertEquals(MATCH, result1.getAttributes().getResult());
        Assert.assertNotNull(result1.getAttributes());
        Assert.assertNotNull(result1.getAttributes().getAlert());
    }

    @Test
    public void testCaseInsensitive() {
        alert2.put("host", "SeCreT");

        RespondingResult result1 = evaluator.evaluate(alert1);
        RespondingResult result2 = evaluator.evaluate(alert2);

        Assert.assertEquals(RespondingResult.StatusCode.OK, result1.getStatusCode());
        Assert.assertEquals(MATCH, result1.getAttributes().getResult());
        Assert.assertNotNull(result1.getAttributes());
        Assert.assertNotNull(result1.getAttributes().getAlert());

        Assert.assertEquals(RespondingResult.StatusCode.OK, result2.getStatusCode());
        Assert.assertEquals(FILTERED, result2.getAttributes().getResult());
        Assert.assertNotNull(result2.getAttributes());
        Assert.assertNotNull(result2.getAttributes().getAlert());
    }
}
