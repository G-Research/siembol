package uk.co.gresearch.siembol.response.evaluators.markdowntable;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import uk.co.gresearch.siembol.response.common.ProvidedEvaluators;
import uk.co.gresearch.siembol.response.common.RespondingResult;

public class ArrayTableFormatterEvaluatorFactoryTest {
    private final String attributes = """
     {
       "field_name": "output_field",
       "table_name": "test_table",
       "array_field": "test_array_field",
       "field_filter": {
         "including_fields": [
           ".*"
         ],
         "excluding_fields": [
           "secret"
         ]
       }
     }
     """;

    private ArrayTableFormatterEvaluatorFactory factory;

    @Before
    public void setUp() throws Exception {
        factory = new ArrayTableFormatterEvaluatorFactory();
    }

    @Test
    public void testGetType() {
        RespondingResult result = factory.getType();
        Assert.assertEquals(RespondingResult.StatusCode.OK, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes());
        Assert.assertEquals(ProvidedEvaluators.ARRAY_MARKDOWN_TABLE_FORMATTER_EVALUATOR.toString(),
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
    public void testValidateAttributesInvalid() {
        RespondingResult result = factory.validateAttributes(
                attributes.replace("\"field_name\"", "\"unsupported\""));
        Assert.assertEquals(RespondingResult.StatusCode.ERROR, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes().getMessage());
    }
}
