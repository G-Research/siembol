package uk.co.gresearch.siembol.parsers.generic;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import uk.co.gresearch.siembol.parsers.common.SiembolParser;
import uk.co.gresearch.siembol.parsers.common.ParserResult;
import uk.co.gresearch.siembol.parsers.factory.ParserFactory;
import uk.co.gresearch.siembol.parsers.factory.ParserFactoryImpl;

import java.util.Map;

public class SiembolGenericParserTest {
    private SiembolParser genericParser;
    private ParserFactory factory;

    private final String simpleGenericParserConfig = """
     {
      "parser_attributes": {
          "parser_type": "generic"
       },
       "parser_extractors" : [
       {
          "extractor_type": "pattern_extractor",
          "name": "simple_message",
          "field": "original_string",
          "attributes": {
             "regular_expressions": [
               "^msg:\\\\s(?<secret_msg>.*)$",
               "^msg2:\\\\s(?<timestamp>.*)$"
             ],
             "should_remove_field" : false
           }
         }],
         "transformations" : [
          {
              "transformation_type": "field_name_string_replace_all",
               "attributes": {
                  "string_replace_target": "secret_msg",
                  "string_replace_replacement": "dummy"
         }
         }]
      }
     """;

    private final String simpleGenericParserFiltered = """
     {
      "parser_attributes": {
          "parser_type": "generic"
       },
       "parser_extractors" : [
       {
          "extractor_type": "pattern_extractor",
          "name": "simple_message",
          "field": "original_string",
          "attributes": {
             "regular_expressions": [
               "^msg:\\\\s(?<secret_msg>.*)$"
             ],
             "should_remove_field" : false
           }
         }],
         "transformations" : [
          {
              "transformation_type": "filter_message",
               "attributes": {
                  "message_filter" : {
                      "matchers" : [
                      {
                          "field_name" : "secret_msg",
                          "pattern" : "secret",
                          "negated" : false
                    }]
               }}}]
      }
     """;

    private final String simpleMessage = """
     msg: secret""";

    @Before
    public void setUp() throws Exception {
        factory = ParserFactoryImpl.createParserFactory();
        genericParser = factory.create(simpleGenericParserConfig).getAttributes().getSiembolParser();
    }

    @Test
    public void goodSimpleMessage() {
        Map<String, Object> out = genericParser.parse(simpleMessage.trim().getBytes()).get(0);

        Assert.assertEquals(3, out.size());
        Assert.assertEquals("secret", out.get("dummy"));

        Assert.assertTrue(out.get("timestamp") instanceof Long);
        Assert.assertEquals(simpleMessage.trim(), out.get("original_string"));
    }

    @Test
    public void goodSimpleMessageDuplicates() {
        String message = "msg2: abc";
        Map<String, Object> out = genericParser.parse(message.getBytes()).get(0);

        Assert.assertEquals(3, out.size());
        Assert.assertEquals("abc", out.get("duplicate_timestamp_1"));
        Assert.assertTrue(out.get("timestamp") instanceof Long);
        Assert.assertEquals(message, out.get("original_string"));
    }

    @Test
    public void goodSimpleMessageFiltered() {
        genericParser = factory.create(simpleGenericParserFiltered).getAttributes().getSiembolParser();
        ParserResult result = genericParser.parseToResult(null, simpleMessage.trim().getBytes());
        Assert.assertTrue(result.getParsedMessages().isEmpty());
    }
}
