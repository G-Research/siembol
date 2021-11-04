package uk.co.gresearch.siembol.response.evaluators.assignment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import uk.co.gresearch.siembol.response.common.RespondingResult;
import uk.co.gresearch.siembol.response.common.ResponseAlert;
import uk.co.gresearch.siembol.response.model.AssignmentEvaluatorAttributesDto;
import uk.co.gresearch.siembol.response.model.JsonPathAssignmentTypeDto;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import static uk.co.gresearch.siembol.response.common.ResponseEvaluationResult.MATCH;
import static uk.co.gresearch.siembol.response.common.ResponseEvaluationResult.NO_MATCH;

public class JsonPathAssignmentEvaluatorTest {
    private static final ObjectReader JSON_ATTRIBUTES_READER = new ObjectMapper()
            .readerFor(AssignmentEvaluatorAttributesDto.class);
    private final String attributes = """
            {
              "assignment_type": "match_always",
              "field_name": "new_field",
              "json_path": "$.a"
            }
            """;

    private JsonPathAssignmentEvaluator evaluator;
    private ResponseAlert alert = new ResponseAlert();
    private AssignmentEvaluatorAttributesDto attributesDto;

    @Before
    public void setUp() throws IOException {
        alert = new ResponseAlert();
        alert.put("is_alert", true);
        alert.put("a", "secret");
        attributesDto = JSON_ATTRIBUTES_READER.readValue(attributes);
        evaluator = new JsonPathAssignmentEvaluator(attributesDto);
    }

    @Test
    public void testAssignmentEvalautorMatchNonEmptyPath() {
        RespondingResult result = evaluator.evaluate(alert);
        Assert.assertEquals(RespondingResult.StatusCode.OK, result.getStatusCode());
        Assert.assertEquals(MATCH, result.getAttributes().getResult());
        Assert.assertNotNull(result.getAttributes());
        Assert.assertNotNull(result.getAttributes().getAlert());
        ResponseAlert returnedAlert = result.getAttributes().getAlert();
        Assert.assertEquals(3, returnedAlert.size());
        Assert.assertEquals(alert.get("is_alert"), returnedAlert.get("is_alert"));
        Assert.assertEquals(alert.get("a"), returnedAlert.get("a"));
        Assert.assertEquals(alert.get("a"), returnedAlert.get("new_field"));
    }

    @Test
    public void testAssignmentEvalautorMatchEmptyPath() {
        attributesDto.setAssignmentType(JsonPathAssignmentTypeDto.MATCH_ALWAYS);
        evaluator = new JsonPathAssignmentEvaluator(attributesDto);
        alert.remove("a");
        RespondingResult result = evaluator.evaluate(alert);
        Assert.assertEquals(RespondingResult.StatusCode.OK, result.getStatusCode());
        Assert.assertEquals(MATCH, result.getAttributes().getResult());
        Assert.assertNotNull(result.getAttributes());
        Assert.assertNotNull(result.getAttributes().getAlert());
        ResponseAlert returnedAlert = result.getAttributes().getAlert();
        Assert.assertEquals(1, returnedAlert.size());
        Assert.assertEquals(alert.get("is_alert"), returnedAlert.get("is_alert"));
    }

    @Test
    public void testAssignmentEvalautorNoMatchEmptyPath() {
        attributesDto.setAssignmentType(JsonPathAssignmentTypeDto.NO_MATCH_WHEN_EMPTY);
        evaluator = new JsonPathAssignmentEvaluator(attributesDto);
        alert.remove("a");
        RespondingResult result = evaluator.evaluate(alert);
        Assert.assertEquals(RespondingResult.StatusCode.OK, result.getStatusCode());
        Assert.assertEquals(NO_MATCH, result.getAttributes().getResult());
        Assert.assertNotNull(result.getAttributes());
        Assert.assertNotNull(result.getAttributes().getAlert());
        ResponseAlert returnedAlert = result.getAttributes().getAlert();
        Assert.assertEquals(1, returnedAlert.size());
        Assert.assertEquals(alert.get("is_alert"), returnedAlert.get("is_alert"));
    }

    @Test
    public void testAssignmentEvalautorErrorEmptyPath() {
        attributesDto.setAssignmentType(JsonPathAssignmentTypeDto.ERROR_MATCH_WHEN_EMPTY);
        evaluator = new JsonPathAssignmentEvaluator(attributesDto);
        alert.remove("a");
        RespondingResult result = evaluator.evaluate(alert);
        Assert.assertEquals(RespondingResult.StatusCode.ERROR, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes().getMessage());
    }

    @Test
    public void testAssignmentEvalautorMatchNonEmptyList() {
        attributesDto.setJsonPath("$..users");
        evaluator = new JsonPathAssignmentEvaluator(attributesDto);
        alert.put("users", Arrays.asList("john", "peter", "sam"));
        RespondingResult result = evaluator.evaluate(alert);
        Assert.assertEquals(RespondingResult.StatusCode.OK, result.getStatusCode());
        Assert.assertEquals(MATCH, result.getAttributes().getResult());
        Assert.assertNotNull(result.getAttributes());
        Assert.assertNotNull(result.getAttributes().getAlert());
        ResponseAlert returnedAlert = result.getAttributes().getAlert();
        Assert.assertEquals(4, returnedAlert.size());
        Assert.assertEquals(alert.get("is_alert"), returnedAlert.get("is_alert"));
        Assert.assertEquals(alert.get("a"), returnedAlert.get("a"));
        Assert.assertEquals("[\"john\",\"peter\",\"sam\"]", returnedAlert.get("new_field"));
    }

    @Test
    public void testAssignmentEvalautorMatchEmptyList() {
        attributesDto.setJsonPath("$..users");
        evaluator = new JsonPathAssignmentEvaluator(attributesDto);
        alert.put("users", new ArrayList<String>());
        RespondingResult result = evaluator.evaluate(alert);
        Assert.assertEquals(RespondingResult.StatusCode.OK, result.getStatusCode());
        Assert.assertEquals(MATCH, result.getAttributes().getResult());
        Assert.assertNotNull(result.getAttributes());
        Assert.assertNotNull(result.getAttributes().getAlert());
        ResponseAlert returnedAlert = result.getAttributes().getAlert();
        Assert.assertEquals(4, returnedAlert.size());
        Assert.assertEquals(alert.get("is_alert"), returnedAlert.get("is_alert"));
        Assert.assertEquals(alert.get("a"), returnedAlert.get("a"));
        Assert.assertEquals("[]", returnedAlert.get("new_field"));
    }

    @Test
    public void testAssignmentEvalautorMatchInteger() {
        alert.put("a", 12345);
        RespondingResult result = evaluator.evaluate(alert);
        Assert.assertEquals(RespondingResult.StatusCode.OK, result.getStatusCode());
        Assert.assertEquals(MATCH, result.getAttributes().getResult());
        Assert.assertNotNull(result.getAttributes());
        Assert.assertNotNull(result.getAttributes().getAlert());
        ResponseAlert returnedAlert = result.getAttributes().getAlert();
        Assert.assertEquals(3, returnedAlert.size());
        Assert.assertEquals(alert.get("is_alert"), returnedAlert.get("is_alert"));
        Assert.assertEquals(alert.get("a"), returnedAlert.get("a"));
        Assert.assertEquals("12345", returnedAlert.get("new_field"));
    }
}
