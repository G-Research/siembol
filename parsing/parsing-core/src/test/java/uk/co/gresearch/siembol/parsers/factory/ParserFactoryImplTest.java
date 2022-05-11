package uk.co.gresearch.siembol.parsers.factory;

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

    private final String simpleGenericParser = """
              {
                "parser_name" : "test",
                "parser_version" : 1,
                "parser_author" : "dummy",
                "parser_attributes": {
                  "parser_type": "generic"
                },
                "parser_extractors": [
                  {
                    "is_enabled": true,
                    "extractor_type": "json_extractor",
                    "name": "test",
                    "field": "original_string",
                    "post_processing_functions": [
                      "format_timestamp"
                    ],
                    "attributes": {
                      "skip_empty_values" : true,
                      "should_overwrite_fields": true,
                      "should_remove_field": false,
                      "time_formats": [
                        {
                          "time_format": "yyyy-MM-dd HH:mm:ss.SSS 'Z'"
                        }
                      ]
                    }
                  },
                  {
                    "is_enabled" : true,
                    "extractor_type": "pattern_extractor",
                    "name": "pattern",
                    "field": "dummy_field",
                    "attributes": {
                      "skip_empty_values" : true,
                      "should_overwrite_fields": false,
                      "should_remove_field": false,
                      "should_match_pattern": true,
                      "dot_all_regex_flag": true,
                      "regular_expressions" : [
                           "^(?<test_match>).*$"
                      ]
                    }
                  },
                  {
                    "is_enabled": true,
                    "extractor_type": "json_path_extractor",
                    "name": "json_path_queries",
                    "field": "json_field",
                    "attributes": {
                      "skip_empty_values": true,
                      "should_overwrite_fields": false,
                      "should_remove_field": false,
                      "at_least_one_query_result": true,
                      "json_path_queries": [
                        {
                          "output_field": "product_title",
                          "query": "$.products.title"
                        }
                      ]
                    }
                  }
                ],
                "transformations": [
                {
                  "is_enabled":true,
                  "transformation_type": "field_name_string_replace",
                  "attributes": {
                    "string_replace_target": " ",
                    "string_replace_replacement": "_"
                  }
                }
              ]
              }
            """;

    private final String message = """
     {"timestamp":"2019-03-27 18:52:02.732 Z", "test field" : true, "test_field1" : "   message     ", "test_field2" : "   message     ", "empty" : ""}""";

    @Test
    public void getSchema() {
        ParserFactoryResult schemaResult = factory.getSchema();
        Assert.assertSame(ParserFactoryResult.StatusCode.OK, schemaResult.getStatusCode());
        Assert.assertFalse(schemaResult.getAttributes().getJsonSchema().isEmpty());
    }

    @Test
    public void createOk() {
        ParserFactoryResult result = factory.create(simpleGenericParser);
        Assert.assertSame(ParserFactoryResult.StatusCode.OK, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes().getSiembolParser());

        List<Map<String, Object>> parsed = result.getAttributes().getSiembolParser().parse(message.getBytes());
        Assert.assertEquals(1553712722732L, parsed.get(0).get("timestamp"));
        Assert.assertEquals(true, parsed.get(0).get("test_field"));
        Assert.assertNull(parsed.get(0).get("source_type"));
        Assert.assertNull(parsed.get(0).get("empty"));

    }

    @Test
    public void createDisabledExtractor() {
        ParserFactoryResult result = factory.create(simpleGenericParser.replace("\"is_enabled\": true",
                "\"is_enabled\": false"));
        Assert.assertSame(ParserFactoryResult.StatusCode.OK, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes().getSiembolParser());

        List<Map<String, Object>> parsed = result.getAttributes().getSiembolParser().parse(message.getBytes());
        Assert.assertNotEquals(1553712722732L, parsed.get(0).get("timestamp"));
        Assert.assertNull(parsed.get(0).get("test_field"));
    }

    @Test
    public void createDisabledTransformation() {
        ParserFactoryResult result = factory.create(simpleGenericParser.replace("\"is_enabled\":true",
                "\"is_enabled\": false"));
        Assert.assertSame(ParserFactoryResult.StatusCode.OK, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes().getSiembolParser());

        List<Map<String, Object>> parsed = result.getAttributes().getSiembolParser().parse(message.getBytes());
        Assert.assertEquals(1553712722732L, parsed.get(0).get("timestamp"));
        Assert.assertEquals(true, parsed.get(0).get("test field"));
    }

    @Test
    public void invalidCreate() {
        ParserFactoryResult result = factory.create("INVALID");
        Assert.assertSame(ParserFactoryResult.StatusCode.ERROR, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes().getMessage());
    }

    @Test
    public void validationOk() {
        ParserFactoryResult result = factory.validateConfiguration(simpleGenericParser);
        Assert.assertSame(ParserFactoryResult.StatusCode.OK, result.getStatusCode());
    }

    @Test
    public void validationInvalidPatternExtractor() {
        ParserFactoryResult result = factory.validateConfiguration(simpleGenericParser.replace("<test_match>",
                "<test_match"));
        Assert.assertSame(ParserFactoryResult.StatusCode.ERROR, result.getStatusCode());
        Assert.assertTrue(result.getAttributes().getMessage().contains("No variables found"));
    }

    @Test
    public void testingOk() {
        ParserFactoryResult result = factory.test(simpleGenericParser, null, message.getBytes());
        Assert.assertSame(result.getStatusCode(), ParserFactoryResult.StatusCode.OK);
        List<Map<String, Object>> parsed = result.getAttributes().getParserResult().getParsedMessages();
        Assert.assertEquals(1553712722732L, parsed.get(0).get("timestamp"));
        Assert.assertEquals(true, parsed.get(0).get("test_field"));
        Assert.assertEquals("test", parsed.get(0).get(SiembolMessageFields.SENSOR_TYPE.toString()));
    }

    @Test
    public void testingInvalidPatternExtractor() {
        ParserFactoryResult result = factory.test(simpleGenericParser.replace("<test_match>",
                "<test_match"), null, message.getBytes());
        Assert.assertSame(ParserFactoryResult.StatusCode.ERROR, result.getStatusCode());
        Assert.assertTrue(result.getAttributes().getMessage().contains("No variables found"));
    }
}
