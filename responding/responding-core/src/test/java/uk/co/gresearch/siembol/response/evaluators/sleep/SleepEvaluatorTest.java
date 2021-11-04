package uk.co.gresearch.siembol.response.evaluators.sleep;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import uk.co.gresearch.siembol.response.common.RespondingResult;
import uk.co.gresearch.siembol.response.common.ResponseAlert;
import uk.co.gresearch.siembol.response.model.SleepEvaluatorAttributesDto;

import java.io.IOException;
import static uk.co.gresearch.siembol.response.common.ResponseEvaluationResult.MATCH;

public class SleepEvaluatorTest {
    private static final ObjectReader JSON_ATTRIBUTES_READER = new ObjectMapper()
            .readerFor(SleepEvaluatorAttributesDto.class);
    private final String attributes = """
            {
              "time_unit_type": "milli_seconds",
              "sleeping_time": 500
            }
            """;

    private SleepEvaluator evaluator;
    private ResponseAlert alert = new ResponseAlert();
    private SleepEvaluatorAttributesDto attributesDto;

    @Before
    public void setUp() throws IOException {
        alert = new ResponseAlert();
        alert.put("is_alert", true);
        attributesDto = JSON_ATTRIBUTES_READER.readValue(attributes);
        evaluator = new SleepEvaluator(attributesDto);
    }

    @Test
    public void testSleepOk() {
        long startTime = System.currentTimeMillis();

        RespondingResult result = evaluator.evaluate(alert);
        Assert.assertEquals(RespondingResult.StatusCode.OK, result.getStatusCode());
        Assert.assertEquals(MATCH, result.getAttributes().getResult());
        Assert.assertNotNull(result.getAttributes());
        Assert.assertNotNull(result.getAttributes().getAlert());
        long endTime = System.currentTimeMillis();
        Assert.assertTrue(endTime > startTime + 300);
    }
}
