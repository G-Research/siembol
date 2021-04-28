package uk.co.gresearch.siembol.response.evaluators.arrayreducers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import org.adrianwalker.multilinestring.Multiline;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import uk.co.gresearch.siembol.response.common.RespondingResult;
import uk.co.gresearch.siembol.response.common.ResponseAlert;
import uk.co.gresearch.siembol.response.common.ResponseEvaluationResult;
import uk.co.gresearch.siembol.response.model.ArrayReducerTypeDto;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ArrayReducerEvaluatorTest {
    private static final ObjectReader RESPONSE_ALERT_READER = new ObjectMapper()
            .readerFor(ResponseAlert.class);

    /**
     * {"tmp_array" :[ {"f1" : "v11"}, {"f3": "v23", "f1" : "v21"}, {"f2" : "v31", "f3": "v33"} ] }
     */
    @Multiline
    public static String testAlertWithArray;


    private List<String> includingFields;
    private List<String> excludingFields;
    private ArrayReducerEvaluator.Builder builder;
    private ArrayReducerEvaluator evaluator;
    private ResponseAlert responseAlert;
    private String prefixName = "result_field";

    @Before
    public void setUp() throws IOException {
        includingFields = Arrays.asList(".*");
        excludingFields = new ArrayList<>();

        responseAlert = RESPONSE_ALERT_READER.readValue(testAlertWithArray);
        builder = new ArrayReducerEvaluator.Builder()
                .prefixName(prefixName).delimiter("_")
                .arrayFieldName("tmp_array")
                .patternFilter(includingFields, excludingFields);
    }


    @Test
    public void firstFieldReducerOk() {
        evaluator = builder.reducerType(ArrayReducerTypeDto.FIRST_FIELD).build();
        RespondingResult result = evaluator.evaluate(responseAlert);

        Assert.assertEquals(RespondingResult.StatusCode.OK, result.getStatusCode());
        Assert.assertEquals(ResponseEvaluationResult.MATCH, result.getAttributes().getResult());
        Assert.assertNotNull(result.getAttributes().getAlert());
        ResponseAlert alert = result.getAttributes().getAlert();
        Assert.assertEquals("v11", alert.get("result_field_f1"));
        Assert.assertEquals("v31", alert.get("result_field_f2"));
        Assert.assertEquals("v23", alert.get("result_field_f3"));
    }

    @Test
    public void firstFieldReducerWIthExcludedFieldOk() {
        excludingFields.add("f2");
        evaluator = builder.patternFilter(includingFields, excludingFields)
                .reducerType(ArrayReducerTypeDto.FIRST_FIELD).build();
        RespondingResult result = evaluator.evaluate(responseAlert);

        Assert.assertEquals(RespondingResult.StatusCode.OK, result.getStatusCode());
        Assert.assertEquals(ResponseEvaluationResult.MATCH, result.getAttributes().getResult());
        Assert.assertNotNull(result.getAttributes().getAlert());
        ResponseAlert alert = result.getAttributes().getAlert();
        Assert.assertEquals("v11", alert.get("result_field_f1"));
        Assert.assertNull(alert.get("result_field_f2"));
        Assert.assertEquals("v23", alert.get("result_field_f3"));
    }

    @Test
    public void firstFieldNoMatchNoArrayField() {
        responseAlert.remove("tmp_array");
        evaluator = builder.reducerType(ArrayReducerTypeDto.FIRST_FIELD).build();
        RespondingResult result = evaluator.evaluate(responseAlert);

        Assert.assertEquals(RespondingResult.StatusCode.OK, result.getStatusCode());
        Assert.assertEquals(ResponseEvaluationResult.NO_MATCH, result.getAttributes().getResult());
    }

    @Test
    public void firstFieldNoMatchNoArrayField2() {
        responseAlert.put("tmp_array", 1L);
        evaluator = builder.reducerType(ArrayReducerTypeDto.FIRST_FIELD).build();
        RespondingResult result = evaluator.evaluate(responseAlert);

        Assert.assertEquals(RespondingResult.StatusCode.ERROR, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes().getMessage());
    }

    @Test
    public void firstFieldReducerWIthAllExcludedFieldsNoMatchOk() {
        excludingFields.add(".*");
        evaluator = builder.patternFilter(includingFields, excludingFields)
                .reducerType(ArrayReducerTypeDto.FIRST_FIELD).build();
        RespondingResult result = evaluator.evaluate(responseAlert);

        Assert.assertEquals(RespondingResult.StatusCode.OK, result.getStatusCode());
        Assert.assertEquals(ResponseEvaluationResult.NO_MATCH, result.getAttributes().getResult());

    }

    @Test
    public void concatenateReducerOk() {
        evaluator = builder.reducerType(ArrayReducerTypeDto.CONCATENATE_FIELDS).build();
        RespondingResult result = evaluator.evaluate(responseAlert);

        Assert.assertEquals(RespondingResult.StatusCode.OK, result.getStatusCode());
        Assert.assertEquals(ResponseEvaluationResult.MATCH, result.getAttributes().getResult());
        Assert.assertNotNull(result.getAttributes().getAlert());
        ResponseAlert alert = result.getAttributes().getAlert();
        Assert.assertEquals("v11,v21", alert.get("result_field_f1"));
        Assert.assertEquals("v31", alert.get("result_field_f2"));
        Assert.assertEquals("v23,v33", alert.get("result_field_f3"));
    }

    @Test
    public void concatenateReducerWIthExcludedFieldOk() {
        excludingFields.add("f2");
        evaluator = builder.patternFilter(includingFields, excludingFields)
                .reducerType(ArrayReducerTypeDto.CONCATENATE_FIELDS).build();
        RespondingResult result = evaluator.evaluate(responseAlert);

        Assert.assertEquals(RespondingResult.StatusCode.OK, result.getStatusCode());
        Assert.assertEquals(ResponseEvaluationResult.MATCH, result.getAttributes().getResult());
        Assert.assertNotNull(result.getAttributes().getAlert());
        ResponseAlert alert = result.getAttributes().getAlert();
        Assert.assertEquals("v11,v21", alert.get("result_field_f1"));
        Assert.assertEquals("v23,v33", alert.get("result_field_f3"));
    }

    @Test
    public void concatenateNoMatchNoArrayField() {
        responseAlert.remove("tmp_array");
        evaluator = builder.reducerType(ArrayReducerTypeDto.CONCATENATE_FIELDS).build();
        RespondingResult result = evaluator.evaluate(responseAlert);

        Assert.assertEquals(RespondingResult.StatusCode.OK, result.getStatusCode());
        Assert.assertEquals(ResponseEvaluationResult.NO_MATCH, result.getAttributes().getResult());
    }

    @Test
    public void concatenateNoMatchNoArrayField2() {
        responseAlert.put("tmp_array", 1L);
        evaluator = builder.reducerType(ArrayReducerTypeDto.CONCATENATE_FIELDS).build();
        RespondingResult result = evaluator.evaluate(responseAlert);

        Assert.assertEquals(RespondingResult.StatusCode.ERROR, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes().getMessage());
    }

    @Test
    public void concatenateReducerWIthAllExcludedFieldsNoMatchOk() {
        excludingFields.add(".*");
        evaluator = builder.patternFilter(includingFields, excludingFields)
                .reducerType(ArrayReducerTypeDto.CONCATENATE_FIELDS).build();
        RespondingResult result = evaluator.evaluate(responseAlert);

        Assert.assertEquals(RespondingResult.StatusCode.OK, result.getStatusCode());
        Assert.assertEquals(ResponseEvaluationResult.NO_MATCH, result.getAttributes().getResult());
    }
}
