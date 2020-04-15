package uk.co.gresearch.siembol.parsers.factory;

import org.adrianwalker.multilinestring.Multiline;
import org.junit.Assert;
import org.junit.Test;
import uk.co.gresearch.siembol.common.constants.SiembolMessageFields;

import java.util.List;
import java.util.Map;

public class ParserFactoryImplTest {
    private final ParserFactory factory;
    public ParserFactoryImplTest() throws Exception {
        factory = ParserFactoryImpl.createParserFactory();
    }

    /**
     *   {
     *     "parser_name" : "test",
     *     "parser_version" : 1,
     *     "parser_author" : "dummy",
     *     "parser_attributes": {
     *       "parser_type": "generic"
     *     },
     *     "parser_extractors": [
     *       {
     *         "extractor_type": "json_extractor",
     *         "name": "test",
     *         "field": "original_string",
     *         "post_processing_functions": [
     *           "format_timestamp"
     *         ],
     *         "attributes": {
     *           "should_overwrite_fields": true,
     *           "should_remove_field": false,
     *           "time_formats": [
     *             {
     *               "time_format": "yyyy-MM-dd HH:mm:ss.SSS 'Z'"
     *             }
     *           ]
     *         }
     *       }
     *     ],
     *     "transformations": [
     *     {
     *       "transformation_type": "field_name_string_replace",
     *       "attributes": {
     *         "string_replace_target": " ",
     *         "string_replace_replacement": "_"
     *       }
     *     }
     *   ]
     *   }
     **/
    @Multiline
    public static String simpleGenericParser;

    /**
     * {"timestamp":"2019-03-27 18:52:02.732 Z", "test field" : true, "test_field1" : "   message     ", "test_field2" : "   message     "}
     **/
    @Multiline
    public static String message;

    @Test
    public void testGetSchema() {
        ParserFactoryResult schemaResult = factory.getSchema();
        Assert.assertTrue(schemaResult.getStatusCode() == ParserFactoryResult.StatusCode.OK);
        Assert.assertFalse(schemaResult.getAttributes().getJsonSchema().isEmpty());
    }

    @Test
    public void testGoodCreate() {
        ParserFactoryResult result = factory.create(simpleGenericParser);
        Assert.assertTrue(result.getStatusCode() == ParserFactoryResult.StatusCode.OK);
        Assert.assertTrue(result.getAttributes().getSiembolParser() != null);

        List<Map<String, Object>> parsed = result.getAttributes().getSiembolParser().parse(message.getBytes());
        Assert.assertEquals(1553712722732L, parsed.get(0).get("timestamp"));
        Assert.assertEquals(true, parsed.get(0).get("test_field"));
        Assert.assertNull(parsed.get(0).get("source_type"));
    }

    @Test
    public void testInvalidCreate() {
        ParserFactoryResult result = factory.create("INVALID");
        Assert.assertTrue(result.getStatusCode() == ParserFactoryResult.StatusCode.ERROR);
        Assert.assertTrue(result.getAttributes().getMessage() != null);
    }

    @Test
    public void testValidationGood() {
        ParserFactoryResult result = factory.validateConfiguration(simpleGenericParser);
        Assert.assertTrue(result.getStatusCode() == ParserFactoryResult.StatusCode.OK);
    }

    @Test
    public void testTestingGood() {
        ParserFactoryResult result = factory.test(simpleGenericParser, null, message.getBytes());
        Assert.assertTrue(result.getStatusCode() == ParserFactoryResult.StatusCode.OK);
        List<Map<String, Object>> parsed = result.getAttributes().getParserResult().getParsedMessages();
        Assert.assertEquals(1553712722732L, parsed.get(0).get("timestamp"));
        Assert.assertEquals(true, parsed.get(0).get("test_field"));
        Assert.assertEquals("test", parsed.get(0).get(SiembolMessageFields.SENSOR_TYPE.toString()));
    }
}
