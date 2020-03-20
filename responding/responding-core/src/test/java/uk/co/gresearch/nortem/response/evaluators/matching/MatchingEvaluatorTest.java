package uk.co.gresearch.nortem.response.evaluators.matching;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import org.adrianwalker.multilinestring.Multiline;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import uk.co.gresearch.nortem.response.common.RespondingResult;
import uk.co.gresearch.nortem.response.common.ResponseAlert;
import uk.co.gresearch.nortem.response.model.MatchingEvaluatorAttributesDto;

import java.io.IOException;

import static uk.co.gresearch.nortem.response.common.ResponseEvaluationResult.FILTERED;
import static uk.co.gresearch.nortem.response.common.ResponseEvaluationResult.MATCH;
import static uk.co.gresearch.nortem.response.common.ResponseEvaluationResult.NO_MATCH;

public class MatchingEvaluatorTest {
    private static final ObjectReader JSON_ATTRIBUTES_READER = new ObjectMapper()
            .readerFor(MatchingEvaluatorAttributesDto.class);
    /**
     * {
     *   "evaluation_result": "match",
     *   "matchers": [
     *     {
     *       "matcher_type": "REGEX_MATCH",
     *       "is_negated": false,
     *       "field": "is_alert",
     *       "data": "(?i)true"
     *     },
     *     {
     *       "matcher_type": "REGEX_MATCH",
     *       "is_negated": false,
     *       "field": "to_copy",
     *       "data": "(?<new_field>.*)"
     *     }
     *   ]
     * }
     */
    @Multiline
    public static String attributes;
    private MatchingEvaluator evaluator;
    private ResponseAlert alert = new ResponseAlert();
    private MatchingEvaluatorAttributesDto attributesDto;

    @Before
    public void setUp() throws IOException {
        alert = new ResponseAlert();
        alert.put("is_alert", true);
        alert.put("to_copy", "secret");
        attributesDto = JSON_ATTRIBUTES_READER.readValue(attributes);
        evaluator = new MatchingEvaluator(attributesDto);
    }

    @Test
    public void testMatchingEvaluatorFiltered() throws IOException {
        attributesDto = JSON_ATTRIBUTES_READER.readValue(
                attributes.replace("\"match\"", "\"filtered\""));
        evaluator = new MatchingEvaluator(attributesDto);
        RespondingResult result = evaluator.evaluate(alert);
        Assert.assertEquals(RespondingResult.StatusCode.OK, result.getStatusCode());
        Assert.assertEquals(FILTERED, result.getAttributes().getResult());
        Assert.assertNotNull(result.getAttributes());
        Assert.assertNotNull(result.getAttributes().getAlert());
        ResponseAlert returnedAlert = result.getAttributes().getAlert();
        Assert.assertEquals(3, returnedAlert.size());
        Assert.assertEquals(alert.get("is_alert"), returnedAlert.get("is_alert"));
        Assert.assertEquals(alert.get("to_copy"), returnedAlert.get("to_copy"));
        Assert.assertEquals(alert.get("to_copy"), returnedAlert.get("new_field"));
    }

    @Test
    public void testMatchingEvaluatorMatch() {
        RespondingResult result = evaluator.evaluate(alert);
        Assert.assertEquals(RespondingResult.StatusCode.OK, result.getStatusCode());
        Assert.assertEquals(MATCH, result.getAttributes().getResult());
        Assert.assertNotNull(result.getAttributes());
        Assert.assertNotNull(result.getAttributes().getAlert());
        ResponseAlert returnedAlert = result.getAttributes().getAlert();
        Assert.assertEquals(3, returnedAlert.size());
        Assert.assertEquals(alert.get("is_alert"), returnedAlert.get("is_alert"));
        Assert.assertEquals(alert.get("to_copy"), returnedAlert.get("to_copy"));
        Assert.assertEquals(alert.get("to_copy"), returnedAlert.get("new_field"));
    }

    @Test
    public void testMatchingEvaluatorNoMatch() {
        alert.put("is_alert", false);
        RespondingResult result = evaluator.evaluate(alert);
        Assert.assertEquals(RespondingResult.StatusCode.OK, result.getStatusCode());
        Assert.assertEquals(NO_MATCH, result.getAttributes().getResult());
        Assert.assertNotNull(result.getAttributes());
        Assert.assertNotNull(result.getAttributes().getAlert());
        ResponseAlert returnedAlert = result.getAttributes().getAlert();
        Assert.assertEquals(2, returnedAlert.size());
        Assert.assertEquals(alert.get("is_alert"), returnedAlert.get("is_alert"));
        Assert.assertEquals(alert.get("to_copy"), returnedAlert.get("to_copy"));
    }
}
