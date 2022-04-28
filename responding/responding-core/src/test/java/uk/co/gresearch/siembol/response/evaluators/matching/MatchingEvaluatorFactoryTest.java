package uk.co.gresearch.siembol.response.evaluators.matching;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import uk.co.gresearch.siembol.response.common.ProvidedEvaluators;
import uk.co.gresearch.siembol.response.common.RespondingResult;

public class MatchingEvaluatorFactoryTest {
    private final String attributes = """
            {
              "evaluation_result": "match",
              "matchers": [
                {
                  "is_enabled" : true,
                  "matcher_type": "IS_IN_SET",
                  "is_negated": false,
                  "field": "is_alert",
                  "data": "true"
                },
                {
                  "is_enabled" : true,
                  "matcher_type": "REGEX_MATCH",
                  "is_negated": false,
                  "field": "to_copy",
                  "data": "(?<new_field>.)"
                }
              ]
            }
            """;

    private MatchingEvaluatorFactory factory;

    @Before
    public void setUp() throws Exception {
        factory = new MatchingEvaluatorFactory();
    }

    @Test
    public void testGetType() {
        RespondingResult result = factory.getType();
        Assert.assertEquals(RespondingResult.StatusCode.OK, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes());
        Assert.assertEquals(ProvidedEvaluators.MATCHING_EVALUATOR.toString(),
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
    public void testValidateAttributesInvalidRegex() {
        RespondingResult result = factory.validateAttributes(attributes.replace("<new_field>", "["));
        Assert.assertEquals(RespondingResult.StatusCode.ERROR, result.getStatusCode());
        Assert.assertTrue(result.getAttributes().getMessage().contains("PatternSyntaxException"));
    }

    @Test
    public void testValidateAttributesDisabledMatchers() {
        RespondingResult result = factory.validateAttributes(attributes.replace("\"is_enabled\" : true",
                "\"is_enabled\" : false"));
        Assert.assertEquals(RespondingResult.StatusCode.ERROR, result.getStatusCode());
        Assert.assertTrue(result.getAttributes().getMessage().contains("Empty matchers in matching evaluator"));
    }

    @Test
    public void testValidateAttributesInvalid() {
        RespondingResult result = factory.validateAttributes(
                attributes.replace("\"match\"", "\"unsupported\""));
        Assert.assertEquals(RespondingResult.StatusCode.ERROR, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes().getMessage());
    }
}
