package uk.co.gresearch.siembol.response.evaluators.arrayreducers;

import org.adrianwalker.multilinestring.Multiline;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import uk.co.gresearch.siembol.response.common.ProvidedEvaluators;
import uk.co.gresearch.siembol.response.common.RespondingResult;
import uk.co.gresearch.siembol.response.common.ResponseAlert;

public class ArrayReducerEvaluatorFactoryTest {
    /**
     * {
     *   "array_reducer_type": "first_field",
     *   "prefix_name": "test",
     *   "field_name_delimiter": "_",
     *   "array_field": "test_array_field",
     *   "field_filter": {
     *     "including_fields": [
     *       ".*"
     *     ],
     *     "excluding_fields": [
     *       "secret"
     *     ]
     *   }
     * }
     */
    @Multiline
    public static String attributes;

    private ArrayReducerEvaluatorFactory factory;
    private ResponseAlert alert = new ResponseAlert();

    @Before
    public void setUp() throws Exception {
        factory = new ArrayReducerEvaluatorFactory();
    }

    @Test
    public void testGetType() {
        RespondingResult result = factory.getType();
        Assert.assertEquals(RespondingResult.StatusCode.OK, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes());
        Assert.assertEquals(ProvidedEvaluators.ARRAY_REDUCER_EVALUATOR.toString(), result.getAttributes().getEvaluatorType());
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
        RespondingResult result = factory.validateAttributes(attributes.replace("array_reducer_type", "_"));
        Assert.assertEquals(RespondingResult.StatusCode.ERROR, result.getStatusCode());
    }
}
