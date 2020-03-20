package uk.co.gresearch.nortem.response.evaluators.fixed;

import org.adrianwalker.multilinestring.Multiline;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import uk.co.gresearch.nortem.response.common.ProvidedEvaluators;
import uk.co.gresearch.nortem.response.common.RespondingResult;


public class FixedEvaluatorFactoryTest {
    /**
     *{"evaluation_result" : "match"}
     */
    @Multiline
    public static String attributes;

    private FixedEvaluatorFactory factory;

    @Before
    public void setUp() throws Exception {
        factory = new FixedEvaluatorFactory();
    }

    @Test
    public void testGetType() {
        RespondingResult result = factory.getType();
        Assert.assertEquals(RespondingResult.StatusCode.OK, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes());
        Assert.assertEquals(ProvidedEvaluators.FIXED_EVALUATOR.toString(), result.getAttributes().getEvaluatorType());
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
