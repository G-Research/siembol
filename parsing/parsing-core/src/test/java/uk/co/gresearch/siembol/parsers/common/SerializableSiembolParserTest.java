package uk.co.gresearch.siembol.parsers.common;

import org.adrianwalker.multilinestring.Multiline;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Map;

public class SerializableSiembolParserTest {
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
     *     ]
     *   }
     **/
    @Multiline
    public static String simpleGenericParser;

    /**
     * {"timestamp":"2019-03-27 18:52:02.732 Z"}
    **/
    @Multiline
    public static String message;

    @Test
    public void serializableTest() throws Exception {
        SerializableSiembolParser original = new SerializableSiembolParser(simpleGenericParser);

        List<Map<String, Object>> parsedOriginal = original.parse(message.getBytes());
        byte[] blob = SerializationUtils.serialize(original);
        Assert.assertTrue(blob.length > 0);
        SerializableSiembolParser clone = SerializationUtils.clone(original);
        List<Map<String, Object>> parsedClone = clone.parse(message.getBytes());

        Assert.assertTrue(parsedOriginal.equals(parsedClone));
        Assert.assertEquals(1553712722732L, parsedClone.get(0).get("timestamp"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void serializableInvalidTest() throws Exception {
        new SerializableSiembolParser("INVALID");
    }
}
