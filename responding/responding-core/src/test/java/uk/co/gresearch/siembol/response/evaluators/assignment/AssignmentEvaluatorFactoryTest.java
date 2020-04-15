package uk.co.gresearch.siembol.response.evaluators.assignment;

import org.adrianwalker.multilinestring.Multiline;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import uk.co.gresearch.siembol.response.common.ProvidedEvaluators;
import uk.co.gresearch.siembol.response.common.RespondingResult;
import uk.co.gresearch.siembol.response.common.ResponseAlert;

public class AssignmentEvaluatorFactoryTest {
    /**
     * {
     *   "assignment_type": "match_always",
     *   "field_name": "new_field",
     *   "json_path": "$..a"
     * }
     */
    @Multiline
    public static String attributes;

    private AssignmentEvaluatorFactory factory;
    private ResponseAlert alert = new ResponseAlert();

    @Before
    public void setUp() throws Exception {
        factory = new AssignmentEvaluatorFactory();
    }

    @Test
    public void testGetType() {
        RespondingResult result = factory.getType();
        Assert.assertEquals(RespondingResult.StatusCode.OK, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes());
        Assert.assertEquals(ProvidedEvaluators.ASSIGNMENT_EVALUATOR.toString(), result.getAttributes().getEvaluatorType());
    }

    @Test
    public void testGetAttributesJsonSchema(){
        RespondingResult result = factory.getAttributesJsonSchema();
        Assert.assertEquals(RespondingResult.StatusCode.OK, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes());
        Assert.assertNotNull(result.getAttributes().getAttributesSchema());
    }

    @Test
    public void testCreateInstance(){
        RespondingResult result = factory.createInstance(attributes);
        Assert.assertEquals(RespondingResult.StatusCode.OK, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes());
        Assert.assertNotNull(result.getAttributes().getRespondingEvaluator());
    }

    @Test
    public void testValidateAttributesOk(){
        RespondingResult result = factory.validateAttributes(attributes);
        Assert.assertEquals(RespondingResult.StatusCode.OK, result.getStatusCode());
    }

    @Test
    public void testValidateAttributesInvalidJson(){
        RespondingResult result = factory.validateAttributes("INVALID");
        Assert.assertEquals(RespondingResult.StatusCode.ERROR, result.getStatusCode());
    }

    @Test
    public void testValidateAttributesInvalid(){
        RespondingResult result = factory.validateAttributes(attributes.replace("match", "_"));
        Assert.assertEquals(RespondingResult.StatusCode.ERROR, result.getStatusCode());
    }
}
